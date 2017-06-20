/*
 * Copyright 2008-2009 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package voldemort.server.protocol.admin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import voldemort.VoldemortException;
import voldemort.client.ClientConfig;
import voldemort.client.protocol.VoldemortFilter;
import voldemort.client.protocol.admin.AdminClient;
import voldemort.client.protocol.admin.ProtoBuffAdminClientRequestFormat;
import voldemort.client.protocol.admin.filter.DefaultVoldemortFilter;
import voldemort.client.protocol.pb.ProtoUtils;
import voldemort.client.protocol.pb.VAdminProto;
import voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest;
import voldemort.cluster.Cluster;
import voldemort.routing.RoutingStrategy;
import voldemort.server.StoreRepository;
import voldemort.server.VoldemortConfig;
import voldemort.server.protocol.RequestHandler;
import voldemort.store.ErrorCodeMapper;
import voldemort.store.StorageEngine;
import voldemort.store.metadata.MetadataStore;
import voldemort.utils.ByteArray;
import voldemort.utils.ByteUtils;
import voldemort.utils.ClosableIterator;
import voldemort.utils.EventThrottler;
import voldemort.utils.NetworkClassLoader;
import voldemort.utils.Pair;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;

import com.google.protobuf.Message;

/**
 * Protocol buffers implementation of a {@link RequestHandler}
 * 
 * @author afeinberg
 */
public class ProtoBuffAdminServiceRequestHandler implements RequestHandler {

    private final static Logger logger = Logger.getLogger(ProtoBuffAdminServiceRequestHandler.class);

    private final ErrorCodeMapper errorCodeMapper;
    private final MetadataStore metadataStore;
    private final StoreRepository storeRepository;
    private final NetworkClassLoader networkClassLoader;
    private final VoldemortConfig voldemortConfig;
    private final AsyncOperationRunner asyncRunner;

    private final static int ASYNC_REQUEST_THREADS = 8;
    private final static int ASYNC_REQUEST_CACHE_SIZE = 64;

    private final AtomicInteger lastOperationId = new AtomicInteger(0);

    public ProtoBuffAdminServiceRequestHandler(ErrorCodeMapper errorCodeMapper,
                                               StoreRepository storeRepository,
                                               MetadataStore metadataStore,
                                               VoldemortConfig voldemortConfig) {
        this.errorCodeMapper = errorCodeMapper;
        this.metadataStore = metadataStore;
        this.storeRepository = storeRepository;
        this.voldemortConfig = voldemortConfig;
        this.networkClassLoader = new NetworkClassLoader(Thread.currentThread()
                                                               .getContextClassLoader());
        this.asyncRunner = new AsyncOperationRunner(ASYNC_REQUEST_THREADS, ASYNC_REQUEST_CACHE_SIZE);
    }

    public void handleRequest(final DataInputStream inputStream, final DataOutputStream outputStream)
            throws IOException {
        final VoldemortAdminRequest.Builder request = ProtoUtils.readToBuilder(inputStream,
                                                                               VoldemortAdminRequest.newBuilder());

        switch(request.getType()) {
            case GET_METADATA:
                ProtoUtils.writeMessage(outputStream, handleGetMetadata(request.getGetMetadata()));
                break;
            case UPDATE_METADATA:
                ProtoUtils.writeMessage(outputStream,
                                        handleUpdateMetadata(request.getUpdateMetadata()));
                break;
            case DELETE_PARTITION_ENTRIES:
                ProtoUtils.writeMessage(outputStream,
                                        handleDeletePartitionEntries(request.getDeletePartitionEntries()));
                break;
            case FETCH_PARTITION_ENTRIES:
                handleFetchPartitionEntries(request.getFetchPartitionEntries(), outputStream);
                break;
            case UPDATE_PARTITION_ENTRIES:
                handleUpdatePartitionEntries(request.getUpdatePartitionEntries(),
                                             inputStream,
                                             outputStream);
                break;
            case INITIATE_FETCH_AND_UPDATE:
                ProtoUtils.writeMessage(outputStream,
                                        handleFetchAndUpdate(request.getInitiateFetchAndUpdate()));
                break;
            case ASYNC_OPERATION_STATUS:
                ProtoUtils.writeMessage(outputStream,
                                        handleAsyncStatus(request.getAsyncOperationStatus()));
                break;
            default:
                throw new VoldemortException("Unkown operation " + request.getType());
        }

    }

    public void handleFetchPartitionEntries(VAdminProto.FetchPartitionEntriesRequest request,
                                            DataOutputStream outputStream) throws IOException {
        try {
            String storeName = request.getStore();
            StorageEngine<ByteArray, byte[]> storageEngine = getStorageEngine(storeName);
            RoutingStrategy routingStrategy = metadataStore.getRoutingStrategy(storageEngine.getName());
            EventThrottler throttler = new EventThrottler(voldemortConfig.getStreamMaxReadBytesPerSec());
            List<Integer> partitionList = request.getPartitionsList();
            VoldemortFilter filter = (request.hasFilter()) ? getFilterFromRequest(request.getFilter())
                                                          : new DefaultVoldemortFilter();

            // boolean switch to fetchEntries/fetchKeys Only
            boolean fetchValues = request.hasFetchValues() && request.getFetchValues();

            ClosableIterator<Pair<ByteArray, Versioned<byte[]>>> iterator = storageEngine.entries();
            while(iterator.hasNext()) {
                Pair<ByteArray, Versioned<byte[]>> entry = iterator.next();

                if(validPartition(entry.getFirst().get(), partitionList, routingStrategy)
                   && filter.accept(entry.getFirst(), entry.getSecond())) {

                    VAdminProto.FetchPartitionEntriesResponse.Builder response = VAdminProto.FetchPartitionEntriesResponse.newBuilder();

                    if(fetchValues) {
                        VAdminProto.PartitionEntry partitionEntry = VAdminProto.PartitionEntry.newBuilder()
                                                                                              .setKey(ProtoUtils.encodeBytes(entry.getFirst()))
                                                                                              .setVersioned(ProtoUtils.encodeVersioned(entry.getSecond()))
                                                                                              .build();
                        response.setPartitionEntry(partitionEntry);
                    } else {
                        response.setKey(ProtoUtils.encodeBytes(entry.getFirst()));
                    }

                    Message message = response.build();
                    ProtoUtils.writeMessage(outputStream, message);

                    if(throttler != null) {
                        throttler.maybeThrottle(entrySize(entry));
                    }
                }
            }

            iterator.close();
            ProtoUtils.writeEndOfStream(outputStream);
        } catch(VoldemortException e) {
            VAdminProto.FetchPartitionEntriesResponse response = VAdminProto.FetchPartitionEntriesResponse.newBuilder()
                                                                                                          .setError(ProtoUtils.encodeError(errorCodeMapper,
                                                                                                                                           e))
                                                                                                          .build();

            ProtoUtils.writeMessage(outputStream, response);
        }
    }

    public void handleUpdatePartitionEntries(VAdminProto.UpdatePartitionEntriesRequest originalRequest,
                                             DataInputStream inputStream,
                                             DataOutputStream outputStream) throws IOException {
        VAdminProto.UpdatePartitionEntriesRequest request = originalRequest;
        VAdminProto.UpdatePartitionEntriesResponse.Builder response = VAdminProto.UpdatePartitionEntriesResponse.newBuilder();
        boolean continueReading = true;

        try {
            String storeName = request.getStore();
            StorageEngine<ByteArray, byte[]> storageEngine = getStorageEngine(storeName);
            VoldemortFilter filter = (request.hasFilter()) ? getFilterFromRequest(request.getFilter())
                                                          : new DefaultVoldemortFilter();

            EventThrottler throttler = new EventThrottler(voldemortConfig.getStreamMaxWriteBytesPerSec());
            while(continueReading) {
                VAdminProto.PartitionEntry partitionEntry = request.getPartitionEntry();
                ByteArray key = ProtoUtils.decodeBytes(partitionEntry.getKey());
                Versioned<byte[]> value = ProtoUtils.decodeVersioned(partitionEntry.getVersioned());

                if(filter.accept(key, value)) {
                    storageEngine.put(key, value);

                    if(throttler != null) {
                        throttler.maybeThrottle(entrySize(Pair.create(key, value)));
                    }
                }

                int size = inputStream.readInt();

                if(size <= 0)
                    continueReading = false;
                else {
                    byte[] input = new byte[size];
                    ByteUtils.read(inputStream, input);
                    VAdminProto.UpdatePartitionEntriesRequest.Builder builder = VAdminProto.UpdatePartitionEntriesRequest.newBuilder();
                    builder.mergeFrom(input);
                    request = builder.build();
                }
            }
        } catch(VoldemortException e) {
            response.setError(ProtoUtils.encodeError(errorCodeMapper, e));
        } finally {
            ProtoUtils.writeMessage(outputStream, response.build());
        }
    }

    public static String requestToId(String storeName, int nodeId, List<Integer> partitions) {
        StringBuilder requestIdBuilder = new StringBuilder();
        for(int partition: partitions) {
            requestIdBuilder.append('p').append(partition).append(';');
        }
        requestIdBuilder.append(storeName).append(nodeId);
        return requestIdBuilder.toString();
    }

    public VAdminProto.AsyncOperationStatusResponse handleFetchAndUpdate(VAdminProto.InitiateFetchAndUpdateRequest request) {
        final int nodeId = request.getNodeId();
        Cluster cluster = metadataStore.getCluster();
        final List<Integer> partitions = request.getPartitionsList();
        final VoldemortFilter filter = request.hasFilter() ? getFilterFromRequest(request.getFilter())
                                                          : new DefaultVoldemortFilter();
        final String storeName = request.getStore();

        int requestId = lastOperationId.getAndIncrement();
        VAdminProto.AsyncOperationStatusResponse.Builder response = VAdminProto.AsyncOperationStatusResponse.newBuilder()
                                                                                                            .setRequestId(requestId)
                                                                                                            .setComplete(false)
                                                                                                            .setDescription("Fetch and update")
                                                                                                            .setStatus("started");

        try {
            asyncRunner.startRequest(requestId, new AsyncOperation(requestId, "Fetch and Update") {

                @Override
                public void operate() {
                    AdminClient adminClient = createTempAdminClient();
                    try {
                        StorageEngine<ByteArray, byte[]> storageEngine = getStorageEngine(storeName);
                        Iterator<Pair<ByteArray, Versioned<byte[]>>> entriesIterator = adminClient.fetchPartitionEntries(nodeId,
                                                                                                                         storeName,
                                                                                                                         partitions,
                                                                                                                         filter);
                        updateStatus("Initated fetchPartitionEntries");
                        EventThrottler throttler = new EventThrottler(voldemortConfig.getStreamMaxWriteBytesPerSec());
                        for(long i = 0; entriesIterator.hasNext(); i++) {
                            Pair<ByteArray, Versioned<byte[]>> entry = entriesIterator.next();
                            storageEngine.put(entry.getFirst(), entry.getSecond());

                            throttler.maybeThrottle(entrySize(entry));

                            if((i % 1000) == 0) {
                                updateStatus(i + " entries processed");
                            }
                        }
                    } finally {
                        adminClient.stop();
                    }
                }

                private AdminClient createTempAdminClient() {
                    ClientConfig config = new ClientConfig();
                    config.setMaxConnectionsPerNode(1);
                    config.setMaxThreads(1);
                    config.setConnectionTimeout(voldemortConfig.getAdminConnectionTimeout(),
                                                TimeUnit.MILLISECONDS);
                    config.setSocketTimeout(voldemortConfig.getAdminSocketTimeout(),
                                            TimeUnit.MILLISECONDS);
                    config.setSocketBufferSize(voldemortConfig.getAdminSocketBufferSize());

                    return new ProtoBuffAdminClientRequestFormat(metadataStore.getCluster(), config);
                }
            });

        } catch(VoldemortException e) {
            response.setError(ProtoUtils.encodeError(errorCodeMapper, e));
        }

        return response.build();
    }

    public VAdminProto.AsyncOperationStatusResponse handleAsyncStatus(VAdminProto.AsyncOperationStatusRequest request) {
        VAdminProto.AsyncOperationStatusResponse.Builder response = VAdminProto.AsyncOperationStatusResponse.newBuilder();
        try {
            int requestId = request.getRequestId();
            String requestStatus = asyncRunner.getRequestStatus(requestId);
            boolean requestComplete = asyncRunner.isComplete(requestId);
            response.setDescription("description");
            response.setComplete(requestComplete);
            response.setStatus(requestStatus);
            response.setRequestId(requestId);
        } catch(VoldemortException e) {
            response.setError(ProtoUtils.encodeError(errorCodeMapper, e));
        }

        return response.build();
    }

    public VAdminProto.DeletePartitionEntriesResponse handleDeletePartitionEntries(VAdminProto.DeletePartitionEntriesRequest request) {
        VAdminProto.DeletePartitionEntriesResponse.Builder response = VAdminProto.DeletePartitionEntriesResponse.newBuilder();

        try {
            String storeName = request.getStore();
            List<Integer> partitions = request.getPartitionsList();
            StorageEngine<ByteArray, byte[]> storageEngine = getStorageEngine(storeName);
            VoldemortFilter filter = (request.hasFilter()) ? getFilterFromRequest(request.getFilter())
                                                          : new DefaultVoldemortFilter();
            RoutingStrategy routingStrategy = metadataStore.getRoutingStrategy(storageEngine.getName());

            EventThrottler throttler = new EventThrottler(voldemortConfig.getStreamMaxReadBytesPerSec());
            ClosableIterator<Pair<ByteArray, Versioned<byte[]>>> iterator = storageEngine.entries();
            int deleteSuccess = 0;

            while(iterator.hasNext()) {
                Pair<ByteArray, Versioned<byte[]>> entry = iterator.next();

                if(validPartition(entry.getFirst().get(), partitions, routingStrategy)
                   && filter.accept(entry.getFirst(), entry.getSecond())) {
                    if(storageEngine.delete(entry.getFirst(), entry.getSecond().getVersion()))
                        deleteSuccess++;
                    if(throttler != null)
                        throttler.maybeThrottle(entrySize(entry));
                }
            }
            iterator.close();
            response.setCount(deleteSuccess);
        } catch(VoldemortException e) {
            response.setError(ProtoUtils.encodeError(errorCodeMapper, e));
        }

        return response.build();
    }

    public VAdminProto.UpdateMetadataResponse handleUpdateMetadata(VAdminProto.UpdateMetadataRequest request) {
        VAdminProto.UpdateMetadataResponse.Builder response = VAdminProto.UpdateMetadataResponse.newBuilder();

        try {
            ByteArray key = ProtoUtils.decodeBytes(request.getKey());
            String keyString = ByteUtils.getString(key.get(), "UTF-8");

            if(MetadataStore.METADATA_KEYS.contains(keyString)) {
                Versioned<byte[]> versionedValue = ProtoUtils.decodeVersioned(request.getVersioned());
                metadataStore.put(new ByteArray(ByteUtils.getBytes(keyString, "UTF-8")),
                                  versionedValue);
            }
        } catch(VoldemortException e) {
            response.setError(ProtoUtils.encodeError(errorCodeMapper, e));
        }

        return response.build();
    }

    public VAdminProto.GetMetadataResponse handleGetMetadata(VAdminProto.GetMetadataRequest request) {
        VAdminProto.GetMetadataResponse.Builder response = VAdminProto.GetMetadataResponse.newBuilder();

        try {
            ByteArray key = ProtoUtils.decodeBytes(request.getKey());
            String keyString = ByteUtils.getString(key.get(), "UTF-8");
            if(MetadataStore.METADATA_KEYS.contains(keyString)) {
                List<Versioned<byte[]>> versionedList = metadataStore.get(key);
                int size = (versionedList.size() > 0) ? 1 : 0;

                if(size > 0) {
                    Versioned<byte[]> versioned = versionedList.get(0);
                    response.setVersion(ProtoUtils.encodeVersioned(versioned));
                }
            } else {
                throw new VoldemortException("Metadata Key passed " + keyString
                                             + " is not handled yet ...");
            }
        } catch(VoldemortException e) {
            response.setError(ProtoUtils.encodeError(errorCodeMapper, e));
        }

        return response.build();
    }

    /**
     * This method is used by non-blocking code to determine if the give buffer
     * represents a complete request. Because the non-blocking code can by
     * definition not just block waiting for more data, it's possible to get
     * partial reads, and this identifies that case.
     * 
     * @param buffer Buffer to check; the buffer is reset to position 0 before
     *        calling this method and the caller must reset it after the call
     *        returns
     * @return True if the buffer holds a complete request, false otherwise
     */
    public boolean isCompleteRequest(ByteBuffer buffer) {
        throw new VoldemortException("Non-blocking server not supported for ProtoBuffAdminServiceRequestHandler");
    }

    /* Private helper methods */
    private VoldemortFilter getFilterFromRequest(VAdminProto.VoldemortFilter request) {
        VoldemortFilter filter;
        byte[] classBytes = ProtoUtils.decodeBytes(request.getData()).get();
        String className = request.getName();

        try {
            Class<?> cl = networkClassLoader.loadClass(className, classBytes, 0, classBytes.length);
            filter = (VoldemortFilter) cl.newInstance();
        } catch(Exception e) {
            throw new VoldemortException("Failed to load and instantiate the filter class", e);
        }

        return filter;
    }

    private static int entrySize(Pair<ByteArray, Versioned<byte[]>> entry) {
        return entry.getFirst().get().length + entry.getSecond().getValue().length
               + ((VectorClock) entry.getSecond().getVersion()).sizeInBytes() + 1;
    }

    private StorageEngine<ByteArray, byte[]> getStorageEngine(String storeName) {
        StorageEngine<ByteArray, byte[]> storageEngine = storeRepository.getStorageEngine(storeName);

        if(storageEngine == null) {
            throw new VoldemortException("No store named '" + storeName + "'.");
        }

        return storageEngine;
    }

    protected boolean validPartition(byte[] key,
                                     List<Integer> partitionList,
                                     RoutingStrategy routingStrategy) {
        List<Integer> keyPartitions = routingStrategy.getPartitionList(key);

        for(int p: partitionList) {
            if(keyPartitions.contains(p)) {
                return true;
            }
        }

        return false;
    }
}

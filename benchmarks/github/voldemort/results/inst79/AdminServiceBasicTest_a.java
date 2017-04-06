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

package voldemort.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import voldemort.ServerTestUtils;
import voldemort.TestUtils;
import voldemort.client.protocol.admin.AdminClient;
import voldemort.cluster.Cluster;
import voldemort.cluster.Node;
import voldemort.routing.RoutingStrategy;
import voldemort.routing.RoutingStrategyFactory;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;
import voldemort.store.Store;
import voldemort.store.StoreDefinition;
import voldemort.store.metadata.MetadataStore;
import voldemort.utils.ByteArray;
import voldemort.utils.ByteUtils;
import voldemort.utils.Pair;
import voldemort.versioning.Versioned;

import com.google.common.collect.ImmutableList;

/**
 * @author afeinberg
 */
public class AdminServiceBasicTest extends TestCase {

    private static String storeName = "test-replication-memory";
    private static String storesXmlfile = "test/common/voldemort/config/stores.xml";

    VoldemortConfig config;
    VoldemortServer server;
    Cluster cluster;

    @Override
    public void setUp() throws IOException {
        // start 2 node cluster with free ports
        int[] ports = ServerTestUtils.findFreePorts(3);
        Node node0 = new Node(0, "localhost", ports[0], ports[1], ports[2], Arrays.asList(0, 1));

        ports = ServerTestUtils.findFreePorts(3);
        Node node1 = new Node(1, "localhost", ports[0], ports[1], ports[2], Arrays.asList(2, 3));

        cluster = new Cluster("admin-service-test", Arrays.asList(node0, node1));
        config = ServerTestUtils.createServerConfig(0,
                                                    TestUtils.createTempDir().getAbsolutePath(),
                                                    null,
                                                    storesXmlfile);
        server = new VoldemortServer(config, cluster);
        server.start();
    }

    @Override
    public void tearDown() throws IOException, InterruptedException {
        server.stop();
    }

    public AdminClient getAdminClient() {
        return ServerTestUtils.getAdminClient(server.getMetadataStore().getCluster());
    }

    public void testUpdateClusterMetadata() {
        Cluster cluster = server.getMetadataStore().getCluster();
        List<Node> nodes = new ArrayList<Node>(cluster.getNodes());
        nodes.add(new Node(3, "localhost", 8883, 6668, ImmutableList.of(4, 5)));
        Cluster updatedCluster = new Cluster("new-cluster", nodes);

        AdminClient client = getAdminClient();
        client.updateRemoteCluster(server.getIdentityNode().getId(), updatedCluster);

        assertEquals("Cluster should match", updatedCluster, server.getMetadataStore().getCluster());
        assertEquals("AdminClient.getMetdata() should match",
                     client.getRemoteCluster(server.getIdentityNode().getId()).getValue(),
                     updatedCluster);

    }

    public void testStateTransitions() {
        // change to REBALANCING STATE
        AdminClient client = getAdminClient();
        client.updateRemoteServerState(server.getIdentityNode().getId(),
                                       MetadataStore.VoldemortState.REBALANCING_MASTER_SERVER);

        MetadataStore.VoldemortState state = server.getMetadataStore().getServerState();
        assertEquals("State should be changed correctly to rebalancing state",
                     MetadataStore.VoldemortState.REBALANCING_MASTER_SERVER,
                     state);

        // change back to NORMAL state
        client.updateRemoteServerState(server.getIdentityNode().getId(),
                                       MetadataStore.VoldemortState.NORMAL_SERVER);

        state = server.getMetadataStore().getServerState();
        assertEquals("State should be changed correctly to rebalancing state",
                     MetadataStore.VoldemortState.NORMAL_SERVER,
                     state);

        // lets revert back to REBALANCING STATE AND CHECK
        client.updateRemoteServerState(server.getIdentityNode().getId(),
                                       MetadataStore.VoldemortState.REBALANCING_MASTER_SERVER);

        state = server.getMetadataStore().getServerState();

        assertEquals("State should be changed correctly to rebalancing state",
                     MetadataStore.VoldemortState.REBALANCING_MASTER_SERVER,
                     state);

        client.updateRemoteServerState(server.getIdentityNode().getId(),
                                       MetadataStore.VoldemortState.NORMAL_SERVER);

        state = server.getMetadataStore().getServerState();
        assertEquals("State should be changed correctly to rebalancing state",
                     MetadataStore.VoldemortState.NORMAL_SERVER,
                     state);
    }

    public void testDeletePartitionEntries() {
        Store<ByteArray, byte[]> store = server.getStoreRepository().getStorageEngine(storeName);
        assertNotSame("Store '" + storeName + "' should not be null", null, store);

        Set<Pair<ByteArray, Versioned<byte[]>>> entrySet = createEntries();
        for(Pair<ByteArray, Versioned<byte[]>> entry: entrySet) {
            store.put(entry.getFirst(), entry.getSecond());
        }

        getAdminClient().deletePartitions(0, storeName, Arrays.asList(0, 2), null);

        RoutingStrategy routingStrategy = server.getMetadataStore().getRoutingStrategy(storeName);
        for(Pair<ByteArray, Versioned<byte[]>> entry: entrySet) {
            if(routingStrategy.getPartitionList(entry.getFirst().get()).contains(0)
               || routingStrategy.getPartitionList(entry.getFirst().get()).contains(2)) {
                assertEquals("store should be missing all 0,2 entries",
                             0,
                             store.get(entry.getFirst()).size());
            } else {
                assertEquals("store should have all 1,3 entries", 1, store.get(entry.getFirst())
                                                                          .size());
                assertEquals("entry should match",
                             entry.getSecond().getValue(),
                             store.get(entry.getFirst()).get(0).getValue());
            }
        }

    }

    public void testFetchPartitionKeys() throws IOException {
        Store<ByteArray, byte[]> store = server.getStoreRepository().getStorageEngine(storeName);
        Set<Pair<ByteArray, Versioned<byte[]>>> entrySet = createEntries();
        RoutingStrategy routingStrategy = server.getMetadataStore().getRoutingStrategy(storeName);
        Set<String> expected = new HashSet<String>();

        for(Pair<ByteArray, Versioned<byte[]>> entry: entrySet) {
            store.put(entry.getFirst(), entry.getSecond());
            if(routingStrategy.getPartitionList(entry.getFirst().get()).contains(0)
               || routingStrategy.getPartitionList(entry.getFirst().get()).contains(1)) {
                expected.add(new String(entry.getFirst().get()));
            }
        }

        int checked = 0;
        int matched = 0;

        AdminClient client = getAdminClient();
        Iterator<ByteArray> fetchIt = client.fetchPartitionKeys(server.getIdentityNode().getId(),
                                                                storeName,
                                                                Arrays.asList(0, 1),
                                                                null);
        while(fetchIt.hasNext()) {
            ByteArray fetchedKey = fetchIt.next();
            checked++;

            String fetchedKeyStr = new String(fetchedKey.get());
            if(expected.contains(fetchedKeyStr))
                matched++;
        }
        assertEquals(expected.size(), checked);
        assertEquals("All values should have matched", checked, matched);
    }

    public void testFetch() throws IOException {
        Store<ByteArray, byte[]> store = server.getStoreRepository().getStorageEngine(storeName);
        Set<Pair<ByteArray, Versioned<byte[]>>> entrySet = createEntries();
        RoutingStrategy routingStrategy = server.getMetadataStore().getRoutingStrategy(storeName);
        Map<String, String> expected = new HashMap<String, String>();

        for(Pair<ByteArray, Versioned<byte[]>> entry: entrySet) {
            store.put(entry.getFirst(), entry.getSecond());
            if(routingStrategy.getPartitionList(entry.getFirst().get()).contains(0)
               || routingStrategy.getPartitionList(entry.getFirst().get()).contains(1)) {
                expected.put(new String(entry.getFirst().get()), new String(entry.getSecond()
                                                                                 .getValue()));

            }
        }

        int checked = 0;
        int matched = 0;

        AdminClient client = getAdminClient();
        Iterator<Pair<ByteArray, Versioned<byte[]>>> fetchIt = client.fetchPartitionEntries(server.getIdentityNode()
                                                                                                  .getId(),
                                                                                            storeName,
                                                                                            Arrays.asList(0,
                                                                                                          1),
                                                                                            null);
        while(fetchIt.hasNext()) {
            Pair<ByteArray, Versioned<byte[]>> fetchedKv = fetchIt.next();
            checked++;

            String fetchedKey = new String(fetchedKv.getFirst().get());
            String fetchedValue = new String(fetchedKv.getSecond().getValue());

            if(expected.get(fetchedKey).equals(fetchedValue))
                matched++;
        }
        assertEquals(expected.size(), checked);
        assertEquals("All values should have matched", checked, matched);

    }

    public void testUpdate() {
        Store<ByteArray, byte[]> store = server.getStoreRepository().getStorageEngine(storeName);
        assertNotSame("Store '" + storeName + "' should not be null", null, store);

        Iterator<Pair<ByteArray, Versioned<byte[]>>> iterator = createEntries().iterator();

        // Write
        AdminClient client = getAdminClient();

        client.updateEntries(0, storeName, iterator, null);

        for(int i = 100; i <= 104; i++) {
            assertNotSame("Store should return a valid value",
                          "value-" + i,
                          new String(store.get(new ByteArray(ByteUtils.getBytes("" + i, "UTF-8")))
                                          .get(0)
                                          .getValue()));
        }
    }

    /**
     * @throws IOException
     */
    public void testFetchAndUpdate() throws IOException {
        Store<ByteArray, byte[]> store = server.getStoreRepository().getStorageEngine(storeName);
        assertNotSame("Store '" + storeName + "' should not be null", null, store);

        Set<Pair<ByteArray, Versioned<byte[]>>> entrySet = createEntries();

        for(Pair<ByteArray, Versioned<byte[]>> entry: entrySet) {
            store.put(entry.getFirst(), entry.getSecond());
        }

        // lets start a new server
        VoldemortConfig config2 = ServerTestUtils.createServerConfig(1,
                                                                     TestUtils.createTempDir()
                                                                              .getAbsolutePath(),
                                                                     null,
                                                                     storesXmlfile);
        VoldemortServer server2 = new VoldemortServer(config2, cluster);
        server2.start();

        // assert server2 is missing all keys
        for(Pair<ByteArray, Versioned<byte[]>> entry: entrySet) {
            assertEquals("Server2 should return empty result List for all",
                         0,
                         server2.getStoreRepository()
                                .getStorageEngine(storeName)
                                .get(entry.getFirst())
                                .size());
        }

        // use pipeGetAndPutStream to add values to server2
        AdminClient client = getAdminClient();

        client.fetchAndUpdateStreams(0, 1, storeName, Arrays.asList(0, 1), null);

        // assert all partition 0, 1 keys present in server 2
        Store<ByteArray, byte[]> store2 = server2.getStoreRepository().getStorageEngine(storeName);
        assertNotSame("Store '" + storeName + "' should not be null", null, store2);

        StoreDefinition storeDef = server.getMetadataStore().getStoreDef(storeName);
        assertNotSame("StoreDefinition for 'users' should not be null", null, storeDef);
        RoutingStrategy routingStrategy = new RoutingStrategyFactory().updateRoutingStrategy(storeDef,
                                                                                             server.getMetadataStore()
                                                                                                   .getCluster());

        int checked = 0;
        int matched = 0;
        for(int i = 100; i <= 1000; i++) {
            ByteArray key = new ByteArray(ByteUtils.getBytes("" + i, "UTF-8"));
            byte[] value = ByteUtils.getBytes("value-" + i, "UTF-8");

            if(routingStrategy.getPartitionList(key.get()).get(0) == 0
               || routingStrategy.getPartitionList(key.get()).get(0) == 1) {
                checked++;
                if(store2.get(key).size() > 0
                   && new String(value).equals(new String(store2.get(key).get(0).getValue()))) {
                    matched++;
                }
            }
        }

        server2.stop();
        assertEquals("All Values should have matched", checked, matched);
    }

    private Set<Pair<ByteArray, Versioned<byte[]>>> createEntries() {
        Set<Pair<ByteArray, Versioned<byte[]>>> entrySet = new HashSet<Pair<ByteArray, Versioned<byte[]>>>();

        for(int i = 0; i <= 1000; i++) {
            ByteArray key = new ByteArray(ByteUtils.getBytes("" + i, "UTF-8"));
            Versioned<byte[]> value = new Versioned<byte[]>(ByteUtils.getBytes("value-" + i,
                                                                               "UTF-8"));
            entrySet.add(new Pair<ByteArray, Versioned<byte[]>>(key, value));
        }

        return entrySet;
    }

}

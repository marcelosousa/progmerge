@Override
 protected Table getTableWithHeader (final RestRequest request)
{
  Table table = new Table();
  table.startHeaders();
  table.addCell("id", "default:false;alias:id,nodeId;desc:unique node id");
  table.addCell("pid", "default:false;alias:p;desc:process id");
  table.addCell("ip", "alias:i;desc:ip address");
  table.addCell("port", "default:false;alias:po;desc:bound transport port");
  table.addCell("http_address", "default:false;alias:http;desc:bound http address");
  table.addCell("version", "default:false;alias:v;desc:es version");
  table.addCell("build", "default:false;alias:b;desc:es build hash");
  table.addCell("jdk", "default:false;alias:j;desc:jdk version");
  table.addCell("disk.avail", "default:false;alias:d,disk,diskAvail;text-align:right;desc:available disk space");
  table.addCell("heap.current", "default:false;alias:hc,heapCurrent;text-align:right;desc:used heap");
  table.addCell("heap.percent", "alias:hp,heapPercent;text-align:right;desc:used heap ratio");
  table.addCell("heap.max", "default:false;alias:hm,heapMax;text-align:right;desc:max configured heap");
  table.addCell("ram.current", "default:false;alias:rc,ramCurrent;text-align:right;desc:used machine memory");
  table.addCell("ram.percent", "alias:rp,ramPercent;text-align:right;desc:used machine memory ratio");
  table.addCell("ram.max", "default:false;alias:rm,ramMax;text-align:right;desc:total machine memory");
  table.addCell("file_desc.current", "default:false;alias:fdc,fileDescriptorCurrent;text-align:right;desc:used file descriptors");
  table.addCell("file_desc.percent", "default:false;alias:fdp,fileDescriptorPercent;text-align:right;desc:used file descriptor ratio");
  table.addCell("file_desc.max", "default:false;alias:fdm,fileDescriptorMax;text-align:right;desc:max file descriptors");
  table.addCell("cpu", "alias:cpu;text-align:right;desc:recent cpu usage");
  table.addCell("load_1m", "alias:l;text-align:right;desc:1m load avg");
  table.addCell("load_5m", "alias:l;text-align:right;desc:5m load avg");
  table.addCell("load_15m", "alias:l;text-align:right;desc:15m load avg");
  table.addCell("uptime", "default:false;alias:u;text-align:right;desc:node uptime");
  table.addCell("node.role", "alias:r,role,dc,nodeRole;desc:d:data node, c:client node");
  table.addCell("master", "alias:m;desc:m:master-eligible, *:current master");
  table.addCell("name", "alias:n;desc:node name");
  table.addCell("completion.size", "alias:cs,completionSize;default:false;text-align:right;desc:size of completion");
  table.addCell("fielddata.memory_size", "alias:fm,fielddataMemory;default:false;text-align:right;desc:used fielddata cache");
  table.addCell("fielddata.evictions", "alias:fe,fielddataEvictions;default:false;text-align:right;desc:fielddata evictions");
  table.addCell("query_cache.memory_size", "alias:qcm,queryCacheMemory;default:false;text-align:right;desc:used query cache");
  table.addCell("query_cache.evictions", "alias:qce,queryCacheEvictions;default:false;text-align:right;desc:query cache evictions");
  table.addCell("request_cache.memory_size", "alias:rcm,requestCacheMemory;default:false;text-align:right;desc:used request cache");
  table.addCell("request_cache.evictions", "alias:rce,requestCacheEvictions;default:false;text-align:right;desc:request cache evictions");
  table.addCell("request_cache.hit_count", "alias:rchc,requestCacheHitCount;default:false;text-align:right;desc:request cache hit counts");
  table.addCell("request_cache.miss_count", "alias:rcmc,requestCacheMissCount;default:false;text-align:right;desc:request cache miss counts");
  table.addCell("flush.total", "alias:ft,flushTotal;default:false;text-align:right;desc:number of flushes");
  table.addCell("flush.total_time", "alias:ftt,flushTotalTime;default:false;text-align:right;desc:time spent in flush");
  table.addCell("get.current", "alias:gc,getCurrent;default:false;text-align:right;desc:number of current get ops");
  table.addCell("get.time", "alias:gti,getTime;default:false;text-align:right;desc:time spent in get");
  table.addCell("get.total", "alias:gto,getTotal;default:false;text-align:right;desc:number of get ops");
  table.addCell("get.exists_time", "alias:geti,getExistsTime;default:false;text-align:right;desc:time spent in successful gets");
  table.addCell("get.exists_total", "alias:geto,getExistsTotal;default:false;text-align:right;desc:number of successful gets");
  table.addCell("get.missing_time", "alias:gmti,getMissingTime;default:false;text-align:right;desc:time spent in failed gets");
  table.addCell("get.missing_total", "alias:gmto,getMissingTotal;default:false;text-align:right;desc:number of failed gets");
  table.addCell("indexing.delete_current", "alias:idc,indexingDeleteCurrent;default:false;text-align:right;desc:number of current deletions");
  table.addCell("indexing.delete_time", "alias:idti,indexingDeleteTime;default:false;text-align:right;desc:time spent in deletions");
  table.addCell("indexing.delete_total", "alias:idto,indexingDeleteTotal;default:false;text-align:right;desc:number of delete ops");
  table.addCell("indexing.index_current", "alias:iic,indexingIndexCurrent;default:false;text-align:right;desc:number of current indexing ops");
  table.addCell("indexing.index_time", "alias:iiti,indexingIndexTime;default:false;text-align:right;desc:time spent in indexing");
  table.addCell("indexing.index_total", "alias:iito,indexingIndexTotal;default:false;text-align:right;desc:number of indexing ops");
  table.addCell("indexing.index_failed", "alias:iif,indexingIndexFailed;default:false;text-align:right;desc:number of failed indexing ops");
  table.addCell("merges.current", "alias:mc,mergesCurrent;default:false;text-align:right;desc:number of current merges");
  table.addCell("merges.current_docs", "alias:mcd,mergesCurrentDocs;default:false;text-align:right;desc:number of current merging docs");
  table.addCell("merges.current_size", "alias:mcs,mergesCurrentSize;default:false;text-align:right;desc:size of current merges");
  table.addCell("merges.total", "alias:mt,mergesTotal;default:false;text-align:right;desc:number of completed merge ops");
  table.addCell("merges.total_docs", "alias:mtd,mergesTotalDocs;default:false;text-align:right;desc:docs merged");
  table.addCell("merges.total_size", "alias:mts,mergesTotalSize;default:false;text-align:right;desc:size merged");
  table.addCell("merges.total_time", "alias:mtt,mergesTotalTime;default:false;text-align:right;desc:time spent in merges");
  table.addCell("percolate.current", "alias:pc,percolateCurrent;default:false;text-align:right;desc:number of current percolations");
  table.addCell("percolate.memory_size", "alias:pm,percolateMemory;default:false;text-align:right;desc:memory used by percolations");
  table.addCell("percolate.queries", "alias:pq,percolateQueries;default:false;text-align:right;desc:number of registered percolation queries");
  table.addCell("percolate.time", "alias:pti,percolateTime;default:false;text-align:right;desc:time spent percolating");
  table.addCell("percolate.total", "alias:pto,percolateTotal;default:false;text-align:right;desc:total percolations");
  table.addCell("refresh.total", "alias:rto,refreshTotal;default:false;text-align:right;desc:total refreshes");
  table.addCell("refresh.time", "alias:rti,refreshTime;default:false;text-align:right;desc:time spent in refreshes");
  table.addCell("script.compilations", "alias:scrcc,scriptCompilations;default:false;text-align:right;desc:script compilations");
  table.addCell("script.cache_evictions", "alias:scrce,scriptCacheEvictions;default:false;text-align:right;desc:script cache evictions");
  table.addCell("search.fetch_current", "alias:sfc,searchFetchCurrent;default:false;text-align:right;desc:current fetch phase ops");
  table.addCell("search.fetch_time", "alias:sfti,searchFetchTime;default:false;text-align:right;desc:time spent in fetch phase");
  table.addCell("search.fetch_total", "alias:sfto,searchFetchTotal;default:false;text-align:right;desc:total fetch ops");
  table.addCell("search.open_contexts", "alias:so,searchOpenContexts;default:false;text-align:right;desc:open search contexts");
  table.addCell("search.query_current", "alias:sqc,searchQueryCurrent;default:false;text-align:right;desc:current query phase ops");
  table.addCell("search.query_time", "alias:sqti,searchQueryTime;default:false;text-align:right;desc:time spent in query phase");
  table.addCell("search.query_total", "alias:sqto,searchQueryTotal;default:false;text-align:right;desc:total query phase ops");
  table.addCell("search.scroll_current", "alias:scc,searchScrollCurrent;default:false;text-align:right;desc:open scroll contexts");
  table.addCell("search.scroll_time", "alias:scti,searchScrollTime;default:false;text-align:right;desc:time scroll contexts held open");
  table.addCell("search.scroll_total", "alias:scto,searchScrollTotal;default:false;text-align:right;desc:completed scroll contexts");
  table.addCell("segments.count", "alias:sc,segmentsCount;default:false;text-align:right;desc:number of segments");
  table.addCell("segments.memory", "alias:sm,segmentsMemory;default:false;text-align:right;desc:memory used by segments");
  table.addCell("segments.index_writer_memory", "alias:siwm,segmentsIndexWriterMemory;default:false;text-align:right;desc:memory used by index writer");
  table.addCell("segments.index_writer_max_memory", "alias:siwmx,segmentsIndexWriterMaxMemory;default:false;text-align:right;desc:maximum memory index writer may use before it must write buffered documents to a new segment");
  table.addCell("segments.version_map_memory", "alias:svmm,segmentsVersionMapMemory;default:false;text-align:right;desc:memory used by version map");
  table.addCell("segments.fixed_bitset_memory", "alias:sfbm,fixedBitsetMemory;default:false;text-align:right;desc:memory used by fixed bit sets for nested object field types and type filters for types referred in _parent fields");
  table.addCell("suggest.current", "alias:suc,suggestCurrent;default:false;text-align:right;desc:number of current suggest ops");
  table.addCell("suggest.time", "alias:suti,suggestTime;default:false;text-align:right;desc:time spend in suggest");
  table.addCell("suggest.total", "alias:suto,suggestTotal;default:false;text-align:right;desc:number of suggest ops");
  table.endHeaders();
  return table;
}
@Override
 protected Table getTableWithHeader (final RestRequest request)
{
  Table table = new Table();
  table.startHeaders().addCell("index", "default:true;alias:i,idx;desc:index name").addCell("shard", "default:true;alias:s,sh;desc:shard name").addCell("prirep", "alias:p,pr,primaryOrReplica;default:true;desc:primary or replica").addCell("state", "default:true;alias:st;desc:shard state").addCell("docs", "alias:d,dc;text-align:right;desc:number of docs in shard").addCell("store", "alias:sto;text-align:right;desc:store size of shard (how much disk it uses)").addCell("ip", "default:true;desc:ip of node where it lives").addCell("id", "default:false;desc:unique id of node where it lives").addCell("node", "default:true;alias:n;desc:name of node where it lives");
  table.addCell("sync_id", "alias:sync_id;default:false;desc:sync id");
  table.addCell("unassigned.reason", "alias:ur;default:false;desc:reason shard is unassigned");
  table.addCell("unassigned.at", "alias:ua;default:false;desc:time shard became unassigned (UTC)");
  table.addCell("unassigned.for", "alias:uf;default:false;text-align:right;desc:time has been unassigned");
  table.addCell("unassigned.details", "alias:ud;default:false;desc:additional details as to why the shard became unassigned");
  table.addCell("completion.size", "alias:cs,completionSize;default:false;text-align:right;desc:size of completion");
  table.addCell("fielddata.memory_size", "alias:fm,fielddataMemory;default:false;text-align:right;desc:used fielddata cache");
  table.addCell("fielddata.evictions", "alias:fe,fielddataEvictions;default:false;text-align:right;desc:fielddata evictions");
  table.addCell("query_cache.memory_size", "alias:fcm,queryCacheMemory;default:false;text-align:right;desc:used query cache");
  table.addCell("query_cache.evictions", "alias:fce,queryCacheEvictions;default:false;text-align:right;desc:query cache evictions");
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
  table.addCell("warmer.current", "alias:wc,warmerCurrent;default:false;text-align:right;desc:current warmer ops");
  table.addCell("warmer.total", "alias:wto,warmerTotal;default:false;text-align:right;desc:total warmer ops");
  table.addCell("warmer.total_time", "alias:wtt,warmerTotalTime;default:false;text-align:right;desc:time spent in warmers");
  table.endHeaders();
  return table;
}
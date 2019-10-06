package com.qlangtech.tis.solrextend.handler.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.util.PriorityQueue;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.search.DocListAndSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * 对命中结果集进行group by count(1) 操作 <br>
 * 可以通用
 * 
 * @author 百岁
 *
 * @date 2019年9月17日
 */
public class GroupByAndCountSearchComponent extends SearchComponent {

	private static final String NAME = "group_by_count";
	private static final String TIS_GROUP_FIELD = "tis.group.field";
	private static final String TIS_GROUP_LIMIT = "tis.group.limit";

	private static final Logger logger = LoggerFactory.getLogger(GroupByAndCountSearchComponent.class);

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {

		SolrParams params = rb.req.getParams();
		Integer purpose = params.getInt(ShardParams.SHARDS_PURPOSE);
		if (purpose != null && (purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0) {
			return;
		}

		if (params.getBool(NAME, false)) {
			rb.setNeedDocSet(true);
			SolrParams r = rb.req.getParams();
			int groupLimit = r.getInt(TIS_GROUP_LIMIT, 50);
			String groupField = r.get(TIS_GROUP_FIELD);
			if (StringUtils.isEmpty(groupField)) {
				throw new IllegalArgumentException("param:" + TIS_GROUP_FIELD + " can not be empty");
			}
			String[] groupFieldArg = StringUtils.split(groupField, ":");
			if (groupFieldArg.length != 2) {
				throw new IllegalArgumentException("param:" + TIS_GROUP_FIELD + " is not illegal");
			}
			rb.req.getContext().put(NAME,
					new GroupByCountContext(groupFieldArg[0], DVType.parse(groupFieldArg[1]), groupLimit));

			log("prep");
		}
	}

	private void log(String message) {
		// logger.warn("{}ip:{}->{}", Thread.currentThread().hashCode(),
		// MDC.get("ip"), message);
	}

	@Override
	public void process(ResponseBuilder rb) throws IOException {
		GroupByCountContext countContext = null;
		if ((countContext = (GroupByCountContext) rb.req.getContext().get(NAME)) == null) {
			return;
		}

		DocListAndSet results = rb.getResults();
		CountCollector collector = new CountCollector(countContext);
		rb.req.getSearcher().search(results.docSet.getTopFilter(), collector);

		//log("process," + collector.segmentSummary.toString());
		rb.rsp.add(NAME, ResultUtils.writeMap(collector.getGroupByCount()));
	}

	// @Override
	// public int distributedProcess(ResponseBuilder rb) throws IOException {
	// if (!rb.req.getParams().getBool(NAME, false)) {
	// return ResponseBuilder.STAGE_DONE;
	// }
	//
	// if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
	// createSubRequests(rb);
	// }
	// return ResponseBuilder.STAGE_DONE;
	// }

	@Override
	@SuppressWarnings("all")
	public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
		GroupByCountContext countContext = null;
		if ((countContext = (GroupByCountContext) rb.req.getContext().get(NAME)) == null) {
			return;
		}
		if (rb.stage != ResponseBuilder.STAGE_EXECUTE_QUERY) {
			return;
		}

		SimpleOrderedMap<String> shardResult = null;
		Map<String /* group key */, AtomicInteger> mergeResult = new HashMap<>();
		AtomicInteger val = null;
		String key = null;
		// 将几个分片数据合并
		for (ShardResponse sr : sreq.responses) {
			shardResult = (SimpleOrderedMap<String>) sr.getSolrResponse().getResponse().get(NAME);
			for (int i = 0; i < shardResult.size(); i++) {
				key = shardResult.getName(i);
				if ((val = mergeResult.get(key)) == null) {
					val = new AtomicInteger();
					mergeResult.put(key, val);
				}
				val.addAndGet(Integer.parseInt(shardResult.getVal(i)));
			}
		}
		DocVal top = null;
		int hitCount;
		SortQueue sortQueue = new SortQueue(countContext.groupLimit);
		for (Map.Entry<String /* group key */, AtomicInteger> entry : mergeResult.entrySet()) {
			if ((hitCount = entry.getValue().get()) > (top = sortQueue.top()).count) {
				top.count = hitCount;
				top.keyVal = entry.getKey();
				sortQueue.updateTop();
			}
		}

		mergeResult.clear();
		while ((top = sortQueue.pop()) != null) {
			mergeResult.put(top.keyVal, new AtomicInteger(top.count));
		}

		log("handleResponses,mergeResult.size:" + mergeResult.size());

		rb.rsp.add(NAME, ResultUtils.writeMap(mergeResult));
	}


	private static class CountCollector extends SimpleCollector {
		private final String groupByField;
		// private SortedDocValues groupFieldDV = null;

		private DVProcess dvProcessor;
		// private Map<Order/* order */, AtomicInteger> groupByCountSegment;
		private int[] groupByCountSegment;
		private final Map<String/* groupField */, AtomicInteger> groupByCount = Maps.newHashMap();
		private final DVType dvType;

		private final int sortQueueLimit;
		// private final Order offset = new Order();
		private SortQueue sortQueue;

		//private int segmentCount;
		//private StringBuffer segmentSummary = new StringBuffer();

		public CountCollector(GroupByCountContext countContext) {
			super();
			if (countContext == null) {
				throw new IllegalArgumentException("countContext can not be null");
			}
			this.groupByField = countContext.groupBy;
			this.dvType = countContext.dvType;
			this.sortQueueLimit = countContext.groupLimit;

		}

		public Map<String, AtomicInteger> getGroupByCount() {
			try {
				this.mergeResult();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this.groupByCount;
		}

		public boolean needsScores() {
			return false;
		}

		protected void doSetNextReader(LeafReaderContext context) throws IOException {
			mergeResult();
			this.groupByCountSegment = new int[context.reader().maxDoc()];
			this.dvProcessor = dvType.createDVProcess(context, groupByField);
			this.sortQueue = new SortQueue(this.sortQueueLimit);
//			segmentSummary.append("\nsegment_").append(++segmentCount).append(",length:")
//					.append(this.groupByCountSegment.length);

		}

		private void mergeResult() throws IOException {

			if (dvProcessor != null) {
				AtomicInteger groupCount = null;
				int hitCount;
				DocVal top = null;
				//int[] count = new int[1];
				for (int i = 0; i < this.groupByCountSegment.length; i++) {
					if ((hitCount = groupByCountSegment[i]) > 0) {
						if (hitCount > (top = sortQueue.top()).count) {
							top.count = hitCount;
							top.keyVal = this.dvProcessor.getValByOrder(i);
							sortQueue.updateTop();
						}
					}
				}
				//segmentSummary.append(",hit:").append(count[0]);
				while ((top = this.sortQueue.pop()) != null) {
					groupCount = groupByCount.get(top.keyVal);
					if (groupCount == null) {
						groupCount = new AtomicInteger();
						groupByCount.put(top.keyVal, groupCount);
					}
					groupCount.addAndGet(top.count);
				}
			}

			this.dvProcessor = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			int ord = dvProcessor.ordValue(doc);
			try {
				groupByCountSegment[ord]++;
			} catch (Exception e) {
				throw new RuntimeException("ord:" + ord, e);
			}
		}

	}

	@Override
	public String getDescription() {
		return NAME;
	}

	public enum DVType {
		Numeric("numeric", (context, fname) -> {
			// final NumericDocValues numericGroupDV =
			// org.apache.lucene.index.DocValues.getNumeric(context.reader(),
			// fname);
			throw new UnsupportedOperationException();
			// return new DVProcess() {
			// public int ordValue(int docId) throws IOException {
			// // numericGroupDV.advance(docId);
			// // return numericGroupDV.longValue();
			// throw new UnsupportedOperationException();
			// }
			//
			// public String getValByOrder(int ord) throws IOException {
			// throw new UnsupportedOperationException();
			// }
			// };
		})//
		, Str("string", (context, fname) -> {

			final SortedDocValues groupFieldDV = org.apache.lucene.index.DocValues.getSorted(context.reader(), fname);

			return new DVProcess() {
				public int ordValue(int docId) throws IOException {
					groupFieldDV.advance(docId);
					return groupFieldDV.ordValue();
				}

				public String getValByOrder(int ord) throws IOException {
					return groupFieldDV.lookupOrd(ord).utf8ToString();
				}
			};
		});

		private final String type;
		private final DVProcessCreator dvProcessCreator;

		private DVType(String type, DVProcessCreator dvProcessCreator) {
			this.type = type;
			this.dvProcessCreator = dvProcessCreator;
		}

		public DVProcess createDVProcess(LeafReaderContext context, String fieldName) throws IOException {
			return dvProcessCreator.createDVProcess(context, fieldName);
		}

		public static DVType parse(String type) {
			if (Str.type.equals(type)) {
				return Str;
			}
			throw new IllegalStateException("type:" + type + " is illegal");
		}
	}

	private interface DVProcess {
		int ordValue(int docId) throws IOException;

		String getValByOrder(int ord) throws IOException;
	}

	private interface DVProcessCreator {
		public DVProcess createDVProcess(LeafReaderContext context, String fieldName) throws IOException;
	}

	static class SortQueue extends PriorityQueue<DocVal> {
		public SortQueue(int len) {
			super(len, () -> new DocVal(Integer.MIN_VALUE));
		}

		protected boolean lessThan(DocVal t1, DocVal t2) {
			return t1.count < t2.count;
		}
	}

	public static void main(String[] args) {

		SortQueue queue = new SortQueue(10);
		List<DocVal> docs = new ArrayList<>();
		DocVal doc = null;
		for (int i = 0; i < 100; i++) {
			doc = new DocVal((int) (Math.random() * 300));
			docs.add(doc);
			System.out.println(doc.count);
		}

		DocVal top = null;

		System.out.println("============================================");
		for (DocVal d : docs) {
			if (d.count > (top = queue.top()).count) {
				top.count = d.count;
				top.docBase = d.docBase;
				top.keyVal = "";
				queue.updateTop();
			}
		}
		System.out.println("============================================");
		while ((top = queue.pop()) != null) {
			System.out.println("count:" + top.count + ",docId:" + top.keyVal);
		}

	}

	private static class DocVal {
		private String keyVal;
		private int docBase;

		public DocVal(int count) {
			super();
			this.count = count;
		}

		public int count;
	}

	private static class GroupByCountContext {
		private final String groupBy;
		private final DVType dvType;
		private final int groupLimit;

		public GroupByCountContext(String groupBy, DVType dvType, int groupLimit) {
			super();
			this.groupBy = groupBy;
			this.dvType = dvType;
			this.groupLimit = groupLimit;
		}

	}

}

package com.qlangtech.tis.solrextend.handler.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.SimpleCollector;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.cloud.CloudDescriptor;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrException;
//import org.apache.solr.common.StringUtils;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.search.DocListAndSet;

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

	private String groupBy;
	private DVType dvType;

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		if (rb.req.getParams().getBool(NAME, false)) {
			rb.setNeedDocSet(true);
			SolrParams params = rb.req.getParams();
			String groupField = params.get(TIS_GROUP_FIELD);
			if (StringUtils.isEmpty(groupField)) {
				throw new IllegalArgumentException("param:" + TIS_GROUP_FIELD + " can not be empty");
			}
			String[] groupFieldArg = StringUtils.split(groupField, ":");
			if (groupFieldArg.length != 2) {
				throw new IllegalArgumentException("param:" + TIS_GROUP_FIELD + " is not illegal");
			}
			this.groupBy = groupFieldArg[0];
			dvType = DVType.parse(groupFieldArg[1]);
		}
	}

	@Override
	public void process(ResponseBuilder rb) throws IOException {
		if (!rb.req.getParams().getBool(NAME, false)) {
			return;
		}

		DocListAndSet results = rb.getResults();

		CountCollector collector = new CountCollector(groupBy, this.dvType);
		rb.req.getSearcher().search(results.docSet.getTopFilter(), collector);

		rb.rsp.add(NAME, ResultUtils.writeMap(collector.getGroupByCount()));

	}

	@Override
	public int distributedProcess(ResponseBuilder rb) throws IOException {
		if (!rb.req.getParams().getBool(NAME, false)) {
			return ResponseBuilder.STAGE_DONE;
		}

		if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
			createSubRequests(rb);
		}
		return ResponseBuilder.STAGE_DONE;
	}

	@Override
	@SuppressWarnings("all")
	public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
		if (!rb.req.getParams().getBool(NAME, false)) {
			return;
		}
		if (rb.stage != ResponseBuilder.STAGE_EXECUTE_QUERY) {
			return;
		}
		// super.handleResponses(rb, sreq);
		// logger.info("handleResponses,response size:" +
		// sreq.responses.size());
		SimpleOrderedMap<String> shardResult = null;
		Map<String, AtomicInteger> mergeResult = new HashMap<>();
		AtomicInteger val = null;
		String key = null;
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

		rb.rsp.add(NAME, ResultUtils.writeMap(mergeResult));
	}

	private void createSubRequests(ResponseBuilder rb) throws IOException {
		SolrParams params = rb.req.getParams();
		CloudDescriptor cloudDescriptor = rb.req.getCore().getCoreDescriptor().getCloudDescriptor();
		ZkController zkController = rb.req.getCore().getCoreContainer().getZkController();
		String collection = cloudDescriptor.getCollectionName();

		for (Slice slice : zkController.getClusterState().getCollection(cloudDescriptor.getCollectionName())
				.getActiveSlices()) {
			String shard = slice.getName();
			ShardRequest sreq = new ShardRequest();

			sreq.purpose = 1;
			sreq.shards = sliceToShards(rb, collection, slice);
			sreq.actualShards = sreq.shards;

			SolrQuery squery = new SolrQuery();
			squery.set(ShardParams.SHARDS_QT, "/select");
			squery.set(TIS_GROUP_FIELD, this.groupBy+":"+dvType.type);
			squery.set(NAME, true);
			squery.setStart(0);
			squery.setRows(0);
			// String fields = params.get(CommonParams.FL);
			// if (fields != null) {
			// squery.set(CommonParams.FL, fields);
			// }
			// squery.set(CommonParams.DISTRIB, false);
			squery.setQuery(params.get(CommonParams.Q));
			sreq.params = squery;
			// sreq.params.set(CommonParams.DISTRIB, false);

			rb.addRequest(this, sreq);
		}
	}

	private String[] sliceToShards(ResponseBuilder rb, String collection, Slice slice) {

		Optional<Replica> replics = slice.getReplicas().stream().findFirst();
		if (!replics.isPresent()) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"Can't find shard '" + slice.getName() + "',all slice[" + rb.slices.length + "]");
		}

		return new String[] { replics.get().getCoreUrl() };
	}

	private static class CountCollector extends SimpleCollector {
		private final String groupByField;
		// private SortedDocValues groupFieldDV = null;

		private DVProcess dvProcessor;
		private Map<Order/* order */, AtomicInteger> groupByCountSegment;

		private final Map<String/* groupField */, AtomicInteger> groupByCount = Maps.newHashMap();
		private final DVType dvType;
		private final Order offset = new Order();

		public CountCollector(String groupByField, DVType dvType) {
			super();
			this.groupByField = groupByField;
			if (dvType == null) {
				throw new IllegalArgumentException("param dvType can not be null");
			}
			this.dvType = dvType;
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
			this.dvProcessor = dvType.createDVProcess(context, groupByField);
			this.groupByCountSegment = Maps.newHashMap();
		}

		private void mergeResult() throws IOException {

			if (dvProcessor != null) {
				String fieldVal = null;
				AtomicInteger groupCount = null;
				for (Map.Entry<Order, AtomicInteger> entry : groupByCountSegment.entrySet()) {

					fieldVal = this.dvProcessor.getValByOrder(entry.getKey().val);

					groupCount = groupByCount.get(fieldVal);
					if (groupCount == null) {
						groupCount = new AtomicInteger();
						groupByCount.put(fieldVal, groupCount);
					}
					groupCount.addAndGet(entry.getValue().get());
				}
				this.dvProcessor = null;
			}
		}

		@Override
		public void collect(int doc) throws IOException {

			Order order = offset.setVal(dvProcessor.ordValue(doc));
			AtomicInteger count = groupByCountSegment.get(order);
			if (count == null) {
				count = new AtomicInteger();
				groupByCountSegment.put(order.copy(), count);
			}
			// 自增一下
			count.incrementAndGet();

		}
	}

	private static class Order {
		private long val;

		private Order setVal(long order) {
			this.val = order;
			return this;
		}

		public Order copy() {
			return (new Order()).setVal(this.val);
		}

		@Override
		public int hashCode() {
			return Long.hashCode(val);
		}

		@Override
		public boolean equals(Object obj) {
			return obj.hashCode() == this.val;
		}
	}

	@Override
	public String getDescription() {
		return NAME;
	}

	public enum DVType {
		Numeric("numeric", (context, fname) -> {
			final NumericDocValues numericGroupDV = org.apache.lucene.index.DocValues.getNumeric(context.reader(),
					fname);

			return new DVProcess() {
				public long ordValue(int docId) throws IOException {
					numericGroupDV.advance(docId);
					return numericGroupDV.longValue();
				}

				public String getValByOrder(long ord) throws IOException {
					return String.valueOf(ord);
				}
			};
		}), Str("string", (context, fname) -> {

			final SortedDocValues groupFieldDV = org.apache.lucene.index.DocValues.getSorted(context.reader(), fname);

			return new DVProcess() {
				public long ordValue(int docId) throws IOException {
					groupFieldDV.advance(docId);
					return groupFieldDV.ordValue();
				}

				public String getValByOrder(long ord) throws IOException {
					return groupFieldDV.lookupOrd((int) ord).utf8ToString();
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
			if (Numeric.type.equals(type)) {
				return Numeric;
			} else if (Str.type.equals(type)) {
				return Str;
			}

			throw new IllegalStateException("type:" + type + " is illegal");
		}
	}

	private interface DVProcess {
		long ordValue(int docId) throws IOException;

		String getValByOrder(long ord) throws IOException;
	}

	private interface DVProcessCreator {
		public DVProcess createDVProcess(LeafReaderContext context, String fieldName) throws IOException;
	}

}

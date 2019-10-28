package com.qlangtech.tis.solrextend.queryparse.s4message;

import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;

/**
 * 通过反查业务系统通过key找到对应list列表
 * 
 * @author 百岁
 *
 * @date 2019年10月27日
 */
public class CachedTermsQParserPlugin extends QParserPlugin {

	private static final Logger logger = LoggerFactory.getLogger(CachedTermsQParserPlugin.class);

	private static final int CACHE_LIMIT = 1000;

	private static final Cache<String/* key */, TermInSetQueryContext> //
	queryCache = CacheBuilder.newBuilder() //
			.maximumSize(CACHE_LIMIT)//
			.expireAfterWrite(3, TimeUnit.HOURS)//
			.build();

	@Override
	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {

		String key = StringUtils.trimToNull(qstr);
		if (StringUtils.isBlank(key)) {
			throw new IllegalStateException("param 'qstr' can not be null");
		}
		final Integer version = localParams.getInt(QueryParsing.V);
		if (version == null) {
			throw new IllegalStateException("param 'v' can not be null");
		}
		TermInSetQueryContext termsQuery = queryCache.getIfPresent(key);
		if (termsQuery == null || (version > termsQuery.version)) {

			if (termsQuery != null) {
				queryCache.invalidate(key);
			}
			long startTime = System.currentTimeMillis();
			try {
				String fname = localParams.get(QueryParsing.F);
				final FieldType ft = req.getSchema().getFieldType(fname);

				termsQuery = queryCache.get(key, () -> {
					String[] splitVals = getVals(key, version);
					BytesRef[] bytesRefs = new BytesRef[splitVals.length];
					BytesRefBuilder term = new BytesRefBuilder();
					for (int i = 0; i < splitVals.length; i++) {
						String stringVal = splitVals[i];
						ft.readableToIndexed(stringVal, term);
						bytesRefs[i] = term.toBytesRef();
					}

					logger.info("create queryCache relevant key:{} vals size:{},version:{},consume:{}ms", key,
							splitVals.length, version, (System.currentTimeMillis() - startTime));

					return new TermInSetQueryContext(new TermInSetQuery(fname, bytesRefs) {
						@Override
						public int hashCode() {
							return super.hashCode() + version;
						}
					}, version);
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		final TermInSetQuery finalQuery = termsQuery.query;

		return new QParser(qstr, localParams, params, req) {
			@Override
			public Query parse() throws SyntaxError {
				return finalQuery;
			}
		};

	}

	public static class TermInSetQueryContext {
		private final TermInSetQuery query;
		private final long version;

		public TermInSetQueryContext(TermInSetQuery query, long version) {
			super();
			this.query = query;
			this.version = version;
		}

	}

	private static final MessageFormat FORMAT_URL_GET_IDS //
			= new MessageFormat("http://business.aim:10901/abroadIntelligence/queryCachedTerms?key={0}&v={1}");

	public static void main(String[] args) {
		System.out.println(FORMAT_URL_GET_IDS.format(new Object[] { "key", "lastver" }));
	}

	private String[] getVals(String key, int version) throws Exception {

		URL url = new URL(FORMAT_URL_GET_IDS.format(new Object[] { key, version }));
		return HttpUtils.processContent(url, new StreamProcess<String[]>() {
			@Override
			public String[] p(int status, InputStream stream, String md5) {
				JSONTokener tokener = new JSONTokener(stream);
				JSONObject json = new JSONObject(tokener);
				json.getInt("version");
				JSONArray ids = json.getJSONArray("values");
				String[] idsResult = new String[ids.length()];
				for (int i = 0; i < ids.length(); i++) {
					idsResult[i] = ids.getString(i);
				}
				return idsResult;
			}
		});
		// 返回 json:
		// {"version":2, values:["111111", "22222", "333333"]}
		//
		// 如果版本不变，则values为空列表：
		// {"version":1, values:[]}

	}

}

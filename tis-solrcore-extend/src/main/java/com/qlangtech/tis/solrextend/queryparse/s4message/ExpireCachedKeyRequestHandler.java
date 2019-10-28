//package com.qlangtech.tis.solrextend.queryparse.s4message;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.solr.handler.RequestHandlerBase;
//import org.apache.solr.request.SolrQueryRequest;
//import org.apache.solr.response.SolrQueryResponse;
//
///**
// * @author 百岁
// *
// * @date 2019年10月27日
// */
//public class ExpireCachedKeyRequestHandler extends RequestHandlerBase {
//
//	private static final String NAME = "expire_cached_key_handler";
//
//	@Override
//	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
//		String key = req.getParams().get("key");
//		if (StringUtils.isBlank(key)) {
//			throw new IllegalStateException("param key can not be null");
//		}
//		CachedTermsQParserPlugin.informExpire(key);
//	}
//
//	@Override
//	public String getDescription() {
//		return NAME;
//	}
//
//}

/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.solrj.extend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.DocRouter;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkCoreNodeProps;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisUpdateRequest extends UpdateRequest {

    private static final long serialVersionUID = 1L;

    public Map<String, LBHttpSolrClient.Req> getRoutes(DocRouter router, DocCollection col, Map<String, List<String>> urlMap, ModifiableSolrParams params, String idFieldd) {
        Map<SolrInputDocument, Map<String, Object>> documents = getDocumentsMap();
        Map<String, Map<String, Object>> deleteById = getDeleteByIdMap();
        if ((documents == null || documents.size() == 0) && (deleteById == null || deleteById.size() == 0)) {
            return null;
        }
        String routeValue = null;
        Map<String, LBHttpSolrClient.Req> routes = new HashMap<>();
        if (documents != null) {
            Set<Entry<SolrInputDocument, Map<String, Object>>> entries = documents.entrySet();
            for (Entry<SolrInputDocument, Map<String, Object>> entry : entries) {
                SolrInputDocument doc = entry.getKey();
                routeValue = getRouterValue(col, doc);
                Slice slice = router.getTargetSlice(null, doc, routeValue, null, col);
                if (slice == null) {
                    throw new IllegalStateException("can not find target share group,routeValue:" + routeValue);
                }
                // List<String> urls = urlMap.get(slice.getName());
                String leaderUrl = getLeaderUrl(slice);
                LBHttpSolrClient.Req request = (LBHttpSolrClient.Req) routes.get(leaderUrl);
                if (request == null) {
                    UpdateRequest updateRequest = new UpdateRequest();
                    updateRequest.setMethod(getMethod());
                    updateRequest.setCommitWithin(getCommitWithin());
                    updateRequest.setParams(params);
                    updateRequest.setPath(getPath());
                    request = new LBHttpSolrClient.Req(updateRequest, Arrays.asList(leaderUrl));
                    routes.put(leaderUrl, request);
                }
                UpdateRequest urequest = (UpdateRequest) request.getRequest();
                Map<String, Object> value = entry.getValue();
                Boolean ow = null;
                if (value != null) {
                    ow = (Boolean) value.get(OVERWRITE);
                }
                if (ow != null) {
                    urequest.add(doc, ow);
                } else {
                    urequest.add(doc);
                }
            }
        }
        // Route the deleteById's
        String routerFieldName = null;
        String routerKey = null;
        if (deleteById != null) {
            Iterator<Map.Entry<String, Map<String, Object>>> entries = deleteById.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Map<String, Object>> entry = entries.next();
                String deleteId = entry.getKey();
                Map<String, Object> map = entry.getValue();
                Long version = null;
                if (map != null) {
                    version = (Long) map.get(VER);
                }
                routerFieldName = getRouterFieldName(col);
                routerKey = "router" + routerFieldName;
                if (map.get(routerKey) == null) {
                    throw new IllegalStateException("have not set router value");
                }
                routeValue = String.valueOf(map.get(routerKey));
                Slice slice = router.getTargetSlice(null, null, routeValue, null, col);
                if (slice == null) {
                    throw new IllegalStateException("can not find target share group,routeValue:" + routeValue);
                }
                // List<String> urls = urlMap.get(slice.getName());
                // String leaderUrl = urls.get(0);
                String leaderUrl = getLeaderUrl(slice);
                LBHttpSolrClient.Req request = routes.get(leaderUrl);
                if (request != null) {
                    UpdateRequest urequest = (UpdateRequest) request.getRequest();
                    urequest.deleteById(deleteId, version);
                } else {
                    UpdateRequest urequest = new UpdateRequest();
                    urequest.setParams(params);
                    urequest.deleteById(deleteId, version);
                    request = new LBHttpSolrClient.Req(urequest, Arrays.asList(leaderUrl));
                    routes.put(leaderUrl, request);
                }
            }
        }
        return routes;
    }

    /**
     * @param slice
     * @return
     */
    private String getLeaderUrl(Slice slice) {
        Replica replic = slice.getLeader();
        return ZkCoreNodeProps.getCoreUrl(replic.getStr("base_url"), replic.getStr("core"));
    // return leaderUrl;
    }

    /**
     * 百岁add
     *
     * @param col
     * @param doc
     * @return
     */
    private String getRouterValue(DocCollection col, SolrInputDocument doc) {
        // String routeValue;
        String routeField = getRouterFieldName(col);
        Object routerValue = doc.getFieldValue(routeField);
        if (routerValue == null) {
            throw new IllegalStateException("please set router field:" + routeField + " in inputDoc:" + doc);
        }
        return String.valueOf(routerValue);
    // return routeValue;
    }

    /**
     * @param col
     * @return
     */
    @SuppressWarnings("all")
    private String getRouterFieldName(DocCollection col) {
        Map routeMap = (Map) col.get("router");
        if (routeMap == null) {
            throw new IllegalStateException("prop router can not be null in doc Collection:" + col.getName());
        }
        String routeField = (String) routeMap.get("field");
        if (routeField == null) {
            throw new IllegalStateException("prop routeField can not be null in router prop :" + col.getName());
        }
        return routeField;
    }
}

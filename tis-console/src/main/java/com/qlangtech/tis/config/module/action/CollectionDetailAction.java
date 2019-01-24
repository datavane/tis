/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.config.module.action;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.common.utils.KeyPair;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Slice;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CollectionDetailAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    // http://127.0.0.1:8080/config/config.ajax?action=collection_detail_action&event_submit_do_get_shard_index=y&runtime=daily&collection=search4totalpay&resulthandler=advance_query_result
    public void doGetShardIndex(Context context) throws Exception {
        RunEnvironment runtime = RunEnvironment.getEnum(this.getRequest().getParameter("runtime"));
        RunEnvironment.setSysRuntime(runtime);
        List<KeyPair> result = new ArrayList<KeyPair>();
        String collection = this.getRequest().getParameter("collection");
        DocCollection docCollection = this.getZkStateReader().getClusterState().getCollection(collection);
        Map<String, Slice> slicesMap = docCollection.getSlicesMap();
        for (String name : slicesMap.keySet()) {
            result.add(new KeyPair(name, slicesMap.get(name).getRange().toString()));
        }
        this.setBizObjResult(context, result);
    }

    private IResourceParametersDAO resourceParametersDAO;

    public IResourceParametersDAO getResourceParametersDAO() {
        return resourceParametersDAO;
    }

    @Autowired
    public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
        this.resourceParametersDAO = resourceParametersDAO;
    }
}

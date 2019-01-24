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
package com.qlangtech.tis.coredefine.module.screen;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.util.SimpleOrderedMap;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.spring.aop.Func;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Corenodemanage extends CoreDefineScreen {

    private static final Log log = LogFactory.getLog(Corenodemanage.class);

    private static final long serialVersionUID = 1L;

    @Override
    @Func(PermissionConstant.APP_CORE_MANAGE_VIEW)
    public void execute(Context context) throws Exception {
        this.enableChangeDomain(context);
        if (!isIndexExist()) {
            this.forward("coredefine_step1");
            return;
        }
    }

    public static final XMLResponseParser RESPONSE_PARSER = new XMLResponseParser();

    public InstanceDirDesc getInstanceDirDesc() throws Exception {
        InstanceDirDesc dirDesc = new InstanceDirDesc();
        DocCollection collection = this.getIndex();
        final Set<String> instanceDir = new HashSet<String>();
        for (Slice slice : collection.getSlices()) {
            for (final Replica replica : slice.getReplicas()) {
                URL url = new URL(replica.getCoreUrl() + "admin/mbeans?stats=true&cat=CORE&key=core");
                // http://10.1.7.41:8983/solr/search4totalpay_shard1_replica1/admin/mbeans?cat=QUERYHANDLER&key=/select&key=/update&stats=true
                // http://120.55.195.132:8080/solr/search4totalpay_shard1_replica2/admin/mbeans?stats=true&cat=QUERYHANDLER&cat=CORE&key=/select&key=/update&key=searcher&key=core
                ConfigFileContext.processContent(url, new StreamProcess<Object>() {

                    @Override
                    @SuppressWarnings("all")
                    public Object p(int s, InputStream stream, String md5) {
                        SimpleOrderedMap result = (SimpleOrderedMap) RESPONSE_PARSER.processResponse(stream, "utf8");
                        final SimpleOrderedMap mbeans = (SimpleOrderedMap) result.get("solr-mbeans");
                        SimpleOrderedMap core = (SimpleOrderedMap) ((SimpleOrderedMap) mbeans.get("CORE")).get("core");
                        SimpleOrderedMap status = ((SimpleOrderedMap) core.get("stats"));
                        instanceDir.add(StringUtils.substringAfterLast((String) status.get("indexDir"), "/"));
                        return null;
                    }
                });
            }
        }
        dirDesc.setValid(false);
        final StringBuffer replicDirDesc = new StringBuffer();
        if (instanceDir.size() > 1) {
            replicDirDesc.append("(");
            int count = 0;
            for (String d : instanceDir) {
                replicDirDesc.append(d);
                if (++count < instanceDir.size()) {
                    replicDirDesc.append(",");
                }
            }
            replicDirDesc.append(")");
            dirDesc.setDesc("副本目录不一致，分别为:" + replicDirDesc);
        } else if (instanceDir.size() == 1) {
            dirDesc.setValid(true);
            for (String d : instanceDir) {
                dirDesc.setDesc("所有副本目录为:" + d);
                break;
            }
        } else {
            dirDesc.setDesc("副本目录数异常,size:" + instanceDir.size());
        }
        return dirDesc;
    }

    public static class InstanceDirDesc {

        private String desc;

        private boolean valid;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }

    /**
     * “SolrCore属性” 显示应用相关的属性
     *
     * @return
     */
    public boolean isShowServerRelateProp() {
        return false;
    }
}

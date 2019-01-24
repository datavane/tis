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
package com.qlangtech.tis;

import static org.apache.solr.common.cloud.ZkStateReader.BASE_URL_PROP;
import static org.apache.solr.common.cloud.ZkStateReader.CORE_NAME_PROP;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.hdfs.client.bean.HdfsRealTimeTerminatorBean;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestIndexBackflow extends TestCase {

    int phrase = 0;

    private static final Logger log = LoggerFactory.getLogger(TestIndexBackflow.class);

    public void testBackflow() throws Exception {
        HdfsRealTimeTerminatorBean dumpBean = (HdfsRealTimeTerminatorBean) TestOrderinfoDump.context.getBean("search4totalpay");
        ZkStateReader zkStateReader = dumpBean.getDumpContext().getServiceConfig().getZkStateReader();
        DocCollection collection = zkStateReader.getClusterState().getCollection("search4totalpay");
        long timestamp = 20151014210509l;
        String username = "baisui";
        int taskid = 1233 + (int) (Math.random() * 10000);
        Replica leader = null;
        BackflowResult backflowResult = null;
        // String requestId = null;
        for (Slice slice : collection.getSlices()) {
            leader = slice.getLeader();
            backflowResult = triggerIndexBackflow(leader, timestamp, username, taskid);
            if (!backflowResult.isSuccess()) {
                return;
            }
            log.info(leader.getStr(CORE_NAME_PROP) + " index backflow success,node name:" + leader.getNodeName());
            for (Replica replica : slice.getReplicas()) {
                if (leader == replica) {
                    continue;
                }
                backflowResult = triggerIndexBackflow(replica, timestamp, username, taskid);
                if (!backflowResult.isSuccess()) {
                    return;
                }
                log.info(replica.getStr(CORE_NAME_PROP) + " index backflow success,node name:" + replica.getNodeName());
            }
        }
    }

    public static String RUNNING = "running";

    public static String COMPLETED = "completed";

    public static String FAILED = "failed";

    public static String NOT_FOUND = "notfound";

    private BackflowResult triggerIndexBackflow(final Replica replica, long timestamp, String userName, final int taskid) throws Exception {
        final String requestId = taskid + "_p_" + (phrase++);
        log.info("start " + replica.getStr(CORE_NAME_PROP) + " index back");
        URL url = new URL(replica.getStr(BASE_URL_PROP) + "/admin/cores?action=CREATEALIAS&execaction=swapindexfile&core=" + replica.getStr(CORE_NAME_PROP) + "&property.hdfs_timestamp=" + timestamp + "&property.hdfs_user=" + userName + "&" + CommonAdminParams.ASYNC + "=" + requestId);
        BackflowResult result = HttpUtils.processContent(url, new StreamProcess<BackflowResult>() {

            @Override
            public BackflowResult p(int status, InputStream stream, String md5) {
                BackflowResult result = new BackflowResult();
                try {
                    URL url = new URL(replica.getStr(BASE_URL_PROP) + "/admin/cores?action=requeststatus&wt=json&" + CoreAdminParams.REQUESTID + "=" + requestId);
                    return getCallbackResult(replica, url);
                } catch (MalformedURLException e) {
                }
                return result;
            }
        });
        return result;
    }

    private BackflowResult getCallbackResult(Replica replica, URL url) {
        int applyCount = 0;
        BackflowResult callbackResult = null;
        while (applyCount++ < 100) {
            callbackResult = HttpUtils.processContent(url, new StreamProcess<BackflowResult>() {

                @Override
                public BackflowResult p(int status, InputStream stream, String md5) {
                    BackflowResult callbackResult = null;
                    try {
                        callbackResult = (BackflowResult) JSON.parseObject(IOUtils.toString(stream), BackflowResult.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return callbackResult;
                }
            });
            if (callbackResult.isFaild()) {
                log.error(replica.getStr(CORE_NAME_PROP) + " index back faild:" + callbackResult);
                return callbackResult;
            }
            if (!callbackResult.isSuccess()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                log.info("waitting for " + replica.getStr(CORE_NAME_PROP) + " index call back ,retry count:" + applyCount);
                continue;
            }
            return callbackResult;
        }
        return callbackResult;
    }

    public static class BackflowResult {

        private boolean result;

        private String msg;

        private String STATUS;

        private String trace;

        /**
         * 是否失败了
         *
         * @return
         */
        public boolean isFaild() {
            if (FAILED.equalsIgnoreCase(STATUS) || NOT_FOUND.equalsIgnoreCase(STATUS)) {
                return true;
            }
            return false;
        }

        public String getTrace() {
            return trace;
        }

        public void setTrace(String trace) {
            this.trace = trace;
        }

        /**
         * 是否成功了
         *
         * @return
         */
        public boolean isSuccess() {
            return COMPLETED.equalsIgnoreCase(STATUS);
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getSTATUS() {
            return STATUS;
        }

        public void setSTATUS(String sTATUS) {
            STATUS = sTATUS;
        }

        @Override
        public String toString() {
            return "status:" + getSTATUS() + ",msg:" + getMsg();
        }
    }

    public static void main(String[] args) throws Exception {
        TestIndexBackflow backflow = new TestIndexBackflow();
        BackflowResult result = backflow.getCallbackResult(null, new URL("http://10.1.7.42:8983/solr/admin/cores?action=requeststatus&requestid=123&wt=json"));
        System.out.println(result.getMsg() + "  " + result.getSTATUS() + result.isFaild() + " " + result.isSuccess());
    }
}

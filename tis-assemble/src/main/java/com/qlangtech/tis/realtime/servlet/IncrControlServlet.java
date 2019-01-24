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
package com.qlangtech.tis.realtime.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.qlangtech.tis.realtime.utils.IncrControlJobType;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;
import com.qlangtech.tis.realtime.yarn.rpc.impl.IncrStatusUmbilicalProtocolImpl;

/*
 * 控制增量node启停等操作
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IncrControlServlet extends javax.servlet.http.HttpServlet {

    /**
     */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(IncrControlServlet.class);

    private IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtocol;

    public IncrControlServlet() {
    // this.incrStatusUmbilicalProtocol = incrStatusUmbilicalProtocol;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        IndexSwapTaskflowLauncher launcherConext = IndexSwapTaskflowLauncher.getIndexSwapTaskflowLauncher(config.getServletContext());
        this.incrStatusUmbilicalProtocol = launcherConext.getIncrStatusUmbilicalProtocol();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String collection = req.getParameter("collection");
        if (StringUtils.isBlank(collection)) {
            throw new ServletException("param collection:" + collection + " can not be null");
        }
        final String jobTpe = req.getParameter("action");
        if (IncrControlJobType.ACTION_JobRunning.equals(jobTpe)) {
            boolean stop = Boolean.parseBoolean(req.getParameter("stop"));
            if (stop) {
                incrStatusUmbilicalProtocol.pauseConsume(collection);
            } else {
                incrStatusUmbilicalProtocol.resumeConsume(collection);
            }
            logger.info("collection:" + collection + " stop:" + stop);
            wirteXml2Client(resp, true, new JSONSetter() {

                @Override
                public void set(JSONObject j) {
                    j.put("msg", "success execute");
                }
            });
        } else if (IncrControlJobType.ACTION_is_launching.equals(jobTpe)) {
            // 是否启动中
            wirteXml2Client(resp, true, /* success */
            new JSONSetter() {

                @Override
                public void set(JSONObject j) {
                    j.put("launching", incrStatusUmbilicalProtocol.isIncrGoingOn(collection));
                }
            });
        } else if (IncrControlJobType.ACTION_getTopicTags.equals(jobTpe)) {
            // 取得增量任务的topic tags
            final TopicInfo topicInfo = incrStatusUmbilicalProtocol.getFocusTopicInfo(collection);
            wirteXml2Client(resp, true, /* success */
            new JSONSetter() {

                @Override
                public void set(JSONObject j) {
                    JSONObject topics = new JSONObject();
                    JSONArray tags = null;
                    for (Map.Entry<String, Set<String>> /* tags */
                    e : topicInfo.getTopicWithTags().entrySet()) {
                        tags = new JSONArray();
                        for (String tag : e.getValue()) {
                            tags.put(tag);
                        }
                        topics.put(e.getKey(), tags);
                    }
                    j.put("topics", topics);
                }
            });
        } else if (IncrControlJobType.ACTION_Collection_TopicTags_status.equals(jobTpe)) {
            // curl -d"collection=search4totalpay&action=collection_topic_tags_status" http://localhost:8080/incr-control?collection=search4totalpay
            final Map<String, Long> /* absolute count */
            tagCountMap = this.incrStatusUmbilicalProtocol.getUpdateAbsoluteCountMap(collection);
            wirteXml2Client(resp, true, /* success */
            new JSONSetter() {

                @Override
                public void set(JSONObject j) {
                    JSONArray tagsCount = new JSONArray();
                    JSONObject o = null;
                    for (Map.Entry<String, Long> entry : tagCountMap.entrySet()) {
                        o = new JSONObject();
                        o.put("name", entry.getKey());
                        o.put("val", entry.getValue());
                        tagsCount.put(o);
                    }
                    j.put("tags", tagsCount);
                }
            });
        } else {
            throw new ServletException("action:" + req.getParameter("action") + " is not illegal");
        }
    }

    protected void wirteXml2Client(HttpServletResponse response, boolean success, JSONSetter resultSetter) throws ServletException {
        try {
            response.setContentType("text/json");
            JSONObject json = new JSONObject();
            json.put("success", success);
            resultSetter.set(json);
            response.getWriter().write(json.toString(1));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public interface JSONSetter {

        public void set(JSONObject j);
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.web.start.TisSubModule;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/31
 */
public class TriggerBuildResult {
    public static final String TRIGGER_FULL_BUILD_COLLECTION_PATH = "/trigger";
    public static final String KEY_APPNAME = IFullBuildContext.KEY_APP_NAME;// "appname";
    private static final String bizKey = "biz";
    public boolean success;

    public int taskid;

    public TriggerBuildResult() {
    }

    public TriggerBuildResult(boolean success) {
        this.success = success;
    }

    public static TriggerBuildResult triggerBuild(IControlMsgHandler module, final Context context,
                                                  List<HttpUtils.PostParam> appendParams) throws MalformedURLException {
        return triggerBuild(module, context, ConfigFileContext.HTTPMethod.POST, appendParams, Collections.emptyList());
    }

    public static String getAssembleNodeAddress(ITISCoordinator coordinator) {
        // 增量状态收集节点
        final String incrStateCollectAddress = ZkUtils.getFirstChildValue(coordinator,
                ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH, true);
        return "http://" + StringUtils.substringBefore(incrStateCollectAddress, ":") + ":" + (TisSubModule.TIS_ASSEMBLE.getLaunchPort()) + TisSubModule.TIS_ASSEMBLE.servletContext;
    }

    public static TriggerBuildResult triggerBuild(IControlMsgHandler module, final Context context,
                                                  ConfigFileContext.HTTPMethod httpMethod,
                                                  List<HttpUtils.PostParam> appendParams,
                                                  List<ConfigFileContext.Header> headers) throws MalformedURLException {
        final String assembleNodeAddress = getAssembleNodeAddress(ITISCoordinator.create());

        TriggerBuildResult triggerResult =
                HttpUtils.process(new URL(assembleNodeAddress + TRIGGER_FULL_BUILD_COLLECTION_PATH), appendParams,
                        new PostFormStreamProcess<TriggerBuildResult>(headers) {

            //            @Override
            //            public List<ConfigFileContext.Header> getHeaders() {
            //                List<ConfigFileContext.Header> hds = Lists.newArrayList(super.getHeaders());
            //                hds.addAll(headers);
            //                return hds;
            //            }

            @Override
            public ContentType getContentType() {
                return ContentType.Application_x_www_form_urlencoded;
            }

            @Override
            public TriggerBuildResult p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                TriggerBuildResult triggerResult = null;
                try {
                    JSONTokener token = new JSONTokener(stream);
                    JSONObject result = new JSONObject(token);
                    final String successKey = "success";
                    if (result.isNull(successKey)) {
                        return new TriggerBuildResult(false);
                    }
                    triggerResult = new TriggerBuildResult(true);
                    if (!result.isNull(bizKey)) {
                        JSONObject o = result.getJSONObject(bizKey);
                        if (!o.isNull(JobCommon.KEY_TASK_ID)) {
                            triggerResult.taskid = Integer.parseInt(o.getString(JobCommon.KEY_TASK_ID));
                        }
                        module.setBizResult(context, o);
                    }
                    if (result.getBoolean(successKey)) {
                        return triggerResult;
                    }
                    module.addErrorMessage(context, result.getString("msg"));
                    return new TriggerBuildResult(false);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, httpMethod);
        return triggerResult;
    }

    public int getTaskid() {
        return taskid;
    }
}

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
package com.qlangtech.tis.manage.common.valve;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.manage.common.RunContextGetter;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.BasicModule.Rundata;

/*
 * 校验当前应用是否选择了appdomain
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppDomainSelectedCheckValve extends MethodFilterInterceptor {

    private static final long serialVersionUID = -6852248426374953157L;

    private static final Map<String, Collection<RunEnvironment>> include_urls = new HashMap<String, Collection<RunEnvironment>>();

    private static final Collection<RunEnvironment> NULL_RUNTIME = Collections.emptyList();

    static {
        // 以下罗列中表示该应用不支持的环境
        include_urls.put("/runtime/index_query", NULL_RUNTIME);
        include_urls.put("/runtime/hsf_monitor", NULL_RUNTIME);
        include_urls.put("/runtime/jarcontent/snapshotset", NULL_RUNTIME);
        // include_urls.add("/changedomain");
        // include_urls.put("/queryresponse",
        // Arrays.asList(RunEnvironment.DAILY,
        // RunEnvironment.READY));
        include_urls.put("/realtimelog", NULL_RUNTIME);
        include_urls.put("/runtime/cluster_state", NULL_RUNTIME);
        // include_urls.put("/zklockview", Arrays.asList(RunEnvironment.DAILY));
        include_urls.put("/runtime/zklockview", NULL_RUNTIME);
        include_urls.put("/runtime/jarcontent/snapshotlist", NULL_RUNTIME);
        // include_urls.put("/publishZookeeperWrapper", Arrays.asList(
        // http://l.admin.taobao.org/runtime/hdfs_view.htm
        include_urls.put("/runtime/hdfs_view", NULL_RUNTIME);
        // include_urls.put("/launchdumpandbuildindex", Arrays.asList(
        include_urls.put("/runtime/jarcontent/grouplist", NULL_RUNTIME);
        include_urls.put("/runtime/launchdumpandbuildindex", NULL_RUNTIME);
        // http://l.admin.taobao.org/trigger/triggermonitor.htm
        include_urls.put("/runtime/triggermonitor", NULL_RUNTIME);
        // include_urls.put("/hdfsuserlist",
        // RunEnvironment.READY));
        include_urls.put("/runtime/schema_manage", NULL_RUNTIME);
        include_urls.put("/runtime/server_config_view", NULL_RUNTIME);
        // ▼▼▼coredefine
        include_urls.put("/coredefine/coredefine", NULL_RUNTIME);
        include_urls.put("/coredefine/coredefine_step1", NULL_RUNTIME);
        include_urls.put("/coredefine/corenodemanage", NULL_RUNTIME);
        include_urls.put("/trigger/app_list", NULL_RUNTIME);
        include_urls.put("/trigger/buildindexmonitor", NULL_RUNTIME);
        include_urls.put("/coredefine/cluster_servers_view", NULL_RUNTIME);
        // ▲▲▲
        include_urls.put("/trigger/task_list", NULL_RUNTIME);
        include_urls.put("/runtime/app_trigger_view", NULL_RUNTIME);
    }

    private Map<String, String> specialForward = new HashMap<String, String>();

    public void setSpecialForward(Map<String, String> specialForward) {
        this.specialForward = specialForward;
    }

    private RunContextGetter daoContextGetter;

    @Autowired
    public final void setRunContextGetter(RunContextGetter daoContextGetter) {
        this.daoContextGetter = daoContextGetter;
    }

    @Override
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        Boolean tagInvocation = (Boolean) ServletActionContext.getRequest().getAttribute(StrutsStatics.STRUTS_ACTION_TAG_INVOCATION);
        if (tagInvocation != null && tagInvocation) {
            return invocation.invoke();
        }
        // return invocation.invoke();
        // }
        // 
        // 
        // 
        // @Override
        // public void invoke(PipelineContext context) throws Exception {
        // TurbineRunDataInternal navigate = getRunData();
        final ActionProxy proxy = invocation.getProxy();
        if ("control".equals(StringUtils.split(proxy.getNamespace(), "#")[1])) {
            return invocation.invoke();
        }
        final Rundata rundata = BasicModule.getRundataInstance();
        AppDomainInfo domain = CheckAppDomainExistValve.getAppDomain(daoContextGetter.get());
        final String actionTarget = getActionTarget(proxy);
        if (!include_urls.containsKey(actionTarget)) {
            // 不在校验范围之内
            return invocation.invoke();
        }
        final String specialTarget = specialForward.get(actionTarget);
        boolean sensitiveRuntime = true;
        try {
            if (StringUtils.isNotBlank(specialTarget)) {
                sensitiveRuntime = "true".equalsIgnoreCase(StringUtils.substringAfter(specialTarget, ","));
            }
        } catch (Throwable e) {
        }
        if (isInvalidDomain((BasicModule) proxy.getAction(), domain, sensitiveRuntime)) {
            if (StringUtils.isNotBlank(specialTarget)) {
                rundata.forwardTo(StringUtils.substringBefore(specialTarget, ","));
            } else {
                rundata.forwardTo("appdomainhasnotselected");
                return BasicModule.key_FORWARD;
            }
            // context.breakPipeline(0);
            return invocation.invoke();
        // return;
        }
        Collection<RunEnvironment> runtime = include_urls.get(actionTarget);
        if (runtime.contains(domain.getRunEnvironment())) {
            // 跳转到该应用是不能被使用的，不支持 该环境的应用
            rundata.forwardTo("environmentunuseable");
        }
        return invocation.invoke();
    }

    private String getActionTarget(ActionProxy proxy) {
        // ActionProxy proxy = invocation.getProxy();
        return StringUtils.split(proxy.getNamespace(), "#")[0] + "/" + proxy.getActionName();
    }

    protected boolean isInvalidDomain(BasicModule basicAction, AppDomainInfo domain, boolean sensitiveRuntime) {
        if (!basicAction.isAppNameAware()) {
            return (domain instanceof Nullable);
        }
        return (domain instanceof Nullable) || domain instanceof AppDomainInfo.EnvironmentAppDomainInfo;
    }
}

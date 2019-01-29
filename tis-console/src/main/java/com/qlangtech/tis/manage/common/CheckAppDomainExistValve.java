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
package com.qlangtech.tis.manage.common;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.DefaultFilter.AppAndRuntime;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
 * 校验用户当前 应用是否选择了
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CheckAppDomainExistValve {

    // private static final Pattern p = Pattern
    // .compile("bizid(\\d+)appid(\\d+)run(\\d+)");
    // app.getProjectName()
    // + "_run" + form.getRunEnviron()
    @Autowired
    private HttpServletRequest request;

    // @Autowired
    // private URIBrokerService uriService;
    // 
    // public static final String CHANGE_DOMAIN_TARGET = "/changedomain";
    // 
    // public void invoke(PipelineContext pipelineContext) throws Exception {
    // 
    // AppDomainInfo appDomain = getAppDomain((RunContext) null);
    // 
    // if (appDomain == null) {
    // TurbineRunData rundata = TurbineUtil.getTurbineRunData(request);
    // 
    // if (!StringUtils.equalsIgnoreCase(rundata.getTarget(),
    // CHANGE_DOMAIN_TARGET)) {
    // String jumpTo = BasicModule.getBroker(uriService).setTarget(
    // CHANGE_DOMAIN_TARGET).toString();
    // rundata.setRedirectLocation(jumpTo
    // + "?_fm.ch._0.g="
    // + URLEncoder.encode(String.valueOf(request
    // .getRequestURL()), BasicModule.getEncode()));
    // pipelineContext.breakPipeline(Pipeline.TOP_LABEL);
    // }
    // 
    // }
    // 
    // pipelineContext.invokeNext();
    // }
    // public static AppDomainInfo getAppDomain(RunContext context) {
    // return getAppDomain(context.getApplicationDAO());
    // }
    public static AppDomainInfo getAppDomain(RunContext context) {
        HttpServletRequest request = ServletActionContext.getRequest();
        return getAppDomain(request, context);
    }

    public static AppDomainInfo getAppDomain(HttpServletRequest request, RunContext context) {
   
        AppDomainInfo domain = (AppDomainInfo) request.getAttribute(ActionTool.REQUEST_DOMAIN_KEY);
        if (domain != null) {
            return domain;
        }

        AppDomainInfo appDomain = null;
        
        AppAndRuntime environment = DefaultFilter.getAppAndRuntime();
        if (environment == null) {
            domain = AppDomainInfo.createAppNotAware(RunEnvironment.getSysRuntime());
            request.setAttribute(ActionTool.REQUEST_DOMAIN_KEY, domain);
            return domain;
        }
        try {
            if (StringUtils.isEmpty(environment.getAppName())) {
                // 只选择了环境 参数
                appDomain = AppDomainInfo.createAppNotAware(environment.getRuntime());
            } else {
                appDomain = queryApplication(request, context, environment.getAppName(), environment.getRuntime());
            }
        } catch (Exception e) {
            return new NullAppDomainInfo(context.getApplicationDAO());
        }
        if (appDomain == null) {
            appDomain = CheckAppDomainExistValve.createNull();
        }
        request.setAttribute(ActionTool.REQUEST_DOMAIN_KEY, appDomain);
        return appDomain;
    }

    public static AppDomainInfo queryApplication(HttpServletRequest request, RunContext context, final String appname, RunEnvironment runtime) {
        if (true) {
            ApplicationCriteria query = new ApplicationCriteria();
            query.createCriteria().andProjectNameEqualTo(appname);
            AppDomainInfo appDomain = null;
            for (Application app : context.getApplicationDAO().selectByExample(query)) {
                appDomain = new AppDomainInfo(app.getDptId(), app.getAppId(), /* appid */
                runtime, // getRuntime(match),
                context);
                appDomain.setAppName(appname);
                break;
            }
            return appDomain;
        }
        AppDomainInfo appDomain = null;
        // ApplicationCriteria criteria = new ApplicationCriteria();
        // criteria.createCriteria().andProjectNameEqualTo(
        // // match.group(1)
        // appname);// .andNotDelete();
        IAppsFetcher appFetcher = UserUtils.getAppsFetcher(request, context);
        List<Application> applist = appFetcher.getApps(new CriteriaSetter() {

            @Override
            public void set(Criteria criteria) {
                criteria.andProjectNameEqualTo(appname);
            }
        });
        for (Application app : applist) {
            // 如果应用的部门为空则说明不是一个合法的部门
            if (app.getDptId() == null || app.getDptId() < 1) {
                return CheckAppDomainExistValve.createNull();
            }
            appDomain = new AppDomainInfo(app.getDptId(), app.getAppId(), runtime, // getRuntime(match),
            context);
           
            break;
        }
        return appDomain;
    }

    private static final AppDomainInfo NULL = new NullAppDomainInfo(null);

    public static AppDomainInfo createNull() {
        return NULL;
    }

    private static class NullAppDomainInfo extends AppDomainInfo implements Nullable {

        private NullAppDomainInfo(IApplicationDAO applicationDAO) {
            super(Integer.MAX_VALUE, Integer.MAX_VALUE, RunEnvironment.DAILY.getId().intValue(), applicationDAO);
        }

        @Override
        protected void judgeNull(IApplicationDAO applicationDao) {
        }

        @Override
        public Integer getAppid() {
            return 0;
        }

        @Override
        public String getAppName() {
            return StringUtils.EMPTY;
        }

        @Override
        public Integer getDptid() {
            return -1;
        }

        @Override
        public RunEnvironment getRunEnvironment() {
            // throw new UnsupportedOperationException();
            return RunEnvironment.DAILY;
        }
    }
}

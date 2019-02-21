package com.qlangtech.tis.manage.spring.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.manage.biz.dal.dao.IOperationLogDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLog;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContextGetter;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * 执行操作拦截器
 * 
 * @author 百岁（baisui@2dfire.com）
 *
 * @date 2019年2月18日
 */
public class OperationLogInterceptor extends MethodFilterInterceptor {

	private static final long serialVersionUID = 1L;
	private RunContextGetter daoContextGetter;
	private IOperationLogDAO operationLogDAO;
	private static final Logger logger = LoggerFactory.getLogger(OperationLogInterceptor.class);

	@SuppressWarnings("all")
	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {

		BasicModule action = (BasicModule) invocation.getAction();

		Boolean tagInvocation = (Boolean) ServletActionContext.getRequest()
				.getAttribute(StrutsStatics.STRUTS_ACTION_TAG_INVOCATION);
		if (tagInvocation != null && tagInvocation) {
			return invocation.invoke();
		}
		ActionProxy proxy = invocation.getProxy();
		String namespace = proxy.getNamespace();
		final Method method = action.getExecuteMethod();

		OperationIgnore optIgnore = method.getAnnotation(OperationIgnore.class);

		if (!StringUtils.startsWith(method.getName(), "do") || StringUtils.startsWith(method.getName(), "doGet")
				|| StringUtils.startsWith(method.getName(), "doLoad") || optIgnore != null) {
			return invocation.invoke();
		}

		if (StringUtils.startsWith(namespace, "/config")) {
			return invocation.invoke();
		}

		try {
			return invocation.invoke();
		} finally {
			try {
				// 判断是否有错误发生
				List<String> errs = (List<String>) ActionContext.getContext().get(BasicModule.ACTION_ERROR_MSG);
				if (errs == null || errs.size() < 1) {
					// 说明执行成功了
					final IUser user = UserUtils.getUser(ServletActionContext.getRequest(), daoContextGetter.get());
					AppDomainInfo app = action.getAppDomain();

					OperationLog operationLog = new OperationLog();
					operationLog.setAppName(app.getAppName());
					operationLog.setOpType(method.getName());
					operationLog.setTabName(proxy.getActionName());
					operationLog.setRuntime(RunEnvironment.getSysEnvironment().getId());
					operationLog.setUsrId(user.getId());
					operationLog.setUsrName(user.getName());
					operationLog.setCreateTime(new Date());
					operationLog.setOpDesc(getRequestDesc());
					operationLogDAO.insertSelective(operationLog);
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private String getRequestDesc() {
		HttpServletRequest request = ServletActionContext.getRequest();
		StringBuffer desc = new StringBuffer();
		Enumeration<String> paramEnum = request.getParameterNames();
		String key = null;
		while (paramEnum.hasMoreElements()) {
			key = paramEnum.nextElement();
			if ("action".equals(key) || StringUtils.startsWith(key, "event_submit_do")) {
				continue;
			}
			desc.append(key).append("=").append( //
					StringUtils.substring(request.getParameter(key), 0, 200)).append("\n");
		}
		return desc.toString();
	}

	@Autowired
	public void setOperationLogDAO(IOperationLogDAO operationLogDAO) {
		this.operationLogDAO = operationLogDAO;
	}

	@Autowired
	public final void setRunContextGetter(RunContextGetter daoContextGetter) {
		this.daoContextGetter = daoContextGetter;
	}

}

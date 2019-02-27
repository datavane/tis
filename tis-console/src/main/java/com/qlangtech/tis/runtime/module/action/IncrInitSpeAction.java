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
package com.qlangtech.tis.runtime.module.action;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Maps;
import com.openshift.internal.restclient.model.build.GitBuildSource;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NotFoundException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.resources.IBuildCancelable;
import com.openshift.restclient.capability.resources.IBuildTriggerable;
import com.openshift.restclient.capability.resources.IDeploymentTriggerable;
import com.openshift.restclient.capability.resources.IPodLogRetrievalAsync;
import com.openshift.restclient.capability.resources.IPodLogRetrievalAsync.IPodLogListener;
import com.openshift.restclient.capability.resources.IPodLogRetrievalAsync.Options;
import com.openshift.restclient.capability.server.ITemplateProcessing;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IBuildConfig;
import com.openshift.restclient.model.IContainer;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IReplicationController;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.build.IBuildStatus;
import com.openshift.restclient.model.template.IParameter;
import com.openshift.restclient.model.template.ITemplate;
import com.qlangtech.tis.manage.servlet.LogFeedbackServlet;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.IncrUtils.BuildStatus;
import com.qlangtech.tis.runtime.module.action.IncrUtils.DeploymentStatus;
import com.qlangtech.tis.runtime.module.action.IncrUtils.IncrSpec;
import com.qlangtech.tis.runtime.module.action.IncrUtils.Specification;
import com.qlangtech.tis.runtime.module.action.IncrUtils.Status;
import com.qlangtech.tis.runtime.module.screen.IncrInitSpec;

/*
 * TIS管理增量任务
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IncrInitSpeAction extends BasicModule implements OCConstant {

	private static final long serialVersionUID = 1L;

	private IClient ocClient;

	private static final Logger logger = LoggerFactory.getLogger(IncrInitSpeAction.class);

	public static boolean isFinished(IBuild build) {
		return COMPLETE.equals(build.getStatus()) || FAILED.equals(build.getStatus())
				|| CANCELLED.equals(build.getStatus());
	}

	/**
	 * 取得当前部署状态
	 *
	 * @param context
	 */
	public void doGetDeploymentStatus(Context context) {
		final String collection = this.getCollectionName();
		DeploymentStatus deploymentStatus = IncrUtils.readLastDeploymentRecord(collection);
		if (deploymentStatus.getStatus() != Status.CREATED) {
			this.setBizResult(context, deploymentStatus);
			return;
		}
		// 由构建触发
		try {
			IDeploymentConfig deploy = ocClient.get(ResourceKind.DEPLOYMENT_CONFIG, collection, NAME_SPACE);
			if (deploy.getLatestVersionNumber() > deploymentStatus.getPreVersion()) {
				IReplicationController rc = ocClient.get(ResourceKind.REPLICATION_CONTROLLER,
						collection + "-" + deploy.getLatestVersionNumber(), NAME_SPACE);
				Status s = LogFeedbackServlet.getDeployStatus(rc);
				if (s != deploymentStatus.getStatus()) {
					deploymentStatus = IncrUtils.appendDeploymentRecord(collection, s, deploy.getLatestVersionNumber(),
							rc.getCurrentReplicaCount(), true, deploymentStatus.getVersion());
				}
			}
		} catch (NotFoundException e) {
		}
		// }
		this.setBizResult(context, deploymentStatus);
	}

	/**
	 * 重新部署容器
	 *
	 * @param context
	 * @throws Exception
	 */
	public void doReDeployIncr(Context context) throws Exception {
		final String collection = this.getCollectionName();
		IDeploymentConfig deploy = ocClient.get(ResourceKind.DEPLOYMENT_CONFIG, collection, NAME_SPACE);
		// IDeploymentTriggerable
		triggerIncrDeployment(context, collection, deploy);
	}

	private void triggerIncrDeployment(Context context, final String collection, IDeploymentConfig deploy) {
		deploy.accept(new CapabilityVisitor<IDeploymentTriggerable, Object>() {

			public Object visit(IDeploymentTriggerable capability) {
				int preVersion = deploy.getLatestVersionNumber();
				capability.setForce(true);
				IDeploymentConfig d = capability.trigger();
				DeploymentStatus s = IncrUtils.appendDeploymentRecord(collection, Status.CREATED,
						d.getLatestVersionNumber(), -1, true, preVersion);
				setBizResult(context, s);
				addActionMessage(context, "已触发部署:" + s.getReplicationControllerName());
				return null;
			}
		}, null);
	}

	/**
	 * 执行当前取消当前构建
	 *
	 * @param context
	 * @throws Exception
	 */
	public void doCancelIncrBuild(Context context) throws Exception {
		final String collection = (this.getCollectionName());
		BuildStatus buildStatus = IncrUtils.readLastBuildRecordStatus(collection);
		if (buildStatus.getStatus() == Status.CREATED) {
			IBuild build = ocClient.get(ResourceKind.BUILD, buildStatus.getBuildName(), NAME_SPACE);
			// IBuildCancelable
			build.accept(new CapabilityVisitor<IBuildCancelable, Object>() {

				@Override
				public Object visit(IBuildCancelable capability) {
					capability.cancel();
					// 日志回滚
					IncrUtils.rollbackIncrDeploy(collection);
					addActionMessage(context, build.getName() + "构建已取消");
					return null;
				}
			}, null);
		} else if (buildStatus.getStatus() != Status.NONE) {
			this.addErrorMessage(context, buildStatus.getBuildName() + "构建已完成不能取消");
		} else {
			this.addErrorMessage(context, "当前无任务可取消");
		}
	}

	/**
	 * 执行增量构建
	 *
	 * @param context
	 */
	public void doTriggerIncrBuild(Context context) throws Exception {
		// String appName = "search4xx";
		IBuild build;
		try {
			// final String nameSpace = "tis-incr";
			String indexName = (this.getCollectionName());
			IBuildConfig buildConfig = ocClient.get(ResourceKind.BUILD_CONFIG, indexName, NAME_SPACE);
			build = buildConfig.accept(new CapabilityVisitor<IBuildTriggerable, IBuild>() {

				@Override
				public IBuild visit(IBuildTriggerable capability) {
					return capability.trigger();
				}
			}, null);
			IDeploymentConfig deploy = ocClient.get(ResourceKind.DEPLOYMENT_CONFIG, indexName, NAME_SPACE);
			IncrUtils.appendIndeterminacyDeploymentRecord(indexName, deploy.getLatestVersionNumber());
			this.setBizResult(context, createNewBuildStatus(build, indexName));
			this.addActionMessage(context, "成功触发增量构建" + build.getName());
		} catch (NotFoundException e) {
			logger.error(e.getMessage(), e);
			this.addErrorMessage(context, "请先配置索引构建实例" + e.getMessage());
		}
	}

	public static BuildStatus createNewBuildStatus(IBuild build, String indexName) {
		BuildStatus status = new BuildStatus(indexName);
		status.setBuildName(build.getName());
		status.setBuildResult(createBuildPhase(build));
		status.setStatus(Status.CREATED);
		setStatus(status, build);
		IncrUtils.appendBuildRecord(indexName, status);
		return status;
	}

	/**
	 * 取得当前构建状态
	 *
	 * @param context
	 */
	public void doGetBuildStatus(Context context) {
		final String collection = (this.getCollectionName());
		final BuildStatus status = IncrUtils.readLastBuildRecordStatus(collection);
		IncrUtils.Status s = null;
		// BuildResult buildResult = new BuildResult();
		if ((s = status.getStatus()) == Status.CREATED) {
			IBuild build = ocClient.get(ResourceKind.BUILD, status.getBuildName(), NAME_SPACE);
			setStatus(status, build);
			status.setBuildResult(createBuildPhase(build));
			if (s != status.getStatus()) {
				IncrUtils.appendBuildRecord(collection, status);
			}
		}
		this.setBizResult(context, status);
	}

	private static void setStatus(final BuildStatus status, IBuild build) {
		if (COMPLETE.equals(build.getStatus())) {
			status.setStatus(Status.SUCCESS);
		} else if (FAILED.equals(build.getStatus())) {
			status.setStatus(Status.FAILD);
		} else if (CANCELLED.equals(build.getStatus())) {
			status.setStatus(Status.CANCELLED);
		}
	}

	private static BuildResult createBuildPhase(IBuild build) {
		BuildResult buildResult = new BuildResult();
		IBuildStatus bst = build.getBuildStatus();
		buildResult.setDuration(bst.getDuration() / (long) Math.pow(10, 9));
		buildResult.setPhase(bst.getPhase());
		buildResult.setStartTime(bst.getStartTime());
		return buildResult;
	}

	public static class BuildResult {

		private String phase;

		private String startTime;

		private long duration;

		public void setPhase(String phase) {
			this.phase = phase;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}

		public String getPhase() {
			return this.phase;
		}

		public String getStartTime() {
			return this.startTime;
		}

		public long getDuration() {
			return this.duration;
		}
	}

	/**
	 * 更新增量实例配置
	 *
	 * @param context
	 * @throws Exception
	 */
	public void doUpdateIncrInstance(Context context) throws Exception {
		IncrSpecResult specResult = parseIncrSpec(context);
		if (!specResult.isSuccess()) {
			return;
		}
		final String collectionName = (this.getCollectionName());
		final IncrSpec updateSpec = specResult.spec;
		IncrSpec spec = IncrInitSpec.loadIncrSpec(collectionName, this.ocClient);
		// String collection = this.getCollectionName();
		if (spec == null) {
			throw new IllegalStateException("执行更新没有找到本地配置");
		}
		boolean anyDiff = false;
		if (updateSpec.isGitSourceDiff(spec)) {
			anyDiff = true;
			IBuildConfig bc = ocClient.get(ResourceKind.BUILD_CONFIG, collectionName, NAME_SPACE);
			GitBuildSource gitSource = bc.getBuildSource();
			gitSource.setRef(updateSpec.getGitRef());
			gitSource.setURI(updateSpec.getGitAddress());
			triggerIncrBuild(context, collectionName, ocClient.update(bc));
		}
		if (updateSpec.isSpecificationsDiff(spec)) {
			anyDiff = true;
			IDeploymentConfig deploy = ocClient.get(ResourceKind.DEPLOYMENT_CONFIG, collectionName, NAME_SPACE);
			IContainer container = deploy.getContainer(collectionName);
			container.setLimitsCPU(String.valueOf(updateSpec.getCpuLimit()));
			container.setRequestsCPU(String.valueOf(updateSpec.getCpuRequest()));
			container.setLimitsMemory(String.valueOf(updateSpec.getMemoryLimit()));
			container.setRequestsMemory(String.valueOf(updateSpec.getMemoryRequest()));
			Map<String, String> envVars = container.getEnvVars();
			this.setMemoryParams(updateSpec, envVars);
			deploy = ocClient.update(deploy);
			if (!updateSpec.isGitSourceDiff(spec)) {
				// 说明没有触发build,则可以触发deployment
				triggerIncrDeployment(context, collectionName, deploy);
			}
		}
		if (anyDiff) {
			IncrUtils.saveIncrSpec(collectionName, specResult.spec);
		} else {
			this.addErrorMessage(context, "没有任何属性更新");
		}
	}

	/**
	 * 创建增量实例
	 *
	 * @param context
	 */
	public void doCreateIncrInstance(Context context) throws Exception {
		IncrSpecResult specResult = parseIncrSpec(context);
		if (!specResult.isSuccess()) {
			return;
		}
		IncrSpec spec = specResult.spec;
		ITemplate tpl;
		try {
			tpl = (ITemplate) ocClient.get(ResourceKind.TEMPLATE, TEMPLATE_NAME, NAME_SPACE);
		} catch (NotFoundException e) {
			this.addErrorMessage(context, "Openshift中还没有准备好模板" + TEMPLATE_NAME);
			return;
		}
		final String indexName = (this.getCollectionName());
		Map<String, String> params = Maps.newHashMap();
		params.put("APP_NAME", indexName);
		params.put("GIT_URI", spec.getGitAddress());
		params.put("GIT_REF", spec.getGitRef());
		params.put("INCR_EXEC_GROUP", NAME_SPACE);
		params.put("APP_OPTIONS", this.getAppDomain().getAppName());
		params.put("CPU_CORES", String.valueOf(spec.getCpuRequest()));
		params.put("CPU_CORES_LIMIT", String.valueOf(spec.getCpuLimit()));
		params.put("MAVEN_ARGS_APPEND", "-P" + RunEnvironment.getSysEnvironment().getKeyName());
		setMemoryParams(spec, params);
		params.put("MEMORY", String.valueOf(spec.getMemoryRequest()));
		params.put("MEMORY_LIMIT", String.valueOf(spec.getMemoryLimit()));
		for (Map.Entry<String, IParameter> o : tpl.getParameters().entrySet()) {
			String v = params.get(o.getKey());
			if (v != null) {
				o.getValue().setValue(v);
			}
		}
		// final Collection<IResource> results = new ArrayList<>();
		final AtomicReference<IBuildConfig> bconfig = new AtomicReference<>();
		ocClient.accept(new CapabilityVisitor<ITemplateProcessing, Object>() {

			@Override
			public Object visit(ITemplateProcessing capability) {
				ITemplate processedTemplate = capability.process(tpl, NAME_SPACE);
				IResource newCreate = null;
				for (IResource resource : processedTemplate.getObjects()) {
					if ((newCreate = ocClient.create(resource, NAME_SPACE)) instanceof IBuildConfig) {
						// Map<String, String> params = Maps.newHashMap();
						// params.put("newbuildName", newCreate.getName());
						// setBizResult(context, params);
						bconfig.set((IBuildConfig) newCreate);
					}
					logger.info(resource.getName() + "," + resource.getKind() + " created");
				}
				return null;
			}
		}, null);
		triggerIncrBuild(context, indexName, bconfig.get());
		IncrUtils.saveIncrSpec(indexName, spec);
		this.addActionMessage(context, "增量实例添加成功");
	}

	private static final Pattern PATTERN_JVM = Pattern.compile("(-Xm[sx]\\w+)");

	/**
	 * 根据当前内存设置设置JVM启动参数
	 *
	 * @param spec
	 * @param params
	 */
	private void setMemoryParams(IncrSpec spec, Map<String, String> params) {
		final String jvmProp = params.get("JVM_PROPERTY");
		String anyleft = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(jvmProp)) {
			Matcher m = PATTERN_JVM.matcher(jvmProp);
			if (m.find()) {
				anyleft = m.replaceAll(StringUtils.EMPTY);
			}
		}
		int mlimit = (int) (((double) spec.getMemoryLimit().normalizeMemory()) * 0.75);
		int mrquest = (int) (((double) spec.getMemoryRequest().normalizeMemory()) * 0.75);
		// name: JVM_PROPERTY value: '-Xms128m -Xmx1664m'
		params.put("JVM_PROPERTY", "-Xms" + mrquest + "m -Xmx" + mlimit + "m " + anyleft);
	}

	private void triggerIncrBuild(Context context, String indexName, IBuildConfig bc) {
		if (bc != null) {
			bc.accept(new CapabilityVisitor<IBuildTriggerable, Object>() {

				@Override
				public Object visit(IBuildTriggerable capability) {
					IBuild build = capability.trigger();
					setBizResult(context, createNewBuildStatus(build, indexName));
					IncrUtils.appendIndeterminacyDeploymentRecord(indexName, -1);
					return null;
				}
			}, null);
		}
	}

	private IncrSpecResult parseIncrSpec(Context context) {
		IncrSpec spec = new IncrSpec();
		IncrSpecResult result = new IncrSpecResult(spec, context);
		result.success = false;
		String giturl = this.getString("giturl");
		if (StringUtils.isEmpty(giturl)) {
			this.addErrorMessage(context, "请填写GITURL");
			return result;
		}
		String gitref = this.getString("gitref");
		if (StringUtils.isEmpty(gitref)) {
			this.addErrorMessage(context, "请填写GIT_REF");
			return result;
		}
		spec.setGitAddress(giturl);
		spec.setGitRef(gitref);
		Specification s = null;
		String cpurequest = StringUtils.defaultIfBlank(this.getString("cuprequest"), "300");
		String cpurequestUnit = this.getString("cuprequestunit");
		if (!IncrUtils.isNumber(cpurequest)) {
			this.addErrorMessage(context, "cpurequest must be " + IncrUtils.PATTERN_NUMBER);
			return result;
		}
		if (StringUtils.isEmpty(cpurequestUnit)) {
			this.addErrorMessage(context, "请填写CPU请求单位");
			return result;
		}
		cpurequestUnit = "cores".equals(cpurequestUnit) ? StringUtils.EMPTY : cpurequestUnit;
		s = new Specification();
		s.setVal(Integer.parseInt(cpurequest));
		s.setUnit(cpurequestUnit);
		spec.setCpuRequest(s);
		// cpurequest = cpurequest + cpurequestUnit;
		String cupLimit = StringUtils.defaultIfBlank(this.getString("cuplimit"), "1");
		String cupLimitUnit = this.getString("cuplimitunit");
		if (!IncrUtils.isNumber(cupLimit)) {
			this.addErrorMessage(context, "CPU limit must be " + IncrUtils.PATTERN_NUMBER);
			return result;
		}
		if (StringUtils.isEmpty(cupLimitUnit)) {
			this.addErrorMessage(context, "请填写CPU最大请求单位");
			return result;
		}
		cupLimitUnit = "cores".equals(cupLimitUnit) ? StringUtils.EMPTY : cupLimitUnit;
		s = new Specification();
		s.setVal(Integer.parseInt(cupLimit));
		s.setUnit(cupLimitUnit);
		spec.setCpuLimit(s);
		// cupLimit = cupLimit + cupLimitUnit;
		String memoryRequest = StringUtils.defaultIfBlank(this.getString("memoryrequest"), "300");
		String memoryRequestUnit = this.getString("memoryrequestUnit");
		if (!IncrUtils.isNumber(memoryRequest)) {
			this.addErrorMessage(context, "内存格式" + IncrUtils.PATTERN_NUMBER);
			return result;
		}
		if (StringUtils.isEmpty(memoryRequestUnit)) {
			this.addErrorMessage(context, "请填写内存请求单位");
			return result;
		}
		s = new Specification();
		s.setVal(Integer.parseInt(memoryRequest));
		s.setUnit(memoryRequestUnit);
		spec.setMemoryRequest(s);
		// memoryRequest = memoryRequest + memoryRequestUnit;
		String memoryLimit = StringUtils.defaultIfBlank(this.getString("memorylimit"), "2");
		String memoryLimitUnit = this.getString("memorylimitunit");
		if (!IncrUtils.isNumber(memoryLimit)) {
			this.addErrorMessage(context, "内存上限" + IncrUtils.PATTERN_NUMBER);
			return result;
		}
		if (StringUtils.isEmpty(memoryLimitUnit)) {
			this.addErrorMessage(context, "请填写内存上限单位");
			return result;
		}
		s = new Specification();
		s.setVal(Integer.parseInt(memoryLimit));
		s.setUnit(memoryLimitUnit);
		spec.setMemoryLimit(s);
		result.success = true;
		return result;
	}

	private class IncrSpecResult {

		private final IncrSpec spec;

		private boolean success;

		private final Context context;

		public boolean isSuccess() {
			if (!success) {
				return false;
			}
			if (!validRequestAndLimit(spec.getMemoryRequest(), spec.getMemoryLimit(), r -> r.normalizeMemory(),
					"Memory")) {
				return false;
			}
			if (!validRequestAndLimit(spec.getCpuRequest(), spec.getCpuLimit(), r -> r.normalizeCPU(), "CPU")) {
				return false;
			}
			return true;
		}

		private boolean validRequestAndLimit(Specification request, Specification limit, IMetricsGetter getter,
				String resourceLiteria) {
			int r = getter.get(request);
			if (r < 1) {
				addErrorMessage(context, resourceLiteria + " Request Is inValid " + request);
				return false;
			}
			int l = getter.get(limit);
			if (l < 1) {
				addErrorMessage(context, resourceLiteria + " Limit Is InValid " + limit);
				return false;
			}
			if (r > l) {
				addErrorMessage(context,
						resourceLiteria + " Request(" + request + ") can not bigger than Limit(" + limit + ") ");
				return false;
			}
			return true;
		}

		IncrSpecResult(IncrSpec spec, Context context) {
			this.spec = spec;
			this.context = context;
		}
	}

	private interface IMetricsGetter {

		public int get(Specification s);
	}

	@Override
	public String getCollectionName() {
		return StringUtils.lowerCase(super.getCollectionName());
	}

	// protected void saveBuild2Response(Context context, IBuild build) {
	// // IBuild build = capability.trigger();
	// Map<String, String> params = Maps.newHashMap();
	// params.put("newbuildName", build.getName());
	// setBizResult(context, params);
	// }
	@Autowired
	public void setOcClient(IClient ocClient) {
		this.ocClient = ocClient;
	}

	public static void main(String[] args) throws Exception {
		Matcher m = PATTERN_JVM.matcher("-Xms20m -Xmx30m -Dxxx=xxx");
		if (m.find()) {
			System.out.println(m.group());
			System.out.println(m.replaceAll(StringUtils.EMPTY));
		}

	}

	protected static void printAsyncLog(IPod pod, final String container, int retry) {
		pod.accept(new CapabilityVisitor<IPodLogRetrievalAsync, Object>() {

			@Override
			public Object visit(IPodLogRetrievalAsync capability) {

				capability.start(new IPodLogListener() {

					@Override
					public void onClose(int arg0, String arg1) {
					}

					@Override
					public void onFailure(Throwable t) {
						System.out.println("onFailure()");
						t.printStackTrace();
						if (retry <= 200) {
							try {
								Thread.sleep(2000l);
							} catch (InterruptedException e1) {
							}
							printAsyncLog(pod, container, retry + 1);
						}
					}

					@Override
					public void onMessage(String message) {
						System.out.println("=======:" + message);
					}

					@Override
					public void onOpen() {
					}
				}, new Options().follow().container(container));
				return null;
			}
		}, null);
	}
}

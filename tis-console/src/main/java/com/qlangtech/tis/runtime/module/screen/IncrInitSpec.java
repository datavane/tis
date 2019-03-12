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
package com.qlangtech.tis.runtime.module.screen;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NotFoundException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IBuildConfig;
import com.openshift.restclient.model.IContainer;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.build.IGitBuildSource;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.runtime.module.action.IncrInitSpeAction;
import com.qlangtech.tis.runtime.module.action.IncrUtils;
import com.qlangtech.tis.runtime.module.action.IncrUtils.IncrSpec;
import com.qlangtech.tis.runtime.module.action.IncrUtils.Specification;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IncrInitSpec extends BasicScreen {

	private static final Logger logger = LoggerFactory.getLogger(IncrInitSpec.class);

	private static final long serialVersionUID = 1L;

	private IClient ocClient;

	@Override
	public void execute(Context context) throws Exception {
		this.disableNavigationBar(context);
		this.getRundata().setLayout("blank");

		if (ocClient instanceof Nullable) {

		}

	}

	private IncrSpec incrSpec;

	private Boolean createModal;

	public boolean isCreateModal() {
		if (createModal != null) {
			return createModal;
		}
		getSpec();
		return createModal;
	}

	public String getActionMethod() {
		return isCreateModal() ? "create" : "update";
	}

	public IncrSpec getSpec() {
		if (createModal == null && incrSpec == null) {
			incrSpec = loadIncrSpec(this.getCollectionName(), this.ocClient);
		}
		if (incrSpec == null) {
			createModal = true;
			IncrSpec d = new IncrSpec();
			d.setCpuRequest(Specification.parse("300m"));
			d.setCpuLimit(Specification.parse("2"));
			d.setMemoryLimit(Specification.parse("2G"));
			d.setMemoryRequest(Specification.parse("300M"));
			return d;
		} else {
			createModal = false;
		}
		return incrSpec;
	}

	public static IncrSpec loadIncrSpec(String collection, IClient ocClient) {
		IncrSpec incrSpec = null;
		incrSpec = IncrUtils.readIncrSpec(collection);
		if (incrSpec == null) {
			try {
				IDeploymentConfig deploy = ocClient.get(ResourceKind.DEPLOYMENT_CONFIG, collection,
						IncrInitSpeAction.NAME_SPACE);
				IBuildConfig bc = ocClient.get(ResourceKind.BUILD_CONFIG, collection, IncrInitSpeAction.NAME_SPACE);
				incrSpec = new IncrSpec();
				IContainer container = deploy.getContainer(collection);
				incrSpec.setCpuRequest(Specification.parse(container.getRequestsCPU()));
				incrSpec.setCpuLimit(Specification.parse(container.getLimitsCPU()));
				incrSpec.setMemoryLimit(Specification.parse(container.getLimitsMemory()));
				incrSpec.setMemoryRequest(Specification.parse(container.getRequestsMemory()));
				IGitBuildSource source = bc.getBuildSource();
				incrSpec.setGitAddress(source.getURI());
				incrSpec.setGitRef(source.getRef());
			} catch (NotFoundException e) {
				logger.warn("collection:" + collection, e);
			}
		}
		return incrSpec;
	}

	@Override
	public String getCollectionName() {
		return StringUtils.lowerCase(super.getCollectionName());
	}

	@Autowired
	public void setOcClient(IClient ocClient) {
		this.ocClient = ocClient;
	}
}

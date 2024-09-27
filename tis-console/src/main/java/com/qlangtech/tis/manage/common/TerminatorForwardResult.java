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
package com.qlangtech.tis.manage.common;

import com.opensymphony.xwork2.ActionChainResult;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;
import org.apache.struts2.result.StrutsResultSupport;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-6-21
 */
public class TerminatorForwardResult extends StrutsResultSupport {

  private static final long serialVersionUID = 1L;

  // private final TerminatorVelocityResult velocityResult;

  private final ActionChainResult chainResult;

  private final DefaultActionMapper defaultActionMapper;

  public TerminatorForwardResult() {
    super();
    // this.velocityResult = new TerminatorVelocityResult();
    this.chainResult = new ActionChainResult();
    Container container = Dispatcher.getInstance().getContainer();
    //   container.inject(this.velocityResult);
    container.inject(this.chainResult);
    this.defaultActionMapper = (DefaultActionMapper) container.getInstance(ActionMapper.class, "default_terminator");
//    this.defaultActionMapper.setAlwaysSelectFullNamespace("true");
//    this.defaultActionMapper.setAllowDynamicMethodCalls(Boolean.TRUE.toString());
  }

  //private static final Pattern COMPONENT_PATTERN = Pattern.compile("(/(runtime|trigger|coredefine|config|engineplugins)).*");

  @Override
  protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
    HttpServletRequest request = ServletActionContext.getRequest();
    final String namespace = invocation.getProxy().getNamespace();
    // ActionMapping mapping = ServletActionContext.getActionMapping();
    final BasicModule.Forward forward = (BasicModule.Forward) request.getAttribute(BasicModule.TERMINATOR_FORWARD);
    request.removeAttribute(BasicModule.TERMINATOR_FORWARD);

    if (forward == null) {
      return;
    }
    if (StringUtils.endsWith(forward.getAction(), ".vm")) {
      throw new UnsupportedOperationException(forward.getAction());
    } else {
      request.setAttribute(BasicModule.KEY_MEHTO, forward.method);
      // 直接forward到另外一個action上去
      ActionMapping forwardMapping = defaultActionMapper.getMapping(request, null);
      forwardMapping.setName(forward.getAction());
      forwardMapping.setNamespace(forward.getNamespace());
      forwardAction(invocation, forwardMapping);
    }

  }

  private void forwardAction(ActionInvocation invocation, ActionMapping forwardMapping) throws Exception {
    synchronized (this.chainResult) {

      this.chainResult.setActionName(forwardMapping.getName());
      this.chainResult.setNamespace(forwardMapping.getNamespace() + TisActionMapper.ACTION_TOKEN);
      this.chainResult.execute(invocation);
    }
  }
}

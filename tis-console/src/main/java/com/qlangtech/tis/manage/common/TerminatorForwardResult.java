/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;
import org.apache.struts2.result.StrutsResultSupport;
import com.opensymphony.xwork2.ActionChainResult;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Container;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-6-21
 */
public class TerminatorForwardResult extends StrutsResultSupport {

    private static final long serialVersionUID = 1L;

    private final TerminatorVelocityResult velocityResult;

    private final ActionChainResult chainResult;

    private final DefaultActionMapper defaultActionMapper;

    public TerminatorForwardResult() {
        super();
        this.velocityResult = new TerminatorVelocityResult();
        this.chainResult = new ActionChainResult();
        Container container = Dispatcher.getInstance().getContainer();
        container.inject(this.velocityResult);
        container.inject(this.chainResult);
        this.defaultActionMapper = (DefaultActionMapper) container.getInstance(ActionMapper.class, "default_terminator");
        this.defaultActionMapper.setAlwaysSelectFullNamespace("true");
    }

    private static final Pattern COMPONENT_PATTERN = Pattern.compile("(/(runtime|trigger|coredefine|config|engineplugins)).*");

    @Override
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        final String namespace = invocation.getProxy().getNamespace();
        // ActionMapping mapping = ServletActionContext.getActionMapping();
        final BasicModule.Forward forward = (BasicModule.Forward) request.getAttribute(BasicModule.TERMINATOR_FORWARD);
        request.removeAttribute(BasicModule.TERMINATOR_FORWARD);
        try {
            if (forward == null) {
                // throw new IllegalStateException("forward can not be null");
                ActionMapping forwardMapping = defaultActionMapper.getMapping(request, null);
                forwardAction(invocation, forwardMapping);
                return;
            }
            if (StringUtils.endsWith(forward.getAction(), ".vm")) {
                Matcher matcher = COMPONENT_PATTERN.matcher(namespace);
                String lastFinalLocation = null;
                if (matcher.matches()) {
                    lastFinalLocation = (StringUtils.isEmpty(forward.getNamespace()) ? matcher.group(1) : forward.getNamespace()) + "/templates/screen/" + forward.getAction();
                } else {
                    throw new IllegalStateException("mapping.getNamespace()" + namespace + " is not pattern" + COMPONENT_PATTERN);
                }
                // request
                // .setAttribute(BasicModule.Layout_template,
                // );
                velocityResult.setPlaceholder(invocation, lastFinalLocation);
                velocityResult.expressLayout(invocation);
            } else {
                // 直接forward到另外一個action上去
                ActionMapping forwardMapping = defaultActionMapper.getMapping(request, null);
                forwardMapping.setName(forward.getAction());
                forwardAction(invocation, forwardMapping);
            }
        } finally {
        }
    }

    private void forwardAction(ActionInvocation invocation, ActionMapping forwardMapping) throws Exception {
        synchronized (this.chainResult) {
            this.chainResult.setActionName(forwardMapping.getName());
            this.chainResult.setNamespace(forwardMapping.getNamespace() + "#screen");
            this.chainResult.execute(invocation);
        }
    }
}

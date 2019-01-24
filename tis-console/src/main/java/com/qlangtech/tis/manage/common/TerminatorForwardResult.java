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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorForwardResult extends StrutsResultSupport {

    private static final long serialVersionUID = 1L;

    private final TerminatorVelocityResult velocityResult;

    private final ActionChainResult chainResult;

    private final DefaultActionMapper defaultActionMapper;

    public TerminatorForwardResult() // @Inject("default_terminator") DefaultActionMapper defaultActionMapper
    {
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

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

import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.components.ActionComponent;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PageControl {

    public TISActionComponent setTemplate(final String path) throws Exception {
        // if (1 == 1) {
        // OutputStreamWriter writer = new OutputStreamWriter(
        // ServletActionContext.getResponse().getOutputStream());
        // 
        // writer.write("<h1>" + path + "</h1>");
        // writer.flush();
        // 
        // return null;
        // }
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        final ActionMapping mapping = ServletActionContext.getActionMapping();
        final DefaultActionMapper actionMapper = new DefaultActionMapper() {

            @Override
            public String getUriFromActionMapping(ActionMapping mapping) {
                return mapping.getNamespace() + (StringUtils.startsWith(path, "/") ? path : ('/' + path));
            }

            // @Override
            // protected String getUri(HttpServletRequest request) {
            // return mapping.getNamespace()
            // + (StringUtils.startsWith(path, "/") ? path
            // : ('/' + path));
            // }
            @Override
            protected String dropExtension(String name, ActionMapping mapping) {
                for (String ext : extensions) {
                    String extension = "." + ext;
                    if (name.endsWith(extension)) {
                        name = name.substring(0, name.length() - extension.length());
                        mapping.setExtension(StringUtils.equalsIgnoreCase(ext, "vm") ? "vm" : "action");
                        return name;
                    }
                }
                return null;
            }
        };
        actionMapper.setExtensions("action,htm,vm");
        actionMapper.setAllowDynamicMethodCalls("false");
        actionMapper.setAlwaysSelectFullNamespace("true");
        actionMapper.getMapping(request, null);
        ActionMapping mapresult = actionMapper.getMapping(request, null);
        final TISActionComponent action = new TISActionComponent(ServletActionContext.getValueStack(request), request, ServletActionContext.getResponse());
        Container container = Dispatcher.getInstance().getContainer();
        container.inject(action);
        // action.setValueStackFactory(container.getInstance(ValueStackFactory.class));
        action.setName(mapresult.getName());
        action.setNamespace(mapresult.getNamespace());
        action.setExecuteResult(true);
        action.setIgnoreContextParams(false);
        action.setFlush(true);
        action.setRethrowException(true);
        action.setActionMapper(actionMapper);
        action.end(new OutputStreamWriter(response.getOutputStream()), null);
        return action;
    }

    public static class TISActionComponent extends ActionComponent {

        public TISActionComponent(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
            super(stack, req, res);
        }

        public TISActionComponent setParameter(String name, Object value) {
            this.addParameter(name, value);
            return this;
        }
    }

    public PageControl setParameter(String name, String value) {
        return this;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}

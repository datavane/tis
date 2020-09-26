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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PageControl {

    public TisActionComponent setTemplate(final String path) throws Exception {
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
        final TisActionComponent action = new TisActionComponent(ServletActionContext.getValueStack(request), request, ServletActionContext.getResponse());
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

    public static class TisActionComponent extends ActionComponent {

        public TisActionComponent(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
            super(stack, req, res);
        }

        public TisActionComponent setParameter(String name, Object value) {
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

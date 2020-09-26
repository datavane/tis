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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.components.ActionComponent;
import org.apache.struts2.components.Component;
import org.apache.struts2.views.velocity.components.ActionDirective;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-6-20
 */
public class TerminatorActionDirective extends ActionDirective {

    public String getName() {
        return "taction";
    }

    @Override
    protected Component getBean(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
        return new TerminatorActionComponent(stack, req, res);
    }

    @Override
    public String getBeanName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType() {
        return DirectiveConstants.BLOCK;
    }

    private class TerminatorActionComponent extends ActionComponent {

        public TerminatorActionComponent(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
            super(stack, req, res);
        }
        // @Override
        // protected Map<String, String[]> createParametersForContext() {
        // 
        // Map<String, String[]> paramContext = super
        // .createParametersForContext();
        // 
        // paramContext.remove(ownservers);
        // 
        // return paramContext;
        // }
        // 
        // private static final String ownservers = "ownservers";
        // 
        // @SuppressWarnings("all")
        // @Override
        // protected Map createExtraContext() {
        // final Map extraContext = super.createExtraContext();
        // 
        // extraContext.put(ownservers, parameters.get(ownservers));
        // 
        // // if (parameters != null) {
        // // // Map<String, String[]> params = new HashMap<String,
        // // // String[]>();
        // // for (Iterator i = parameters.entrySet().iterator(); i.hasNext();)
        // // {
        // // Map.Entry entry = (Map.Entry) i.next();
        // // String key = (String) entry.getKey();
        // // Object val = entry.getValue();
        // // if (val instanceof String) {
        // // continue;
        // // }
        // //
        // // if (val.getClass().isArray()
        // // && String.class == val.getClass()
        // // .getComponentType()) {
        // // // params.put(key, (String[]) val);
        // // continue;
        // // }
        // // extraContext.put(key, val);
        // // }
        // // }
        // 
        // return extraContext;
        // }
    }
}

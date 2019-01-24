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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.components.ActionComponent;
import org.apache.struts2.components.Component;
import org.apache.struts2.views.velocity.components.ActionDirective;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import com.opensymphony.xwork2.util.ValueStack;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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

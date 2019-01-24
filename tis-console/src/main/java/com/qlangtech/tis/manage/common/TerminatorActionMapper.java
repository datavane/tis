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
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;
import com.opensymphony.xwork2.config.ConfigurationManager;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorActionMapper extends DefaultActionMapper {

    public static final String REQUEST_EXTENDSION_AJAX = "ajax";

    protected void parseNameAndNamespace(String uri, ActionMapping mapping, ConfigurationManager configManager) {
        super.parseNameAndNamespace(uri, mapping, configManager);
        // StringBuffer parsedName = new StringBuffer();
        // char[] nameAry = mapping.getName().toCharArray();
        // for (int i = 0; i < nameAry.length; i++) {
        // if (Character.isUpperCase(nameAry[i])) {
        // parsedName.append('_')
        // .append(Character.toLowerCase(nameAry[i]));
        // } else {
        // parsedName.append(nameAry[i]);
        // // .append(Character.toLowerCase());
        // }
        // }
        mapping.setName(addUnderline(mapping.getName()).toString());
        mapping.setNamespace(mapping.getNamespace() + "#screen");
    }

    public static StringBuffer addUnderline(String value) {
        StringBuffer parsedName = new StringBuffer();
        char[] nameAry = value.toCharArray();
        boolean firstAppend = true;
        for (int i = 0; i < nameAry.length; i++) {
            if (Character.isUpperCase(nameAry[i])) {
                if (firstAppend) {
                    parsedName.append(Character.toLowerCase(nameAry[i]));
                    firstAppend = false;
                } else {
                    parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
                }
            } else {
                parsedName.append(nameAry[i]);
                firstAppend = false;
            // .append(Character.toLowerCase());
            }
        }
        return parsedName;
    }

    @Override
    public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {
        ActionMapping mapping = super.getMapping(request, configManager);
        String action = null;
        if (StringUtils.isNotEmpty(action = request.getParameter("action"))) {
            mapping.setName(action);
            mapping.setNamespace(StringUtils.split(mapping.getNamespace(), "#")[0] + "#action");
        }
        return mapping;
    }
}

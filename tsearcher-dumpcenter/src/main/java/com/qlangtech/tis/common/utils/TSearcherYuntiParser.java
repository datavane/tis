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
package com.qlangtech.tis.common.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherYuntiParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // <tsearcher:yuntidump dailyPattern="" usrToken="" host="" path="" />
        // super.doParse(element, parserContext, builder);
        final String pattern = element.getAttribute("dailyPattern");
        if (StringUtils.isNotEmpty(pattern)) {
            builder.addPropertyValue("dailyDirPattern", pattern);
        } else {
            Element dateformat = DomUtils.getChildElementByTagName(element, "dateformat");
            if (dateformat != null) {
                // builder.addPropertyValue("dateformatTool", parserContext
                // .getDelegate().parseCustomElement(dateformat,
                // builder.getRawBeanDefinition()));
                parseChildEmement("dateformatTool", parserContext, builder, dateformat);
            }
        // else {
        // throw new IllegalArgumentException(
        // "neither attribute dailyDirPattern nor child element dateformat have present");
        // }
        }
        String path = element.getAttribute("path");
        if (StringUtils.isNotBlank(path)) {
            builder.addPropertyValue("yuntipath", path);
        } else {
            Element pathCreator = DomUtils.getChildElementByTagName(element, "pathCreator");
            if (pathCreator != null) {
                parseChildEmement("yuntipathCreator", parserContext, builder, pathCreator);
            } else {
                throw new IllegalArgumentException("neither attribute yuntipath nor yuntipathCreator have present");
            }
        }
        builder.addPropertyValue("usertoken", element.getAttribute("usrToken"));
        builder.addPropertyValue("yuntihost", element.getAttribute("host"));
    }

    private void parseChildEmement(String parentBeanProp, ParserContext parserContext, BeanDefinitionBuilder builder, Element childElement) {
        builder.addPropertyValue(parentBeanProp, parserContext.getDelegate().parseCustomElement(childElement, builder.getRawBeanDefinition()));
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        // return YuntiHDFSProvider.class;
        return Object.class;
    }
}

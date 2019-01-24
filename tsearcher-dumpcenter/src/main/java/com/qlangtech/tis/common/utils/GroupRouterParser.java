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
import org.w3c.dom.Element;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;

/*
 * 解析TSearcher Query标签
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GroupRouterParser extends AbstractSimpleBeanDefinitionParser {

    private static final String SHARE_KEY = "shardKey";

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // String clazz = element.getAttribute("class");
        final String shareKey = element.getAttribute(SHARE_KEY);
        if (StringUtils.isEmpty(element.getAttribute("class")) && StringUtils.isEmpty(shareKey)) {
            parserContext.getReaderContext().error("neither shareKey nor class attribute have any value", element);
            return;
        }
        if (StringUtils.isNotEmpty(shareKey)) {
            builder.addPropertyValue(SHARE_KEY, shareKey);
        }
    }

    @SuppressWarnings("all")
    @Override
    protected Class<? extends GroupRouter> getBeanClass(Element element) {
        try {
            String clazz = StringUtils.defaultIfEmpty(element.getAttribute("class"), com.qlangtech.tis.hdfs.client.router.SolrCloudPainRouter.class.getName());
            return (Class<? extends GroupRouter>) Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    // try {
    // final String clazzName = element.getAttribute("class");
    // return (Class<DataProcessor<String, String>>) Class
    // .forName(clazzName);
    // } catch (ClassNotFoundException e) {
    // throw new RuntimeException(e);
    // }
    // return RealtimeTerminatorSearcher.class;
    }
    // private void addPropertyValue(Element element,
    // BeanDefinitionBuilder builder, String name) {
    // builder.addPropertyValue(name, element.getAttribute(name));
    // }
}

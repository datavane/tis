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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/*
 * 解析TSearcher Query标签
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherDumpAndQueryParser extends AbstractSimpleBeanDefinitionParser {

    private static final String PROPERTY_incrDumpProvider = "incrDumpProvider";

    private static final String FullDumpProvider = "fullDumpProvider";

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String serviceName = TSearcherBeanParser.getServiceName(element);
        builder.addPropertyValue("serviceName", serviceName);
        // 执行dump流程的时候是否需要判断抢锁
        String triggerLock = element.getAttribute("triggerLock");
        builder.addPropertyValue("triggerLock", !"false".equalsIgnoreCase(triggerLock));
        // 表示只作dump
        boolean justDump = "true".equalsIgnoreCase(element.getAttribute("justDump"));
        builder.addPropertyValue("justDump", justDump);
        setFullDumpProvider(element, parserContext, builder);
        Element incrElement = DomUtils.getChildElementByTagName(element, "incrdump");
        if (incrElement == null) {
            builder.addPropertyValue(PROPERTY_incrDumpProvider, new MockHDFSProvider());
        } else {
            builder.addPropertyValue(PROPERTY_incrDumpProvider, parserContext.getDelegate().parseCustomElement(DomUtils.getChildElementByTagName(element, "incrdump"), builder.getRawBeanDefinition()));
        }
        Element dataprocess = DomUtils.getChildElementByTagName(element, "dataprocess");
        if (dataprocess != null) {
            builder.addPropertyValue("dataprocess", parserContext.getDelegate().parseCustomElement(dataprocess, builder.getRawBeanDefinition()));
        }
        Element groupRouter = DomUtils.getChildElementByTagName(element, "grouprouter");
        if (groupRouter != null) {
            builder.addPropertyValue("grouprouter", parserContext.getDelegate().parseCustomElement(groupRouter, builder.getRawBeanDefinition()));
        }
    }

    /**
     * @param element
     * @param parserContext
     * @param builder
     */
    private void setFullDumpProvider(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Element fulldump = DomUtils.getChildElementByTagName(element, "fulldump");
        Element yuntidump = DomUtils.getChildElementByTagName(element, "yuntidump");
        if (fulldump != null) {
            builder.addPropertyValue(FullDumpProvider, parserContext.getDelegate().parseCustomElement(fulldump, builder.getRawBeanDefinition()));
        } else if (yuntidump != null) {
            builder.addPropertyValue(FullDumpProvider, parserContext.getDelegate().parseCustomElement(yuntidump, builder.getRawBeanDefinition()));
        } else {
            builder.addPropertyValue(FullDumpProvider, new MockHDFSProvider());
        }
    }

    @Override
    protected Class<? extends CommonTerminatorBeanFactory> getBeanClass(Element element) {
        if ("tsearcher:realtime".equals(element.getNodeName())) {
            return RealtimeTerminatorBeanFactory.class;
        }
        // }
        throw new IllegalArgumentException("element.getNodeName():" + element.getNodeName() + " is illegal");
    }
}

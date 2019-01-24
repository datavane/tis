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
import com.qlangtech.tis.hdfs.client.bean.searcher.CommonTerminatorSearcher;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl;

/*
 * 解析TSearcher Query标签
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherQueryParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        try {
            TSearcherQueryContextImpl queryContext = new TSearcherQueryContextImpl();
            queryContext.setServiceName(element.getAttribute("id"));
            queryContext.setZkAddress(TSearcherConfigFetcher.get().getZkAddress());
            queryContext.afterPropertiesSet();
            Element groupRouter = DomUtils.getChildElementByTagName(element, "grouprouter");
            if (groupRouter != null) {
                builder.addPropertyValue("groupRouter", parserContext.getDelegate().parseCustomElement(groupRouter, builder.getRawBeanDefinition()));
            }
            builder.addPropertyValue("queryContext", queryContext);
        } catch (Exception e) {
            parserContext.getReaderContext().error("class " + CommonTerminatorSearcher.class.getName() + " can not be create", element, e);
        }
    }

    @Override
    protected Class<TSearcherQueryFactory> getBeanClass(Element element) {
        return TSearcherQueryFactory.class;
    // return RealtimeTerminatorSearcher.class;
    }
    // private void addPropertyValue(Element element,
    // BeanDefinitionBuilder builder, String name) {
    // builder.addPropertyValue(name, element.getAttribute(name));
    // }
}

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
import com.qlangtech.tis.hdfs.client.process.WrapperDataProcessor;

/*
 * 解析TSearcher Query标签
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DataprocessorParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String reference = element.getAttribute("ref");
        // 优先使用ref属性所对应的object
        if (StringUtils.isNotBlank(reference)) {
            builder.addConstructorArgReference(reference);
            return;
        } else {
            try {
                final String clazzName = element.getAttribute("class");
                if (StringUtils.isBlank(clazzName)) {
                    throw new IllegalArgumentException("either property className or property ref shall be set");
                }
                builder.addConstructorArgValue(Class.forName(clazzName).newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("all")
    @Override
    protected Class<WrapperDataProcessor> getBeanClass(Element element) {
        return WrapperDataProcessor.class;
    // return WrapperDataProcessor<String, String>;
    }
    // private void addPropertyValue(Element element,
    // BeanDefinitionBuilder builder, String name) {
    // builder.addPropertyValue(name, element.getAttribute(name));
    // }
}

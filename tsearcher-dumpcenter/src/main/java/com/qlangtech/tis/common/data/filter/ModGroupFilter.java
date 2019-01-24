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
package com.qlangtech.tis.common.data.filter;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * 用对sharkey对分组数取模的方式决定分组
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ModGroupFilter extends AbstractGroupFilter {

    protected static Log logger = LogFactory.getLog(ModGroupFilter.class);

    @Override
    public boolean accept(Map<String, String> rowData) {
        if (!rowData.containsKey(shardKey)) {
            logger.error("给定数据中不包含shardKey为" + shardKey + "的参数");
            throw new IllegalArgumentException("给定数据中不包含shardKey为" + shardKey + "的参数");
        }
        String value = rowData.get(shardKey);
        try {
            return Long.parseLong(value) % groupNumber == shardNumber;
        } catch (NumberFormatException nfe) {
            logger.error("指定的shardKey的值无法转换成long类型");
            throw new IllegalArgumentException("指定的shardKey的值无法转换成long类型", nfe);
        }
    }
}

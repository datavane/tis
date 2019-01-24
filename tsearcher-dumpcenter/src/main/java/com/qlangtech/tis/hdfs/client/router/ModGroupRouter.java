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
package com.qlangtech.tis.hdfs.client.router;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * @description
 * @since 2011-9-17 04:04:42
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ModGroupRouter extends AbstractGroupRouter {

    protected static Log log = LogFactory.getLog(ModGroupRouter.class);

    private ModGroupRouter() {
    }

    public String getGroupName(Map<String, String> rowData) {
        if (rowData == null) {
            return "0";
        }
        int groupNumber = serviceConfig.getGroupNum();
        if (groupNumber > 1) {
            String value = rowData.get(shardKey);
            long longVal = getValue(value);
            return String.valueOf(longVal % groupNumber);
        } else {
            return "0";
        }
    }

    private long getValue(String value) {
        if (value == null) {
            RuntimeException e = new IllegalArgumentException("Can't not found shardKey in input row data!");
            log.error("在输入的一行数据中无法找到hardKey", e);
            throw e;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("切分字段的值无法转换成Long型！", e);
            throw new IllegalArgumentException("Can't convert the shard-key value to Long.", e);
        }
    }
    /**
     * @param groupNum
     */
    // @Override
    // public void setGroupNum(int groupNum) {
    // 
    // }
}

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
package com.qlangtech.tis.hdfs.client.process;

import java.util.Map;

/*
 * 提供默认的删除Doc实现，数据源有isDeleted 标示字段，同时 主键为 id
 * @since 2011-8-25 涓嬪崍07:58:51
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultDeleteDataProcessor extends DeleteDataProcessor {

    /**
     * @return
     */
    @Override
    protected String getDelMarkColumn() {
        // 默认的标示删除列的名称
        return "isDeleted";
    }

    /**
     * @return
     */
    @Override
    protected String getDelMarkValue() {
        // TODO Auto-generated method stub
        return "1";
    }

    /**
     * @return
     */
    @Override
    protected String getUniqueValue(Map<String, String> map) {
        // TODO Auto-generated method stub
        return map.get("id");
    }
}

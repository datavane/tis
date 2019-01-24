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
package com.qlangtech.tis.hdfs.client.data.parse;

import java.util.List;
import java.util.Map;
import com.qlangtech.tis.exception.DataSourceParseException;

/*
 * @description 
 * 定义数据源描述信息解析接口<br>
 * 终搜定义默认实现@see DefaultDataSourceParser#parseDescription<br>
 * 业务方可以根据自己需求实现符合自己需求的Parser
 * @since 2011-8-3 下午01:05:43
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface DataSourceParser {

    /**
     * @param desc 数据源描述信息<br>
     *  (1)ds1 单一数据库，单表 <br>
     *  (2)ds1:1,2,3,4-9 单一数据库，多表<br>
     *  (3)ds1:1,2,3,4-9;ds2:1,3,4,5-9 多数据库，多表<br>
     * @return 返回Map结构，key 为数据源，value 对应的表后缀
     * @throws DataSourceParseException 如果输入描述信息符合规范信息，则抛出异常信息
     */
    public Map<String, List<String>> parseDescription(String desc) throws DataSourceParseException;

    /**
     * 为实现类初始化操作，一般是需要初始化DecimalFormat
     */
    public void init();

    /**
     * 表后缀格式化字符串
     */
    public void setDefaultSubTableString(String defaultSubTableString);
}

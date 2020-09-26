/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.hdfs.client.data.parse;

import java.util.List;
import java.util.Map;
import com.qlangtech.tis.exception.DataSourceParseException;

/**
 * @description 
 * 定义数据源描述信息解析接口<br>
 * 终搜定义默认实现@see DefaultDataSourceParser#parseDescription<br>
 * 业务方可以根据自己需求实现符合自己需求的Parser
 * @since 2011-8-3 下午01:05:43
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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

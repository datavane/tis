/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.hdfs.client.process;

import java.util.Map;
import com.qlangtech.tis.exception.DataImportHDFSException;

/**
 *  读源数据进行符合条件的加工处理<br>
 *  如果某些数据增量需要对数据进行删除操作,
 *  那么需要继承该接口，默认添加一个对该行进行操作的列
 *  如：该行数据 的opt 是删除，则形成(opt,d)到map中
 *  其他操作操作以此类似
 * @since 2011-8-25 下午07:08:55
 * @version 1.0
 * @param <V>
 * @param <K>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface DataProcessor<V, K> {

    public boolean process(Map<K, V> map) throws DataImportHDFSException;
}

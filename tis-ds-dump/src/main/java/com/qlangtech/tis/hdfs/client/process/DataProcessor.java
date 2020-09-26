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

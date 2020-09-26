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
package com.qlangtech.tis.hdfs.client.context;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-12
 */
public interface TSearcherQueryContext {

    public abstract EntityName getDumpTable();

    public abstract Set<String> getGroupNameSet();

    public TisZkClient getZkClient();

    /**
     * 触发serivceconfig对象 更新业务逻辑
     */
    public void fireServiceConfigChange();
}

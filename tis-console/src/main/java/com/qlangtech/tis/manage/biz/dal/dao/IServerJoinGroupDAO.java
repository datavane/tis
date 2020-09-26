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
package com.qlangtech.tis.manage.biz.dal.dao;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-4-13
 */
public interface IServerJoinGroupDAO {

    /**
     * 百岁添加20120413，在做服务器端索引查询的时候，要取得到应用下的所有应用来查询服务器端的索引
     */
    List<ServerJoinGroup> selectServerByAppAndRuntime(Integer appid, RunEnvironment runtime);
}

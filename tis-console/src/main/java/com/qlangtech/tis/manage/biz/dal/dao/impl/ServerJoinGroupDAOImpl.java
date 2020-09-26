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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IServerJoinGroupDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-4-13
 */
public class ServerJoinGroupDAOImpl extends BasicDAO<ServerJoinGroup, ServerGroup> implements IServerJoinGroupDAO {

    @Override
    public String getEntityName() {
        return "server_join_group";
    }

    // 百岁添加
    @Override
    public List<ServerJoinGroup> selectServerByAppAndRuntime(Integer appid, RunEnvironment runtime) {
        ServerGroup group = new ServerGroup();
        group.setAppId(appid);
        group.setRuntEnvironment(runtime.getId());
        return this.list("server.selectByAppAndRunt", group);
    }
}

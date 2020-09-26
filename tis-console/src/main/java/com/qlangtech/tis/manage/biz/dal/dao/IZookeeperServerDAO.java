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

import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServer;
import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServerCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IZookeeperServerDAO {

    int countByExample(ZookeeperServerCriteria example);

    int countFromWriteDB(ZookeeperServerCriteria example);

    int deleteByExample(ZookeeperServerCriteria criteria);

    int deleteByPrimaryKey(Integer zid);

    void insert(ZookeeperServer record);

    void insertSelective(ZookeeperServer record);

    List<ZookeeperServer> selectByExample(ZookeeperServerCriteria criteria);

    List<ZookeeperServer> selectByExample(ZookeeperServerCriteria example, int page, int pageSize);

    ZookeeperServer selectByPrimaryKey(Integer zid);

    int updateByExampleSelective(ZookeeperServer record, ZookeeperServerCriteria example);

    int updateByExample(ZookeeperServer record, ZookeeperServerCriteria example);

    ZookeeperServer loadFromWriteDB(Integer zid);
}

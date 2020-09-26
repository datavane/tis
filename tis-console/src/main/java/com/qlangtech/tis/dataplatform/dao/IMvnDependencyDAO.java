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
package com.qlangtech.tis.dataplatform.dao;

import com.qlangtech.tis.dataplatform.pojo.MvnDependency;
import com.qlangtech.tis.dataplatform.pojo.MvnDependencyCriteria;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IMvnDependencyDAO {

    @SuppressWarnings("all")
    public List<MvnDependency> getALLDependenciesByNobleAppId(Long nobleid, RunEnvironment runtime);

    public int countByCollectionExample(Long nobleAppId, String groupid, String artifactId, RunEnvironment runtime);

    public int countByNobleExample(Long tisAppId, String groupid, String artifactId, RunEnvironment runtime);

    int countByExample(MvnDependencyCriteria example);

    int countFromWriteDB(MvnDependencyCriteria example);

    int deleteByExample(MvnDependencyCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Long insert(MvnDependency record);

    Long insertSelective(MvnDependency record);

    List<MvnDependency> selectByExample(MvnDependencyCriteria criteria);

    List<MvnDependency> selectByExample(MvnDependencyCriteria example, int page, int pageSize);

    MvnDependency selectByPrimaryKey(Long id);

    int updateByExampleSelective(MvnDependency record, MvnDependencyCriteria example);

    int updateByExample(MvnDependency record, MvnDependencyCriteria example);

    MvnDependency loadFromWriteDB(Long id);
}

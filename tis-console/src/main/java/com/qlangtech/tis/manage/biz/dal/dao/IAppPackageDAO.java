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

import com.qlangtech.tis.manage.biz.dal.pojo.AppPackage;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackageCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IAppPackageDAO {

    int countByExample(AppPackageCriteria example);

    int countFromWriteDB(AppPackageCriteria example);

    int deleteByExample(AppPackageCriteria criteria);

    int deleteByPrimaryKey(Integer pid);

    Integer insert(AppPackage record);

    Integer insertSelective(AppPackage record);

    List<AppPackage> selectByExample(AppPackageCriteria criteria);

    List<AppPackage> selectByExample(AppPackageCriteria example, int page, int pageSize);

    AppPackage selectByPrimaryKey(Integer pid);

    int updateByExampleSelective(AppPackage record, AppPackageCriteria example);

    int updateByExample(AppPackage record, AppPackageCriteria example);

    AppPackage loadFromWriteDB(Integer pid);
}

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

import com.qlangtech.tis.manage.biz.dal.pojo.GlobalAppResource;
import com.qlangtech.tis.manage.biz.dal.pojo.GlobalAppResourceCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IGlobalAppResourceDAO {

    int countByExample(GlobalAppResourceCriteria example);

    int countFromWriteDB(GlobalAppResourceCriteria example);

    int deleteByExample(GlobalAppResourceCriteria criteria);

    int deleteByPrimaryKey(Long appResId);

    Long insert(GlobalAppResource record);

    Long insertSelective(GlobalAppResource record);

    List<GlobalAppResource> selectByExample(GlobalAppResourceCriteria criteria);

    List<GlobalAppResource> selectByExample(GlobalAppResourceCriteria example, int page, int pageSize);

    GlobalAppResource selectByPrimaryKey(Long appResId);

    int updateByExampleSelective(GlobalAppResource record, GlobalAppResourceCriteria example);

    int updateByExample(GlobalAppResource record, GlobalAppResourceCriteria example);

    GlobalAppResource loadFromWriteDB(Long appResId);
}

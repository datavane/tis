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

import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthority;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthorityCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IBizFuncAuthorityDAO {

    List<BizFuncAuthority> selectWithGroupByFuncidAppid(BizFuncAuthorityCriteria criteria);

    int countByExample(BizFuncAuthorityCriteria example);

    int countFromWriteDB(BizFuncAuthorityCriteria example);

    int deleteByExample(BizFuncAuthorityCriteria criteria);

    int deleteByPrimaryKey(Integer bfId);

    Integer insert(BizFuncAuthority record);

    Integer insertSelective(BizFuncAuthority record);

    List<BizFuncAuthority> selectByExample(BizFuncAuthorityCriteria criteria);

    /**
     * 查找用户的定时任务
     * @param criteria
     * @return
     */
    List<BizFuncAuthority> selectAppDumpJob(BizFuncAuthorityCriteria criteria);

    List<BizFuncAuthority> selectByExample(BizFuncAuthorityCriteria example, int page, int pageSize);

    BizFuncAuthority selectByPrimaryKey(Integer bfId);

    int updateByExampleSelective(BizFuncAuthority record, BizFuncAuthorityCriteria example);

    int updateByExample(BizFuncAuthority record, BizFuncAuthorityCriteria example);

    BizFuncAuthority loadFromWriteDB(Integer bfId);
}

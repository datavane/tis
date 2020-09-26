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

import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResourceCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IUploadResourceDAO {

    int countByExample(UploadResourceCriteria example);

    int countFromWriteDB(UploadResourceCriteria example);

    int deleteByExample(UploadResourceCriteria criteria);

    int deleteByPrimaryKey(Long urId);

    Integer insert(UploadResource record);

    Integer insertSelective(UploadResource record);

    List<UploadResource> selectByExampleWithBLOBs(UploadResourceCriteria example);

    List<UploadResource> selectByExample(UploadResourceCriteria criteria);

    List<UploadResource> selectByExampleWithoutBLOBs(UploadResourceCriteria example, int page, int pageSize);

    UploadResource selectByPrimaryKey(Long urId);

    int updateByExampleSelective(UploadResource record, UploadResourceCriteria example);

    int updateByExampleWithBLOBs(UploadResource record, UploadResourceCriteria example);

    int updateByExampleWithoutBLOBs(UploadResource record, UploadResourceCriteria example);

    UploadResource loadFromWriteDB(Long urId);
}

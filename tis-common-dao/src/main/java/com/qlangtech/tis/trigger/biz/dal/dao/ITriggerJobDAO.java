/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.trigger.biz.dal.dao;

import com.qlangtech.tis.trigger.biz.dal.pojo.TriggerJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.TriggerJobCriteria;

import java.util.List;

public interface ITriggerJobDAO {
    int countByExample(TriggerJobCriteria example);

    int countFromWriteDB(TriggerJobCriteria example);

    int deleteByExample(TriggerJobCriteria criteria);

    int deleteByPrimaryKey(Long jobId);

    Long insert(TriggerJob record);

    Long insertSelective(TriggerJob record);

    List<TriggerJob> selectByExample(TriggerJobCriteria criteria);

    List<TriggerJob> selectByExample(TriggerJobCriteria example, int page, int pageSize);

    TriggerJob selectByPrimaryKey(Long jobId);

    int updateByExampleSelective(TriggerJob record, TriggerJobCriteria example);

    int updateByExample(TriggerJob record, TriggerJobCriteria example);

    TriggerJob loadFromWriteDB(Long jobId);
}
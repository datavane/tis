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
package com.qlangtech.tis.manage.biz.dal.dao;

import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.TriggerCrontab;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IUsrDptRelationDAO {

  void addAdminUser();

  int countByExample(UsrDptRelationCriteria example);

  int countFromWriteDB(UsrDptRelationCriteria example);

  int deleteByExample(UsrDptRelationCriteria criteria);

  int deleteByPrimaryKey(String usrId);

  void insert(UsrDptRelation record);

  void insertSelective(UsrDptRelation record);

  List<UsrDptRelation> selectByExample(UsrDptRelationCriteria criteria);

  List<UsrDptRelation> selectByExample(UsrDptRelationCriteria example, int page, int pageSize);

  UsrDptRelation selectByPrimaryKey(String usrId);

  int updateByExampleSelective(UsrDptRelation record, UsrDptRelationCriteria example);

  int updateByExample(UsrDptRelation record, UsrDptRelationCriteria example);

  UsrDptRelation loadFromWriteDB(String usrId);

  List<TriggerCrontab> selectAppDumpJob(UsrDptRelationCriteria criteria);
}

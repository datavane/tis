/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.biz.dal.dao;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.TriggerCrontab;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IUsrDptRelationDAO {

    int countByExample(UsrDptRelationCriteria example);

    int countFromWriteDB(UsrDptRelationCriteria example);

    int deleteByExample(UsrDptRelationCriteria criteria);
   
    void addAdminUser();
    
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

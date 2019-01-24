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

import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApplyCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IApplicationApplyDAO {

    int countByExample(ApplicationApplyCriteria example);

    int countFromWriteDB(ApplicationApplyCriteria example);

    int deleteByExample(ApplicationApplyCriteria criteria);

    int deleteByPrimaryKey(Integer appId);

    Integer insert(ApplicationApply record);

    Integer insertSelective(ApplicationApply record);

    List<ApplicationApply> selectByExample(ApplicationApplyCriteria criteria);

    List<ApplicationApply> selectByExample(ApplicationApplyCriteria example, int page, int pageSize);

    ApplicationApply selectByPrimaryKey(Integer appId);

    int updateByExampleSelective(ApplicationApply record, ApplicationApplyCriteria example);

    int updateByExample(ApplicationApply record, ApplicationApplyCriteria example);

    ApplicationApply loadFromWriteDB(Integer appId);
}

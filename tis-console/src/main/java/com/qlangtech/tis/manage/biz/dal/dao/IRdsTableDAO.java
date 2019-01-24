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

import com.qlangtech.tis.manage.biz.dal.pojo.RdsTable;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsTableCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IRdsTableDAO {

    int countByExample(RdsTableCriteria example);

    int countFromWriteDB(RdsTableCriteria example);

    int deleteByExample(RdsTableCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Integer insert(RdsTable record);

    Integer insertSelective(RdsTable record);

    List<RdsTable> selectByExample(RdsTableCriteria criteria);

    List<RdsTable> selectByExample(RdsTableCriteria example, int page, int pageSize);

    RdsTable selectByPrimaryKey(Long id);

    int updateByExampleSelective(RdsTable record, RdsTableCriteria example);

    int updateByExample(RdsTable record, RdsTableCriteria example);

    RdsTable loadFromWriteDB(Long id);
}

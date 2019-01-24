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
package com.qlangtech.tis.dataplatform.dao;

import com.qlangtech.tis.dataplatform.pojo.NobelApp;
import com.qlangtech.tis.dataplatform.pojo.NobelAppCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface INobelAppDAO {

    int countByExample(NobelAppCriteria example);

    int countFromWriteDB(NobelAppCriteria example);

    int deleteByExample(NobelAppCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Long insert(NobelApp record);

    Long insertSelective(NobelApp record);

    List<NobelApp> selectByExample(NobelAppCriteria criteria);

    List<NobelApp> selectByExample(NobelAppCriteria example, int page, int pageSize);

    NobelApp selectByPrimaryKey(Long id);

    int updateByExampleSelective(NobelApp record, NobelAppCriteria example);

    int updateByExample(NobelApp record, NobelAppCriteria example);

    NobelApp loadFromWriteDB(Long id);
}

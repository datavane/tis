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

import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface ISnapshotDAO {

    int countByExample(SnapshotCriteria example);

    int countFromWriteDB(SnapshotCriteria example);

    int deleteByExample(SnapshotCriteria criteria);

    public List<Snapshot> findPassTestSnapshot(SnapshotCriteria example);

    int deleteByPrimaryKey(Integer snId);

    Integer insert(Snapshot record);

    Integer insertSelective(Snapshot record);

    List<Snapshot> selectByExample(SnapshotCriteria criteria);

    List<Snapshot> selectByExample(SnapshotCriteria example, int page, int pageSize);

    public Integer getMaxSnapshotId(SnapshotCriteria criteria);

    Snapshot selectByPrimaryKey(Integer snId);

    int updateByExampleSelective(Snapshot record, SnapshotCriteria example);

    int updateByExample(Snapshot record, SnapshotCriteria example);

    Snapshot loadFromWriteDB(Integer snId);
}

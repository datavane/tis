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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.Date;
import java.util.List;
import com.qlangtech.tis.dataplatform.dao.IClusterSnapshotDAO;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshot;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MockClusterSnapshotDAO implements IClusterSnapshotDAO {

    @Override
    public void insertList(List<ClusterSnapshot> records) {
    }

    @Override
    public void createTodaySummary(Date today) {
    }

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return 0;
    }

    @Override
    public Integer insert(ClusterSnapshot record) {
        return null;
    }

    @Override
    public Integer insertSelective(ClusterSnapshot record) {
        return null;
    }

    @Override
    public ClusterSnapshot selectByPrimaryKey(Integer id) {
        return null;
    }

    @Override
    public ClusterSnapshot loadFromWriteDB(Integer id) {
        return null;
    }

    @Override
    public int countByExample(ClusterSnapshotCriteria example) {
        return 0;
    }

    @Override
    public int countFromWriteDB(ClusterSnapshotCriteria example) {
        return 0;
    }

    @Override
    public int deleteByExample(ClusterSnapshotCriteria criteria) {
        return 0;
    }

    @Override
    public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria criteria) {
        return null;
    }

    @Override
    public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria example, int page, int pageSize) {
        return null;
    }

    @Override
    public int updateByExample(ClusterSnapshot record, ClusterSnapshotCriteria example) {
        return 0;
    }

    @Override
    public int updateByExampleSelective(ClusterSnapshot record, ClusterSnapshotCriteria example) {
        return 0;
    }
}

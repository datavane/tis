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

import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServer;
import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServerCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IZookeeperServerDAO {

    int countByExample(ZookeeperServerCriteria example);

    int countFromWriteDB(ZookeeperServerCriteria example);

    int deleteByExample(ZookeeperServerCriteria criteria);

    int deleteByPrimaryKey(Integer zid);

    void insert(ZookeeperServer record);

    void insertSelective(ZookeeperServer record);

    List<ZookeeperServer> selectByExample(ZookeeperServerCriteria criteria);

    List<ZookeeperServer> selectByExample(ZookeeperServerCriteria example, int page, int pageSize);

    ZookeeperServer selectByPrimaryKey(Integer zid);

    int updateByExampleSelective(ZookeeperServer record, ZookeeperServerCriteria example);

    int updateByExample(ZookeeperServer record, ZookeeperServerCriteria example);

    ZookeeperServer loadFromWriteDB(Integer zid);
}

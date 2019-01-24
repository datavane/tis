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

import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IServerGroupDAO {

    int countByExample(ServerGroupCriteria example);

    int countFromWriteDB(ServerGroupCriteria example);

    int deleteByExample(ServerGroupCriteria criteria);

    int deleteByPrimaryKey(Integer gid);

    int insert(ServerGroup record);

    Integer insertSelective(ServerGroup record);

    List<ServerGroup> selectByExample(ServerGroupCriteria criteria);

    List<ServerGroup> selectByExample(ServerGroupCriteria example, int page, int pageSize);

    ServerGroup selectByPrimaryKey(Integer gid);

    int updateByExampleSelective(ServerGroup record, ServerGroupCriteria example);

    int updateByExample(ServerGroup record, ServerGroupCriteria example);

    ServerGroup loadFromWriteDB(Integer gid);

    // 百岁添加
    public ServerGroup load(String appName, Short groupIndex, Short runtime);
    // end
}

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

import com.qlangtech.tis.dataplatform.pojo.MvnDependency;
import com.qlangtech.tis.dataplatform.pojo.MvnDependencyCriteria;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IMvnDependencyDAO {

    @SuppressWarnings("all")
    public List<MvnDependency> getALLDependenciesByNobleAppId(Long nobleid, RunEnvironment runtime);

    public int countByCollectionExample(Long nobleAppId, String groupid, String artifactId, RunEnvironment runtime);

    public int countByNobleExample(Long tisAppId, String groupid, String artifactId, RunEnvironment runtime);

    int countByExample(MvnDependencyCriteria example);

    int countFromWriteDB(MvnDependencyCriteria example);

    int deleteByExample(MvnDependencyCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Long insert(MvnDependency record);

    Long insertSelective(MvnDependency record);

    List<MvnDependency> selectByExample(MvnDependencyCriteria criteria);

    List<MvnDependency> selectByExample(MvnDependencyCriteria example, int page, int pageSize);

    MvnDependency selectByPrimaryKey(Long id);

    int updateByExampleSelective(MvnDependency record, MvnDependencyCriteria example);

    int updateByExample(MvnDependency record, MvnDependencyCriteria example);

    MvnDependency loadFromWriteDB(Long id);
}

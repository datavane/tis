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
package com.qlangtech.tis.dataplatform.dao.impl;

import java.util.List;
import com.qlangtech.tis.dataplatform.dao.IMvnDependencyDAO;
import com.qlangtech.tis.dataplatform.pojo.MvnDependency;
import com.qlangtech.tis.dataplatform.pojo.MvnDependencyCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MvnDependencyDAOImpl extends BasicDAO<MvnDependency, MvnDependencyCriteria> implements IMvnDependencyDAO {

    @Override
    public String getEntityName() {
        return "mvn_dependency";
    }

    public MvnDependencyDAOImpl() {
        super();
    }

    @SuppressWarnings("all")
    public List<MvnDependency> getALLDependenciesByNobleAppId(Long nobleid, RunEnvironment runtime) {
        MvnDependency query = new MvnDependency();
        query.setNobelAppId(nobleid);
        query.setRuntime(runtime.getKeyName());
        List<MvnDependency> list = this.getSqlMapClientTemplate().queryForList("mvn_dependency.ibatorgenerated_get_aLL_dependency_ByNobleAppId", query);
        return list;
    }

    /**
     * @param appid
     * @param groupid
     * @param artifactId
     * @param version
     * @return
     */
    public int countByCollectionExample(Long nobleAppId, String groupid, String artifactId, RunEnvironment runtime) {
        MvnDependency key = new MvnDependency();
        key.setGroupId(groupid);
        key.setArtifactId(artifactId);
        key.setNobelAppId(nobleAppId);
        key.setRuntime(runtime.getKeyName());
        return (Integer) this.getSqlMapClientTemplate().queryForObject("mvn_dependency.ibatorgenerated_countByCollectionExample", key);
    }

    /**
     * 统计tis关键的noble应用中已经添加的mvn依赖个数
     *
     * @param tisAppId
     * @param groupid
     * @param artifactId
     * @param runtime
     * @return
     */
    public int countByNobleExample(Long tisAppId, String groupid, String artifactId, RunEnvironment runtime) {
        MvnDependency key = new MvnDependency();
        key.setGroupId(groupid);
        key.setArtifactId(artifactId);
        key.setTisAppId(tisAppId);
        key.setRuntime(runtime.getKeyName());
        return (Integer) this.getSqlMapClientTemplate().queryForObject("mvn_dependency.ibatorgenerated_countByNobleExample", key);
    }

    public int countByExample(MvnDependencyCriteria example) {
        Integer count = (Integer) this.count("mvn_dependency.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(MvnDependencyCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("mvn_dependency.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(MvnDependencyCriteria criteria) {
        return this.deleteRecords("mvn_dependency.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        MvnDependency key = new MvnDependency();
        key.setId(id);
        return this.deleteRecords("mvn_dependency.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(MvnDependency record) {
        Object newKey = this.insert("mvn_dependency.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(MvnDependency record) {
        Object newKey = this.insert("mvn_dependency.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<MvnDependency> selectByExample(MvnDependencyCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<MvnDependency> selectByExample(MvnDependencyCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<MvnDependency> list = this.list("mvn_dependency.ibatorgenerated_selectByExample", example);
        return list;
    }

    public MvnDependency selectByPrimaryKey(Long id) {
        MvnDependency key = new MvnDependency();
        key.setId(id);
        MvnDependency record = (MvnDependency) this.load("mvn_dependency.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(MvnDependency record, MvnDependencyCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("mvn_dependency.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(MvnDependency record, MvnDependencyCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("mvn_dependency.ibatorgenerated_updateByExample", parms);
    }

    public MvnDependency loadFromWriteDB(Long id) {
        MvnDependency key = new MvnDependency();
        key.setId(id);
        MvnDependency record = (MvnDependency) this.loadFromWriterDB("mvn_dependency.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends MvnDependencyCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, MvnDependencyCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}

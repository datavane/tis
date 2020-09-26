/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.dataplatform.dao.impl;

import java.util.List;
import com.qlangtech.tis.dataplatform.dao.IMvnDependencyDAO;
import com.qlangtech.tis.dataplatform.pojo.MvnDependency;
import com.qlangtech.tis.dataplatform.pojo.MvnDependencyCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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

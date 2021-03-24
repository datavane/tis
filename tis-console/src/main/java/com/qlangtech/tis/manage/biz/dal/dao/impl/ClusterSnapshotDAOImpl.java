/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.biz.dal.dao.IClusterSnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshotCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshotQuery;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.common.Config;

import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ClusterSnapshotDAOImpl extends BasicDAO<ClusterSnapshot, ClusterSnapshotCriteria> implements IClusterSnapshotDAO {

  @Override
  public String getEntityName() {
    return "cluster_snapshot_dao";
  }

  public ClusterSnapshotDAOImpl() {
    super();
  }

  public int countByExample(ClusterSnapshotCriteria example) {
    Integer count = this.count("cluster_snapshot.ibatorgenerated_countByExample", example);
    return count;
  }

  public int countFromWriteDB(ClusterSnapshotCriteria example) {
    Integer count = this.countFromWriterDB("cluster_snapshot.ibatorgenerated_countByExample", example);
    return count;
  }

  public int deleteByExample(ClusterSnapshotCriteria criteria) {
    return this.deleteRecords("cluster_snapshot.ibatorgenerated_deleteByExample", criteria);
  }

  public Long insert(ClusterSnapshot record) {
    Object newKey = this.insert("cluster_snapshot.ibatorgenerated_insert", record);
    return (Long) newKey;
  }

  public Long insertSelective(ClusterSnapshot record) {
    Object newKey = this.insert("cluster_snapshot.ibatorgenerated_insertSelective", record);
    return (Long) newKey;
  }

  /**
   * @param query
   * @return
   */
  public List<ClusterSnapshot> reportClusterStatus(ClusterSnapshotQuery query) {

    if (Config.DB_TYPE_DERBY.equals(Config.getDbCfg().dbtype)) {
      return Collections.emptyList();
    }

    return this.listAnonymity("cluster_snapshot.clusterReport" + query.getSqlmapSuffix(), query);
  }

  public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria criteria) {
    return this.selectByExample(criteria, 1, 100);
  }

  @SuppressWarnings("all")
  public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria example, int page, int pageSize) {
    example.setPage(page);
    example.setPageSize(pageSize);
    List<ClusterSnapshot> list = this.list("cluster_snapshot.ibatorgenerated_selectByExample", example);
    return list;
  }

  // public ClusterSnapshot selectByPrimaryKey(Long id) {
  // ClusterSnapshot key = new ClusterSnapshot();
  // key.setId(id);
  // ClusterSnapshot record = (ClusterSnapshot) this.load("cluster_snapshot.ibatorgenerated_selectByPrimaryKey",
  // key);
  // return record;
  // }
  public int updateByExampleSelective(ClusterSnapshot record, ClusterSnapshotCriteria example) {
    UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
    return this.updateRecords("cluster_snapshot.ibatorgenerated_updateByExampleSelective", parms);
  }

  public int updateByExample(ClusterSnapshot record, ClusterSnapshotCriteria example) {
    UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
    return this.updateRecords("cluster_snapshot.ibatorgenerated_updateByExample", parms);
  }

  private static class UpdateByExampleParms extends ClusterSnapshotCriteria {

    private Object record;

    public UpdateByExampleParms(Object record, ClusterSnapshotCriteria example) {
      super(example);
      this.record = record;
    }

    public Object getRecord() {
      return record;
    }
  }
}

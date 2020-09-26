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
package com.qlangtech.tis.realtime.test.order.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.order.dao.IServicebillinfoDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Servicebillinfo;
import com.qlangtech.tis.realtime.test.order.pojo.ServicebillinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ServicebillinfoDAOImpl extends BasicDAO<Servicebillinfo, ServicebillinfoCriteria> implements IServicebillinfoDAO {

    public ServicebillinfoDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "servicebillinfo";
    }

    public int countByExample(ServicebillinfoCriteria example) {
        Integer count = (Integer) this.count("servicebillinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ServicebillinfoCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("servicebillinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ServicebillinfoCriteria criteria) {
        return this.deleteRecords("servicebillinfo.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String servicebillId) {
        Servicebillinfo key = new Servicebillinfo();
        key.setServicebillId(servicebillId);
        return this.deleteRecords("servicebillinfo.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Servicebillinfo record) {
        this.insert("servicebillinfo.ibatorgenerated_insert", record);
    }

    public void insertSelective(Servicebillinfo record) {
        this.insert("servicebillinfo.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Servicebillinfo> selectByExampleWithBLOBs(ServicebillinfoCriteria example) {
        List<Servicebillinfo> list = this.list("servicebillinfo.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Servicebillinfo> selectByExampleWithoutBLOBs(ServicebillinfoCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(ServicebillinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.ServicebillinfoCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("servicebillinfo.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Servicebillinfo> selectByExampleWithoutBLOBs(ServicebillinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Servicebillinfo> list = this.list("servicebillinfo.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Servicebillinfo selectByPrimaryKey(String servicebillId) {
        Servicebillinfo key = new Servicebillinfo();
        key.setServicebillId(servicebillId);
        Servicebillinfo record = (Servicebillinfo) this.load("servicebillinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Servicebillinfo record, ServicebillinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("servicebillinfo.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Servicebillinfo record, ServicebillinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("servicebillinfo.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Servicebillinfo record, ServicebillinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("servicebillinfo.ibatorgenerated_updateByExample", parms);
    }

    public Servicebillinfo loadFromWriteDB(String servicebillId) {
        Servicebillinfo key = new Servicebillinfo();
        key.setServicebillId(servicebillId);
        Servicebillinfo record = (Servicebillinfo) this.loadFromWriterDB("servicebillinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ServicebillinfoCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ServicebillinfoCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}

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

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IOperationLogDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLog;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLogCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class OperationLogDAOImpl extends BasicDAO<OperationLog, OperationLogCriteria> implements IOperationLogDAO {

	@Override
	public String getEntityName() {
		return "operation_log";
	}

	public OperationLogDAOImpl() {
		super();
	}

	public int countByExample(OperationLogCriteria example) {
		Integer count = (Integer) this.count("operation_log.ibatorgenerated_countByExample", example);
		return count;
	}

	public int countFromWriteDB(OperationLogCriteria example) {
		Integer count = (Integer) this.countFromWriterDB("operation_log.ibatorgenerated_countByExample", example);
		return count;
	}

	public int deleteByExample(OperationLogCriteria criteria) {
		return this.deleteRecords("operation_log.ibatorgenerated_deleteByExample", criteria);
	}

	public int deleteByPrimaryKey(Integer opId) {
		OperationLog key = new OperationLog();
		key.setOpId(opId);
		return this.deleteRecords("operation_log.ibatorgenerated_deleteByPrimaryKey", key);
	}

	public Integer insert(OperationLog record) {
		Object newKey = this.insert("operation_log.ibatorgenerated_insert", record);
		return (Integer) newKey;
	}

	public Integer insertSelective(OperationLog record) {
		Object newKey = this.insert("operation_log.ibatorgenerated_insertSelective", record);
		return (Integer) newKey;
	}

	@SuppressWarnings("unchecked")
	public List<OperationLog> selectByExampleWithBLOBs(OperationLogCriteria example) {
		List<OperationLog> list = this.list("operation_log.ibatorgenerated_selectByExampleWithBLOBs", example);
		return list;
	}

	public List<OperationLog> selectByExampleWithoutBLOBs(OperationLogCriteria criteria) {
		return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
	}

	@SuppressWarnings("unchecked")
	public List<OperationLog> selectByExampleWithoutBLOBs(OperationLogCriteria example, int page, int pageSize) {
		example.setPage(page);
		example.setPageSize(pageSize);
		List<OperationLog> list = this.list("operation_log.ibatorgenerated_selectByExample", example);
		return list;
	}

	public OperationLog selectByPrimaryKey(Integer opId) {
		OperationLog key = new OperationLog();
		key.setOpId(opId);
		OperationLog record = (OperationLog) this.load("operation_log.ibatorgenerated_selectByPrimaryKey", key);
		return record;
	}

	public int updateByExampleSelective(OperationLog record, OperationLogCriteria example) {
		UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
		return this.updateRecords("operation_log.ibatorgenerated_updateByExampleSelective", parms);
	}

	public int updateByExampleWithBLOBs(OperationLog record, OperationLogCriteria example) {
		UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
		int rows = this.updateRecords("operation_log.ibatorgenerated_updateByExampleWithBLOBs", parms);
		return rows;
	}

	public int updateByExampleWithoutBLOBs(OperationLog record, OperationLogCriteria example) {
		UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
		return this.updateRecords("operation_log.ibatorgenerated_updateByExample", parms);
	}

	public OperationLog loadFromWriteDB(Integer opId) {
		OperationLog key = new OperationLog();
		key.setOpId(opId);
		OperationLog record = (OperationLog) this.loadFromWriterDB("operation_log.ibatorgenerated_selectByPrimaryKey",
				key);
		return record;
	}

	private static class UpdateByExampleParms extends OperationLogCriteria {

		private Object record;

		public UpdateByExampleParms(Object record, OperationLogCriteria example) {
			super(example);
			this.record = record;
		}

		public Object getRecord() {
			return record;
		}
	}
}

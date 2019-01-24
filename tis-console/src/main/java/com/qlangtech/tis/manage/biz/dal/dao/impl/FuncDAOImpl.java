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
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Func;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FuncDAOImpl extends BasicDAO<Func, FuncCriteria> implements IFuncDAO {

    @Override
    public String getEntityName() {
        return "func";
    }

    public FuncDAOImpl() {
        super();
    }

    public int countByExample(FuncCriteria example) {
        Integer count = (Integer) this.count("func.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(FuncCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("func.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(FuncCriteria criteria) {
        return this.deleteRecords("func.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer funId) {
        Func key = new Func();
        key.setFunId(funId);
        return this.deleteRecords("func.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(Func record) {
        Object newKey = this.insert("func.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(Func record) {
        Object newKey = this.insert("func.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<Func> selectByExample(FuncCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Func> selectByExample(FuncCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Func> list = this.list("func.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Func selectByPrimaryKey(Integer funId) {
        Func key = new Func();
        key.setFunId(funId);
        Func record = (Func) this.load("func.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Func record, FuncCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("func.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Func record, FuncCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("func.ibatorgenerated_updateByExample", parms);
    }

    public Func loadFromWriteDB(Integer funId) {
        Func key = new Func();
        key.setFunId(funId);
        Func record = (Func) this.loadFromWriterDB("func.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends FuncCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, FuncCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}

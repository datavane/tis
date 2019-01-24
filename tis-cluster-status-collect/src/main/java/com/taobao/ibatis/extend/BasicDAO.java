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
package com.taobao.ibatis.extend;

import java.util.List;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

/*
 * @param <T>
 * @param <C>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BasicDAO<T, C> extends SqlMapClientDaoSupport {

    protected int count(String sqlmap, C param) {
        return (Integer) this.getSqlMapClientTemplate().queryForObject(sqlmap, param);
    }

    protected int countFromWriterDB(String sqlmap, C param) {
        return this.count(sqlmap, param);
    }

    protected int deleteRecords(String sqlmap, Object criteria) {
        return this.getSqlMapClientTemplate().delete(sqlmap, criteria);
    }

    @SuppressWarnings("all")
    protected Object insert(String sqlmap, T record) {
        return this.getSqlMapClientTemplate().insert(sqlmap, record);
    }

    @SuppressWarnings("all")
    protected List<T> list(String sqlmap, C criteria) {
        return (List<T>) this.getSqlMapClientTemplate().queryForList(sqlmap, criteria);
    }

    @SuppressWarnings("all")
    protected List<T> queryList(String sqlmap, Object o) {
        return (List<T>) this.getSqlMapClientTemplate().queryForList(sqlmap, o);
    }

    @SuppressWarnings("unchecked")
    protected T load(String sqlmap, T query) {
        return (T) this.getSqlMapClientTemplate().queryForObject(sqlmap, query);
    }

    protected T loadFromWriterDB(String sqlmap, T query) {
        return this.load(sqlmap, query);
    }

    protected int updateRecords(String sqlmap, C criteria) {
        return this.getSqlMapClientTemplate().update(sqlmap, criteria);
    }
}

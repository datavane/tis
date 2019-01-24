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
package com.qlangtech.tis.common.data;

import java.util.ArrayList;
import java.util.List;
import com.qlangtech.tis.common.data.sql.SqlFunction;

/*
 * 支持SqlFunction的MultiDataProvider 
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SFSupportMultiDataProvider extends MultiDataProvider implements PlusSqlFunctionRegisterable {

    protected List<SqlFunction> functionList;

    @Override
    protected void doInit() throws Exception {
        for (DataProvider dp : dataProviders) {
            if (dp instanceof PlusSqlFunctionRegisterable) {
                for (SqlFunction f : this.functionList) {
                    ((PlusSqlFunctionRegisterable) dp).registerSqlFunction(f);
                }
            }
        }
    }

    @Override
    public void unregisterAll() {
        if (this.functionList == null)
            return;
        this.functionList.clear();
    }

    @Override
    public void registerSqlFunction(SqlFunction sqlFunction) {
        if (functionList == null) {
            this.functionList = new ArrayList<SqlFunction>();
        }
        this.functionList.add(sqlFunction);
    }

    @Override
    public void unregisterSqlFunction(String name) {
    }
}

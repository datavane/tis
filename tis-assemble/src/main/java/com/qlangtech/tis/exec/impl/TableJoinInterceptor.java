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
package com.qlangtech.tis.exec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.IExecuteInterceptor;
import com.qlangtech.tis.fullbuild.taskflow.TaskConfigParser;

/*
 * 执行打宽表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TableJoinInterceptor implements IExecuteInterceptor {

    public static final String NAME = "tableJoin";

    private static final Logger log = LoggerFactory.getLogger(TableJoinInterceptor.class);

    @Override
    public ExecuteResult intercept(ActionInvocation invocation) throws Exception {
        log.info("interceptor " + NAME + " start execute");
        IExecChainContext execContext = invocation.getContext();
        String indexName = execContext.getIndexName();
        // ▼▼▼▼ 执行打宽表
        TaskConfigParser parse = TaskConfigParser.getInstance();
        try {
            parse.startJoinSubTables(indexName, execContext);
        } finally {
        }
        // ▲▲▲▲
        return invocation.invoke();
    }

    @Override
    public String getName() {
        return NAME;
    }
}

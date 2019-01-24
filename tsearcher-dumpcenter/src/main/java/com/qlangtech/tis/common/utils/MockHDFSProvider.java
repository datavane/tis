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
package com.qlangtech.tis.common.utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.qlangtech.tis.exception.DataImportHDFSException;
import com.qlangtech.tis.exception.TerminatorInitException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.data.HDFSProvider;
import com.qlangtech.tis.trigger.socket.IWorkflowFeedback;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MockHDFSProvider implements HDFSProvider<String, String> {

    @SuppressWarnings("all")
    @Override
    public void importServiceData(boolean isIncr, IWorkflowFeedback feedback, Map map, int groupNum) throws DataImportHDFSException {
    }

    @Override
    public void init() throws TerminatorInitException {
    }

    private static final AtomicBoolean notBusy = new AtomicBoolean(false);

    @Override
    public AtomicBoolean isBusy() {
        return notBusy;
    }

    @Override
    public void setDumpContext(TSearcherDumpContext dumpContext) {
    }
}

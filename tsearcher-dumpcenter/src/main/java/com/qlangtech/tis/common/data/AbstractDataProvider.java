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

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * DataProvider的抽象实现，完成了一些必要的方法
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractDataProvider implements DataProvider {

    protected Log logger = LogFactory.getLog(AbstractDataProvider.class);

    protected boolean isClosed = false;

    /**
     * @uml.property  name="isInited"
     */
    protected boolean isInited = false;

    @Override
    public void init() throws Exception {
        if (isInited) {
            return;
        }
        try {
            this.doInit();
        } finally {
            isInited = true;
            isClosed = false;
        }
    }

    @Override
    public void close() throws Exception {
        if (!isClosed) {
            try {
                this.doClose();
            } finally {
                isClosed = true;
                isInited = false;
            }
        }
    }

    /**
     * @return
     * @uml.property  name="isInited"
     */
    public boolean isInited() {
        return false;
    }

    protected abstract void doClose() throws Exception;

    protected abstract void doInit() throws Exception;

    public abstract boolean hasNext() throws Exception;

    public abstract Map<String, String> next() throws Exception;
}

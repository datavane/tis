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

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * 多个DataProvider组合运行的场景可通过这个对象进行聚合
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MultiDataProvider extends AbstractDataProvider {

    protected static Log logger = LogFactory.getLog(MultiDataProvider.class);

    /**
     * @uml.property  name="dataProviders"
     */
    protected List<DataProvider> dataProviders;

    /**
     * @uml.property  name="currentDataProviderIndex"
     */
    protected int currentDataProviderIndex = 0;

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doClose() throws Exception {
        try {
            for (DataProvider p : dataProviders) {
                p.close();
            }
        } finally {
            currentDataProviderIndex = 0;
        }
    }

    @Override
    public boolean hasNext() throws Exception {
        DataProvider currentDataProvider = this.dataProviders.get(currentDataProviderIndex);
        currentDataProvider.init();
        boolean currentSigleDataProviderHasNext = currentDataProvider.hasNext();
        if (currentSigleDataProviderHasNext) {
            return true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("当前的 TableDataProvider 的索引为 ==> [" + currentDataProviderIndex + "].");
            }
            if (currentDataProviderIndex >= (dataProviders.size() - 1)) {
                currentDataProvider.close();
                return false;
            } else {
                logger.debug("当前的TableDataProvider [" + currentDataProviderIndex + "] 的数据走到了尽头,尝试移到下一个TableDataProvider对象进行数据导出.");
                currentDataProvider.close();
                currentDataProviderIndex++;
                return this.hasNext();
            }
        }
    }

    @Override
    public Map<String, String> next() throws Exception {
        return this.dataProviders.get(currentDataProviderIndex).next();
    }

    /**
     * @return
     * @uml.property  name="dataProviders"
     */
    public List<DataProvider> getDataProviders() {
        return dataProviders;
    }

    /**
     * @param dataProviders
     * @uml.property  name="dataProviders"
     */
    public void setDataProviders(List<DataProvider> dataProviders) {
        this.dataProviders = dataProviders;
    }

    /**
     * @return
     * @uml.property  name="currentDataProviderIndex"
     */
    public int getCurrentDataProviderIndex() {
        return currentDataProviderIndex;
    }

    /**
     * @param currentDataProviderIndex
     * @uml.property  name="currentDataProviderIndex"
     */
    public void setCurrentDataProviderIndex(int currentDataProviderIndex) {
        this.currentDataProviderIndex = currentDataProviderIndex;
    }
}

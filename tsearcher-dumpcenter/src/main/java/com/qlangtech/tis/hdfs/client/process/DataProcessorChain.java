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
package com.qlangtech.tis.hdfs.client.process;

import java.util.List;
import java.util.Map;
import com.qlangtech.tis.exception.DataImportHDFSException;

/*
 * @description  直接照搬原来终搜代码
 * @since  2011-9-23 下午03:19:02
 * @version  1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DataProcessorChain implements DataProcessor<String, String> {

    private List<DataProcessor> dataProcessors = null;

    /**
     * @param map
     * @return
     */
    @Override
    public boolean process(Map<String, String> map) throws DataImportHDFSException {
        for (DataProcessor processor : dataProcessors) {
            boolean result = processor.process(map);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param dataProcessors
     * @uml.property  name="dataProcessors"
     */
    public void setDataProcessors(List<DataProcessor> dataProcessors) {
        this.dataProcessors = dataProcessors;
    }

    public void addDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessors.add(dataProcessor);
    }
}

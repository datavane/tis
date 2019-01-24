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
package com.qlangtech.tis.common.data.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * 数据处理链
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DataProcessorChain implements DataProcessor {

    /**
     * @uml.property  name="dataProcessors"
     */
    private List<DataProcessor> dataProcessors = null;

    public DataProcessorChain() {
        dataProcessors = new ArrayList<DataProcessor>();
    }

    @Override
    public ResultCode process(Map<String, String> map) throws DataProcessException {
        for (DataProcessor processor : dataProcessors) {
            ResultCode rs = processor.process(map);
            if (!rs.isSuc()) {
                return rs;
            }
        }
        return ResultCode.SUC;
    }

    public DataProcessorChain addDataProcessors(DataProcessor dataProcessor) {
        dataProcessors.add(dataProcessor);
        return this;
    }

    /**
     * @return
     * @uml.property  name="dataProcessors"
     */
    public List<DataProcessor> getDataProcessors() {
        return dataProcessors;
    }

    /**
     * @param dataProcessors
     * @uml.property  name="dataProcessors"
     */
    public void setDataProcessors(List<DataProcessor> dataProcessors) {
        this.dataProcessors = dataProcessors;
    }

    @Override
    public String getDesc() {
        String s = null;
        if (dataProcessors != null) {
            StringBuilder sb = new StringBuilder();
            int i = 1;
            for (DataProcessor dp : dataProcessors) {
                sb.append(i++).append(". ").append(dp.getDesc()).append("\n");
            }
            s = sb.toString();
        }
        return "Processor链，串联所有DataProcessor ==> \n" + (s == null ? "" : s);
    }
}

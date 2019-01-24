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
package com.qlangtech.tis.solrdao.extend;

import org.apache.commons.lang.StringUtils;

/*
 * 扩展schemaField的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ProcessorSchemaField extends BaseExtendConfig {

    // public static final Pattern PATTERN_PARMS =
    // Pattern.compile("(\\w+?)=([^\\s]+)");
    private String processorName;

    // private final Map<String, String> params = new HashMap<String, String>();
    private static final String TARGET_COLUMN = "column";

    private ProcessorSchemaField(String processorName, String processorArgs) {
        super(processorArgs);
        this.processorName = processorName;
    }

    public static ProcessorSchemaField create(String processorName, String processorArgs) {
        ProcessorSchemaField columnProcess = new ProcessorSchemaField(processorName, processorArgs);
        return columnProcess;
    }

    public String getProcessorName() {
        return processorName;
    }

    // public Map<String, String> getParams() {
    // return params;
    // }
    /**
     * 目标列是否为空，該判斷用在處理整個row處理（比如幾個列組合通過一個md5生成一個新列） 還是單個column處理的方式（生成另外一個或者多個列）
     *
     * @return
     */
    public boolean isTargetColumnEmpty() {
        return StringUtils.isBlank(getParams().get(TARGET_COLUMN));
    }

    public String getTargetColumn() {
        String columnName = getParams().get(TARGET_COLUMN);
        if (StringUtils.isBlank(columnName)) {
            throw new IllegalStateException("columnName can not be null");
        }
        return columnName;
    }
}

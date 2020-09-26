/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.solrdao.extend;

import org.apache.commons.lang.StringUtils;

/**
 * 扩展schemaField的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月5日
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

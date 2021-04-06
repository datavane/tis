/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.exec.ExecChainContextUtils;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.sql.parser.TabPartitions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年7月21日下午3:39:06
 */

public class TemplateContext implements ITemplateContext {

    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

    public <T> T getAttribute(String key) {
        return params.getAttribute(key);
    }

    public String datediff(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        return dateFormat.get().format(calendar.getTime());
    }

    public static void main(String[] args) {
        TemplateContext context = new TemplateContext(null);
        System.out.println(context.datediff(-365));
    }

    private Map<String, Object> contextValues = new HashMap<>();

    // 用户提交的参数
    private final IExecChainContext params;

    public TemplateContext(IExecChainContext paramContext) {
        this.params = paramContext;
    }

    public TabPartitions getTablePartition() {
        return ExecChainContextUtils.getDependencyTablesPartitions(params);
    }

    @Override
    public IExecChainContext getExecContext() {
        return this.params;
    }

    // public IExecChainContext getParams() {
    //    return params;
    // }

    public void putContextValue(String key, Object v) {
        this.contextValues.put(key, v);
    }

    /**
     * @param key
     * @return
     */
    @SuppressWarnings("all")
    public <T> T getContextValue(String key) {
        return (T) this.contextValues.get(key);
    }

    public String getDate() {
        if (params == null) {
            return null;
        }
        return params.getPartitionTimestamp();
    }
}

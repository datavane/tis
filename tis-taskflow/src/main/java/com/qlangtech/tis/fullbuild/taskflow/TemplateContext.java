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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.exec.IExecChainContext;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TemplateContext {

    public static final String COL_SPLIT_KEY = "colsplit";

    public static final String COL_SPLIT_TAB = "\\t";

    public static final String COL_SPLIT_001 = "\\001";

    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

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
        // this.odpsPartition = System.getProperty("odpsPartition");
        // if (StringUtils.isEmpty(this.odpsPartition)) {
        // throw new IllegalArgumentException(
        // "sys variable odpsPartition can not be null");
        // }
        this.params = paramContext;
    }

    public IExecChainContext getParams() {
        return params;
    }

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

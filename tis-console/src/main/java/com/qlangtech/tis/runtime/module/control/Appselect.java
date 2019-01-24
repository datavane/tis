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
package com.qlangtech.tis.runtime.module.control;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Appselect extends BasicModule {

    private static final long serialVersionUID = 1L;

    // 是否是从Daily中索取资源
    private boolean fromDaily = false;

    // private boolean maxMatch = false;
    public // @Param("bizid") Integer bizid,
    void execute(Context context) throws Exception {
    }

    public String getFromSymbol() {
        return fromDaily ? "app_relevant_from_daily_action" : "change_domain_action";
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }

    public boolean isFromDaily() {
        return fromDaily;
    }

    // public boolean isMaxMatch() {
    // return maxMatch;
    // }
    public void setFromDaily(boolean fromDaily) {
        this.fromDaily = fromDaily;
    }
}

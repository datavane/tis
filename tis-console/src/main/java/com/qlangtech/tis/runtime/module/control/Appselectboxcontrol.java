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

import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Appselectboxcontrol extends BasicModule {

    private static final long serialVersionUID = 1L;

    static final String key = Appselectboxcontrol.class.getName() + ".AppOptionsList";

    private String contextid;

    private boolean maxMatch = false;

    private boolean onlySelectDpt = false;

    private Integer selectDptId;

    public boolean isOnlySelectDpt() {
        return onlySelectDpt;
    }

    public void setOnlySelectDpt(boolean onlySelectDpt) {
        this.onlySelectDpt = onlySelectDpt;
    }

    public String getContextid() {
        return StringUtils.trimToEmpty(contextid);
    }

    public void setContextid(String contextid) {
        this.contextid = contextid;
    }

    public Integer getSelectDptId() {
        return selectDptId;
    }

    public void setSelectDptId(Integer selectDptId) {
        this.selectDptId = selectDptId;
    }

    public void execute(Context context) throws Exception {
        // ${contextid}
        // if (context.get("contextid") == null) {
        // context.put("contextid", StringUtils.EMPTY);
        // }
        AppOptionsList optionslist = (AppOptionsList) this.getRequest().getAttribute(key);
        if (optionslist == null) {
            final List<Option> bizlist = this.getBizLineList();
            List<Option> applist = null;
            AppDomainInfo domain = this.getAppDomain();
            if (!(domain instanceof Nullable)) {
                // if (bizid != null) {
                applist = this.getAppList(domain.getDptid());
            // }
            }
            optionslist = new AppOptionsList(bizlist, applist);
            this.getRequest().setAttribute(key, optionslist);
        }
        context.put("bizlinelist", optionslist.bizlinelist);
        context.put("applist", optionslist.applist);
    }

    private static class AppOptionsList {

        private final List<Option> bizlinelist;

        private final List<Option> applist;

        public AppOptionsList(List<Option> bizlinelist, List<Option> applist) {
            super();
            this.bizlinelist = bizlinelist;
            this.applist = applist;
        }
    }
}

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
package com.qlangtech.tis.runtime.module.control;

import java.util.List;
import com.alibaba.citrus.turbine.Context;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-11-19
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

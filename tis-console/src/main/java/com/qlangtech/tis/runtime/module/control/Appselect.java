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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-5-11
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

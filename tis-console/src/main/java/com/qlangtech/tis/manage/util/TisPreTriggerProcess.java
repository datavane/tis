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
package com.qlangtech.tis.manage.util;

import com.qlangtech.tis.pubhook.common.IPreTriggerProcess;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年11月9日 下午3:04:03
 */
public class TisPreTriggerProcess implements IPreTriggerProcess {

    // private static final AdapterHttpRequest request;
    // 
    // static {
    // request = new AdapterHttpRequest() {
    // @Override
    // public Cookie[] getCookies() {
    // int runtimeCode = ManageUtils.isDevelopMode() ? RunEnvironment.DAILY
    // .getId() : RunEnvironment.ONLINE.getId();
    // Cookie cookie = new Cookie(
    // ChangeDomainAction.COOKIE_SELECT_APP, "search4xxx_run"
    // + runtimeCode);
    // return new Cookie[] { cookie };
    // }
    // };
    // }
    @Override
    public void process() throws Exception {
    // DefaultFilter.setThreadRequest(request);
    }
}

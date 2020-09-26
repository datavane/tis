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
package com.qlangtech.tis.coredefine.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.spring.aop.Func;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-8-11
 */
public class Addreplica extends Corenodemanage {

    private static final long serialVersionUID = 1L;

    @Override
    @Func(PermissionConstant.APP_REPLICA_MANAGE)
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        super.execute(context);
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}

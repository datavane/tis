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
package com.qlangtech.tis.manage.spring.aop;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.qlangtech.tis.manage.common.IUser;

/**
 * Hecla 校验没有操作权限
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-15
 */
public class HeclaHasNoAuthorityException extends RuntimeException {

    private static final long serialVersionUID = 4109918024138247674L;

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public HeclaHasNoAuthorityException(IUser user, Func func) {
        super("Hecla authorize faild loginUser username:" + user.getName() + " userid:" + user.getId() + " has not grant authority on func:" + func.value() + " time:[" + format.format(new Date()) + "]");
    }
}

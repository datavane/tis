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
package com.qlangtech.tis.runtime.module.misc;

import com.alibaba.citrus.turbine.Context;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IFieldErrorHandler {

    String ACTION_ERROR_FIELDS = "action_error_fields";

    void addFieldError(final Context context, String fieldName, String msg, Object... params);

    enum BizLogic {
        APP_NAME_DUPLICATE,
       // DB_NAME_DUPLICATE
    }

    /**
     * 在插件中校验业务逻辑
     *
     * @param logicType
     * @param context
     * @param fieldName
     * @param value
     * @return
     */
    boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value);
}

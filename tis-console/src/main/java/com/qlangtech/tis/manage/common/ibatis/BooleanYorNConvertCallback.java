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
package com.qlangtech.tis.manage.common.ibatis;

import java.sql.SQLException;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-8-2
 */
public class BooleanYorNConvertCallback implements TypeHandlerCallback {

    public static final String YES = "Y";
    public static final String NO = "N";

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        if (NO.equalsIgnoreCase(getter.getString())) {
            return Boolean.FALSE;
        } else if (YES.equalsIgnoreCase(getter.getString())) {
            return Boolean.TRUE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(((Boolean) parameter) ? YES : NO);
    }

    @Override
    public Object valueOf(String s) {
        return NO.equalsIgnoreCase(s);
    }
}

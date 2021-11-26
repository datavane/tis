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
package com.qlangtech.tis.lang;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * 底层运行时异常运行时可直达web，届时可添加一些格式化处理
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-23 18:56
 */
public class TisException extends RuntimeException {

    public static TisException find(Throwable throwable) {
        final Throwable[] throwables = ExceptionUtils.getThrowables(throwable);
        for (Throwable ex : throwables) {
            if (TisException.class.isAssignableFrom(ex.getClass())) {
                return (TisException) ex;
            }
        }
        return null;
    }

    public TisException(String message, Throwable cause) {
        super(message, cause);
    }
}

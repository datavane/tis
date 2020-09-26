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
package com.qlangtech.tis.exception;

/**
 * @description
 * @since 2011-8-11 04:32:27
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TimeManageException extends Exception {

    private static final long serialVersionUID = -5460461738506102586L;

    public TimeManageException() {
        super();
    }

    public TimeManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeManageException(String message) {
        super(message);
    }

    public TimeManageException(Throwable cause) {
        super(cause);
    }
}

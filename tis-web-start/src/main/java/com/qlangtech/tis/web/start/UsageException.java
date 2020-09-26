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
package com.qlangtech.tis.web.start;

/**
 * A Usage Error has occurred. Print the usage and exit with the appropriate exit code.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
@SuppressWarnings("serial")
public class UsageException extends RuntimeException {

    public static final int ERR_LOGGING = -1;

    public static final int ERR_INVOKE_MAIN = -2;

    public static final int ERR_NOT_STOPPED = -4;

    public static final int ERR_BAD_ARG = -5;

    public static final int ERR_BAD_GRAPH = -6;

    public static final int ERR_BAD_STOP_PROPS = -7;

    public static final int ERR_UNKNOWN = -9;

    private int exitCode;

    public UsageException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public UsageException(int exitCode, String format, Object... objs) {
        super(String.format(format, objs));
        this.exitCode = exitCode;
    }

    public UsageException(String format, Object... objs) {
        this(ERR_UNKNOWN, format, objs);
    }

    public UsageException(int exitCode, Throwable cause) {
        super(cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}

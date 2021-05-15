/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.qlangtech.tis.util;

/**
 * Enum that represents {@link Boolean} state (including null for the absence.)
 *
 * <p>
 * This is for situations where we can't use {@link Boolean}, such as annotation elements.
 *
 * @author Kohsuke Kawaguchi
 */
public enum YesNoMaybe {
    YES,
    NO,
    MAYBE;

    public static Boolean toBoolean(YesNoMaybe v) {
        if (v==null)    return null;
        return v.toBool();
    }

    public Boolean toBool() {
        switch (this) {
            case YES:
                return true;
            case NO:
                return false;
            default:
                return null;
        }
    }
}

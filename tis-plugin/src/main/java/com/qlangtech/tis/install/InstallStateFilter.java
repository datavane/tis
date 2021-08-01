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

package com.qlangtech.tis.install;


import com.qlangtech.tis.extension.ExtensionList;

import javax.inject.Provider;
import java.util.List;

/**
 * Allows plugging in to the lifecycle when determining InstallState
 * from {@link InstallUtil#getNextInstallState(InstallState)}
 */
public abstract class InstallStateFilter {
    /**
     * Determine the current or next install state, proceed with `return proceed.next()`
     */
    public abstract InstallState getNextInstallState(InstallState current, Provider<InstallState> proceed);

    /**
     * Get all the InstallStateFilters, in extension order
     */
    public static List<InstallStateFilter> all() {
        return ExtensionList.lookup(InstallStateFilter.class);
    }
}

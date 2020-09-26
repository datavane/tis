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
package com.qlangtech.tis.extension.impl;

import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.util.Util;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Exception thrown if plugin resolution fails due to missing dependencies
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MissingDependencyException extends IOException {

    private String pluginShortName;

    private List<PluginWrapper.Dependency> missingDependencies;

    public MissingDependencyException(String pluginShortName, List<PluginWrapper.Dependency> missingDependencies) {
        super("One or more dependencies could not be resolved for " + pluginShortName + " : " + Util.join(missingDependencies, ", "));
        this.pluginShortName = pluginShortName;
        this.missingDependencies = missingDependencies;
    }

    public List<PluginWrapper.Dependency> getMissingDependencies() {
        return missingDependencies;
    }

    public String getPluginShortName() {
        return pluginShortName;
    }
}

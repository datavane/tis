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

package com.qlangtech.tis.compiler.java;

import javax.tools.JavaFileObject;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-20 16:47
 **/
public class ZipPath {

    private final String parentPath;

    private final String entryName;

    private final JavaFileObject.Kind sourceKind;

    public ZipPath(String parentPath, String entryName, JavaFileObject.Kind sourceKind) {
        super();
        this.parentPath = parentPath;
        this.entryName = entryName;
        this.sourceKind = sourceKind;
    }

    public String getFullSourcePath() {
        // + JavaFileObject.Kind.CLASS.extension;
        StringBuffer result = new StringBuffer(getFullPath());
        if (sourceKind == JavaFileObject.Kind.CLASS) {
            result.append(JavaFileObject.Kind.CLASS.extension);
        } else if (sourceKind == JavaFileObject.Kind.SOURCE) {
            result.append(JavaFileObject.Kind.SOURCE.extension);
        } else if (sourceKind == JavaFileObject.Kind.OTHER) {
            result.append(".scala");
        } else {
            throw new IllegalStateException("source kind:" + this.sourceKind + " is illegal");
        }
        return result.toString();
    }

    public String getFullClassPath() {
        return getFullPath() + JavaFileObject.Kind.CLASS.extension;
    }

    //
    // public String getFullJavaPath() {
    // return getFullPath() + JavaFileObject.Kind.SOURCE.extension;
    // }
    //
    // public String getFullScalaPath() {
    // return getFullPath() + ".scala";
    // }
    public String getFullPath() {
        return parentPath + "/" + this.entryName;
    }

    public String getParentPath() {
        return this.parentPath;
    }

    public String getEntryName() {
        return this.entryName;
    }
}

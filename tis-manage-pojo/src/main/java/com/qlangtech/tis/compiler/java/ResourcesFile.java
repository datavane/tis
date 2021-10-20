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

import java.io.File;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-20 17:00
 **/
public class ResourcesFile {

    private final ZipPath zipPath;

    private final File file;

    public ResourcesFile(ZipPath zipPath, File file) {
        super();
        this.zipPath = zipPath;
        this.file = file;
    }

    public ZipPath getZipPath() {
        return zipPath;
    }

    public File getFile() {
        return file;
    }
}

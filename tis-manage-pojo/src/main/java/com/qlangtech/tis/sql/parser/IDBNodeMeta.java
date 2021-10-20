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

package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Sets;

import java.io.File;
import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-20 17:10
 **/
public interface IDBNodeMeta {
   public static Set<String> appendDBDependenciesClasspath(Set<IDBNodeMeta> dependencyDBNodes) {
        Set<String> classpathElements = Sets.newHashSet();
        for (IDBNodeMeta db : dependencyDBNodes) {
            File jarFile = new File(db.getDaoDir(), db.getDbName() + "-dao.jar");
            if (!jarFile.exists()) {
                throw new IllegalStateException("jarfile:" + jarFile.getAbsolutePath() + " is not exist");
            }
            classpathElements.add(jarFile.getAbsolutePath());
        }
        return classpathElements;
    }

    // getDaoDir(), db.getDbName()
    File getDaoDir();

    String getDbName();
}

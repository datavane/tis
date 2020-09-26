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
package com.qlangtech.tis.offline.impl;

import com.qlangtech.tis.fs.ITISFileSystemFactory;
import com.qlangtech.tis.fullbuild.indexbuild.ITableDumpJobFactory;
import com.qlangtech.tis.offline.AbstractOfflineAssembleFactory;
import com.qlangtech.tis.offline.TableDumpFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class TidbAssembleFactory extends AbstractOfflineAssembleFactory {

    @Override
    public ITableDumpJobFactory getTableDumpFactory() {
        return TableDumpFactory.NO_OP;
    }

    @Override
    public ITISFileSystemFactory getFSFactory() {
        return null;
    }
}

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
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.fs.IFileSplit;
import com.qlangtech.tis.fs.IFileSplitor;
import com.qlangtech.tis.fs.IndexBuildConfig;
import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-27 16:22
 */
public class MockFileSplitor implements IFileSplitor {

    private final IFileSplit fileSplit;

    public MockFileSplitor(IFileSplit fileSplit) {
        this.fileSplit = fileSplit;
    }

    @Override
    public List<IFileSplit> getSplits(IndexBuildConfig config) throws Exception {
        return Collections.singletonList(fileSplit);
    }

    @Override
    public long getTotalSize() {
        return 999;
    }
}

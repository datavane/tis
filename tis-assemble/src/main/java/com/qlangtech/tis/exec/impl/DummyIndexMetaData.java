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
package com.qlangtech.tis.exec.impl;

import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.exec.lifecycle.hook.IIndexBuildLifeCycleHook;
import com.qlangtech.tis.fullbuild.indexbuild.LuceneVersion;
import com.qlangtech.tis.solrdao.impl.ParseResult;

/**
 * 客户端提交的构建请求，只要求构建宽表，不需要构建索引build
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-06 11:46
 */
public class DummyIndexMetaData implements IIndexMetaData {

    @Override
    public ParseResult getSchemaParseResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IIndexBuildLifeCycleHook getIndexBuildLifeCycleHook() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LuceneVersion getLuceneVersion() {
        throw new UnsupportedOperationException();
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.plugin.ds;

import com.alibaba.datax.common.util.ISelectedTabMeta;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 选中需要导入的表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-18 10:16
 */
public interface ISelectedTab extends ISelectedTabMeta {


    default String getName() {
        throw new UnsupportedOperationException();
    }

    default String getWhere() {
        return null;
    }

    default boolean isAllCols() {
        return true;
    }

    @Override
    List<CMeta> getCols();

    /**
     * 通过 transoformer 改写来源列
     * @param pluginCtx
     * @param readerSource 用于生成基于reader的环境绑定参数用，当用于reader端需要有值，用于writer端应该为Optional.empty()
     * @return
     */
    default List<IColMetaGetter> overwriteCols(IMessageHandler pluginCtx, Optional<IReaderSource> readerSource) {
        throw new UnsupportedOperationException();
    }

    default Set<String> acceptedCols() {
        final Set<String> acceptKeys = this.getCols().stream().filter((c) -> !c.isDisable()).map((c) -> c.getName()).collect(Collectors.toSet());
        return acceptKeys;
    }

}

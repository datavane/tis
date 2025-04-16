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

package com.qlangtech.tis.fullbuild.indexbuild;

import com.qlangtech.tis.datax.TimeFormat;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/12/5
 */
public interface IPartionableWarehouse {

    public static IPartionableWarehouse createForNoWriterForTableName() {
        return new IPartionableWarehouse() {
            @Override
            public boolean noRewriteTabName() {
                return true;
            }

            @Override
            public String appendTabPrefix(String rawTabName) {
                return rawTabName;
            }
        };
    }

    /**
     * 取得分区规则
     *
     * @return
     */
    public default TimeFormat getPsFormat() {
        throw new UnsupportedOperationException();
    }

    /**
     * 询问是否会对表名作重命名？
     *
     * @return
     */
    default boolean noRewriteTabName() {
        return false;
    }

    /**
     * 导入到仓库的表可以进行对表名前加一个前缀，如‘ods_’
     *
     * @param rawTabName
     * @return
     */
    // @see AutoCreateTable
    public String appendTabPrefix(String rawTabName);
}

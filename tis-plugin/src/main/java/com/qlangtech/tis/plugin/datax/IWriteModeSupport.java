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

package com.qlangtech.tis.plugin.datax;

import com.qlangtech.tis.manage.common.Option;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部分dataXWriter中就可以设置 writeMode 所以就不需要在增量SelectedTabExtend 中设置writeMode了
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-02-18 12:37
 * @see com.qlangtech.tis.datax.impl.DataxWriter
 **/
public interface IWriteModeSupport {

    public WriteMode getWriteMode();

    public static List<Option> supportModes() {
        return Arrays.stream(WriteMode.values())
                // update 不能幂等，所以忽略
                .filter((mode) -> mode != WriteMode.Update)
                .map((mode) -> new Option(mode.name(), mode.token)).collect(Collectors.toList());
    }

    public
    enum WriteMode {
        Insert("insert"), Replace("replace"), Update("update");
        private final String token;

        public static WriteMode parse(String token) {
            for (WriteMode mode : WriteMode.values()) {
                if (mode.token.equals(token)) {
                    return mode;
                }
            }
            throw new IllegalStateException("token:" + token + " is illegal");
        }

        WriteMode(String token) {
            this.token = token;
        }
    }
}

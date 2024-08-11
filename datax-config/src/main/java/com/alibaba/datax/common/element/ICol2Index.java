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

package com.alibaba.datax.common.element;

import com.qlangtech.tis.plugin.ds.DataType;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-03 13:42
 **/
public interface ICol2Index {
    /**
     * @return key: 列名 ，val：列所在位置
     */
    public Map<String, Col> getCol2Index();

    /**
     * 统计有多少个上下文绑定参数参与数据同步执行
     *
     * @return
     */
    int contextParamValsCount();

    /**
     * @author: 百岁（baisui@qlangtech.com）
     * @create: 2024-08-08 12:56
     **/
    class Col implements Serializable {
        private final int index;
        private final DataType type;

        public Col(int index, DataType type) {
            this.index = index;
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public DataType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "index=" + index +
                    ", type=" + type;
        }
    }
}

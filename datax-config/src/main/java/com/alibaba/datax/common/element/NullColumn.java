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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-11-05 12:33
 **/
public class NullColumn extends Column {
    NullColumn() {
        super(null, Type.NULL, 0);
    }

    @Override
    public Long asLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double asDouble() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String asString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date asDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date asDate(String dateFormat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] asBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean asBoolean() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal asBigDecimal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigInteger asBigInteger() {
        throw new UnsupportedOperationException();
    }
}

/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.trigger.jst;

import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.trigger.socket.LogType;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PayloadMonitorTarget extends RegisterMonotorTarget {

    private static final String INIT_SHOW = "build";

    private final String payload;

    private static final long serialVersionUID = 1L;

    public PayloadMonitorTarget(boolean register, DataXName collection, String payload, LogType logtype) {
        super(register, collection, logtype);
        if (StringUtils.isEmpty(payload)) {
            throw new IllegalArgumentException("param buildName can not be null");
        }
        this.payload = payload;
    }

    public String getPayLoad() {
        return this.payload;
    }

    public boolean isInitShow() {
        return INIT_SHOW.equals(this.getPayLoad());
    }

    @Override
    public int hashCode() {
        return (this.getCollection() + this.getLogType().getValue() + this.getPayLoad()).hashCode();
    }
}

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

package com.qlangtech.tis.plugin.timezone;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.manage.common.Option;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 时区设置分为两种，1.从列表中选择，2.手动输入
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-06-16 16:11
 **/
public abstract class TISTimeZone implements Describable<TISTimeZone> {

    public static final ZoneId DEFAULT_SERVER_TIME_ZONE = ZoneId.systemDefault();

    public static String dftZoneId() {
        return TISTimeZone.DEFAULT_SERVER_TIME_ZONE.getId();
    }

    public abstract ZoneId getTimeZone();

    public static List<Option> availableZoneIds() {
        List<Option> opts = new ArrayList<>();
        ZoneId.SHORT_IDS.forEach((key, val) -> {
            opts.add(new Option(val, val));
        });
        return opts;
    }
}

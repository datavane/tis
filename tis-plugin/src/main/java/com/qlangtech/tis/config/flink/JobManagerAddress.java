/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.config.flink;

import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-23 12:23
 **/
public class JobManagerAddress {
    public final String host;
    public final int port;

    public static JobManagerAddress parse(String value) {
        String[] address = StringUtils.split(value, ":");
        if (address.length != 2) {
            throw new IllegalArgumentException("illegal jobManagerAddress:" + address);
        }
        return new JobManagerAddress(address[0], Integer.parseInt(address[1]));
    }

    public JobManagerAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }
}

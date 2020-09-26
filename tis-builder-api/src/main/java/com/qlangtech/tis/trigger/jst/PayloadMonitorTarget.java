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
package com.qlangtech.tis.trigger.jst;

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

    public PayloadMonitorTarget(boolean register, String collection, String payload, LogType logtype) {
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

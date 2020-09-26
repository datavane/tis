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
package com.qlangtech.tis;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年5月6日下午12:02:12
 */
public class BuAppMap extends HashMap<String, List<App>> {

    private static final long serialVersionUID = 1L;

    private final AtomicLong allRequestCount = new AtomicLong();

    public void addRequest(long value) {
        this.allRequestCount.addAndGet(value);
    }
}

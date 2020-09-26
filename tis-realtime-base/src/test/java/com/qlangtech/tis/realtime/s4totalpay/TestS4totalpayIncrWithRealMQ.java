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
package com.qlangtech.tis.realtime.s4totalpay;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-26 14:25
 */
public class TestS4totalpayIncrWithRealMQ extends AbstractTestS4totalpayIncr {

    public TestS4totalpayIncrWithRealMQ() {
        super(true);
    }

    public void testReceiveMQ() throws Exception {
        synchronized (this) {
            this.wait();
        }
    }
}

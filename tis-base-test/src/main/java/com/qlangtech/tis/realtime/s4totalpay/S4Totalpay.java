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

package com.qlangtech.tis.realtime.s4totalpay;

import com.qlangtech.tis.manage.common.HttpUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-08-21 15:19
 **/
public class S4Totalpay {

    public static void stubSchemaXStream() {
        HttpUtils.addMockApply(-1, "search4totalpay/0/daily/schema.xml", "schema-xstream.xml", S4Totalpay.class);
    }
}

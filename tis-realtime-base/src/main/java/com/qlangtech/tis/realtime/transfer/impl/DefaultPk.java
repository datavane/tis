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
package com.qlangtech.tis.realtime.transfer.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.realtime.transfer.IPk;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月5日 下午7:37:02
 */
public class DefaultPk implements IPk {

    private final String uuid;

    private static final Pattern PK_PATTERN = Pattern.compile("[\\w+|\\d]+");

    public static void main(String[] args) {
        Matcher m = PK_PATTERN.matcher("12345Wwkolp");
        System.out.println(m.matches());
        m = PK_PATTERN.matcher("12345Wwk,olp");
        System.out.println(m.matches());
    }

    public DefaultPk(String uuid) {
        super();
        if (StringUtils.isEmpty(uuid)) {
            throw new IllegalArgumentException("pk can not be null");
        }
        Matcher m = PK_PATTERN.matcher(uuid);
        if (!m.matches()) {
            throw new IllegalArgumentException(uuid + " is not match pattern:" + PK_PATTERN);
        }
        this.uuid = uuid;
    }

    @Override
    public String getRouterVal(String routerKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public String getValue() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object obj) {
        return this.uuid.equals(((DefaultPk) obj).uuid);
    }

    @Override
    public String toString() {
        return this.uuid;
    }
}

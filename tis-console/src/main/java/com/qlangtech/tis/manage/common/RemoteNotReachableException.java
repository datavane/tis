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
package com.qlangtech.tis.manage.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-4-2
 */
public class RemoteNotReachableException extends Exception {

    /**
     */
    private static final long serialVersionUID = 1L;

    private final int group;

    private final String ipaddress;

    public RemoteNotReachableException(int group, String ipaddress) {
        super();
        this.group = group;
        this.ipaddress = ipaddress;
    }

    public int getGroup() {
        return group;
    }

    public String getIpaddress() {
        return ipaddress;
    }
}

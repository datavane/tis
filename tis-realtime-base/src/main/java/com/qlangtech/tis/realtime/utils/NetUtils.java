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
package com.qlangtech.tis.realtime.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月14日
 */
public class NetUtils {

    /**
     * Return a free port number. There is no guarantee it will remain free, so
     * it should be used immediately.
     *
     * @returns A free port for binding a local socket
     */
    public static int getFreeSocketPort() {
        int port = 0;
        try {
            ServerSocket s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
            return port;
        } catch (IOException e) {
        // Could not get a free port. Return default port 0.
        }
        return port;
    }

    /**
     * Return hostname without throwing exception.
     *
     * @return hostname
     */
    public static String getHostname() {
        try {
            return StringUtils.substringBefore(InetAddress.getLocalHost().getHostName(), ".");
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
    }
}

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
package com.qlangtech.tis.order.dump.task;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月3日
 */
public class FindLogConfig {

    public static void main(String[] args) {
        print("/com/qlangtech/tis/dump/yarn/HelloWorldMaster.class");
        print("/log4j.properties");
        print("/log4j.xml");
        print("/logback.xml");
    }

    private static void print(String arg) {
        System.out.println(arg + ":" + FindLogConfig.class.getResource(arg));
    }
}

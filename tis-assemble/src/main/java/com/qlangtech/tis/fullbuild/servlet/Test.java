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

package com.qlangtech.tis.fullbuild.servlet;

import java.util.concurrent.Future;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-08-31 13:00
 **/
public class Test {
    public static void main(String[] args) throws Exception {
        Future<?> f = TisServlet.executeService.submit(() -> {


            while (true) {
                try {

                    System.out.println("i am here");
                    Thread.sleep(5000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        Thread.sleep(1000l);
        f.cancel(true);
        System.out.println("all over");

        Thread.sleep(90000l);
    }
}

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
package com.qlangtech.tis.coredefine.module.action;

import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestLoopQueue extends TestCase {

    public void testQueue() {
        String[] buffer = new String[100];
        LoopQueue<String> queue = new LoopQueue<>(buffer);
        (new Thread() {

            @Override
            public void run() {
                while (true) {
                    String[] read = queue.readBuffer();
                    System.out.println("============================");
                    for (int i = 0; i < read.length; i++) {
                        System.out.print(read[i] + ",");
                    }
                    System.out.println();
                    try {
                        sleep(500);
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
        int i = 0;
        while (true) {
            System.out.println("wite:" + i);
            queue.write(String.valueOf(i++));
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }
    }
}

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

package com.qlangtech.tis.datax;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-04 17:57
 **/
public class Fibonacci {

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        long current = System.currentTimeMillis();
        int turn = 0;
        while (true) {
            for (long counter = 0; counter <= 40; counter++) {
                fibonacci(counter);
            }
            System.out.println("turn:" + turn++);
            if ((System.currentTimeMillis() - current) > 2 * 60000) {
                break;
            }
        }
    }

    public static long fibonacci(long number) {
        if ((number == 0) || (number == 1))
            return number;
        else
            return fibonacci(number - 1) + fibonacci(number - 2);
    }
}

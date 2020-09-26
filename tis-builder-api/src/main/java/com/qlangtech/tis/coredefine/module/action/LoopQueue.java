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

import java.lang.reflect.Array;

/**
 * 循环队列，一个不断向队列中写，另外一个队列可以随时从循环队列中读到
 * 最新的n条记录，结果中有旧到新排列
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-01-09 23:29
 */
public class LoopQueue<T> {

    private final T[] buffer;

    private final int size;

    private int index = 0;

    public LoopQueue(T[] buffer) {
        this.buffer = buffer;
        this.size = buffer.length;
    }

    public void write(T data) {
        synchronized (this) {
            this.buffer[index++ % size] = data;
        }
    }

    public T[] readBuffer() {
        T[] result = (T[]) Array.newInstance(buffer.getClass().getComponentType(), size);
        synchronized (this) {
            int collectIndex = this.index;
            T tmp = null;
            int collect = 0;
            for (int count = 0; count < this.size; count++) {
                if ((tmp = this.buffer[collectIndex++ % size]) != null) {
                    result[collect++] = tmp;
                    if (collect >= (result.length)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void cleanBuffer() {
        synchronized (this) {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = null;
            }
            this.index = 0;
        }
    }
}

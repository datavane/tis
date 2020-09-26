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
package com.qlangtech.tis.indexbuilder.utils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SimpleStack<E> {

    int index;

    int capacity;

    int size;

    Object[] entry;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SimpleStack(int capacity) {
        this.capacity = capacity;
        entry = new Object[capacity];
    }

    public boolean push(E e) {
        if (size == capacity)
            return false;
        entry[size++] = e;
        return true;
    }

    public E pop() {
        if (size == 0)
            return null;
        else {
            return (E) entry[--size];
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }
}

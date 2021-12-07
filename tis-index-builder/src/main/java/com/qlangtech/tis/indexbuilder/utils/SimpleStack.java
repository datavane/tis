/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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

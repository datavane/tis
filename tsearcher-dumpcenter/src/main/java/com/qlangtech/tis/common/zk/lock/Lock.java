/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.common.zk.lock;

/*
 * most of the lock implements are object waiting blocking
 * so pay attention to thread.interrupt mark
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface Lock {

    /**
     * try to get the lock, return true if success or false immediately
     * return true if this machine already hold the lock
     * return false if ZooKeeper service is disable
     */
    public boolean tryLock();

    /**
     * release the lock and return true if success
     * return false if ZooKeeper service is disable
     */
    public boolean unlock();

    /**
     * return true if currently runnable get the lock
     * return false if ZooKeeper service is disable
     */
    public boolean isOwner();
}

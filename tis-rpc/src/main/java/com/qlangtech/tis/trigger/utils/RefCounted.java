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
package com.qlangtech.tis.trigger.utils;

import java.util.concurrent.atomic.AtomicInteger;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class RefCounted<Type> {

    protected final Type resource;

    private boolean closed = false;

    protected final AtomicInteger refcount = new AtomicInteger();

    public RefCounted(Type resource) {
        this.resource = resource;
    }

    public boolean isClosed() {
        return this.closed;
    }

    protected void init(Type resource) {
    }

    public int getRefcount() {
        return refcount.get();
    }

    public final RefCounted<Type> incref() {
        if (refcount.getAndIncrement() == 0) {
            this.init(resource);
        }
        return this;
    }

    public final Type get() {
        return resource;
    }

    public void decref() {
        if (refcount.decrementAndGet() == 0) {
            close();
            this.closed = true;
        }
    }

    protected abstract void close();
}

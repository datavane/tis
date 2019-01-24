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
package com.qlangtech.tis.wangjubao.jingwei;

import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer;
import com.qlangtech.tis.realtime.transfer.IRowValueGetter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Alias {

    private final String name;

    private final String toName;

    private ITransfer valTransfer = NUll_TRANSFER;

    boolean copy = true;

    boolean ignoreChange = false;

    // 啥都不做
    private static final ITransfer NUll_TRANSFER = (r, t) -> {
        return t;
    };

    public Alias t(ITransfer valTransfer) {
        if (valTransfer == null) {
            throw new IllegalArgumentException("param valTransfer can not be null");
        }
        this.valTransfer = valTransfer;
        return this;
    }

    public Alias ignoreChange() {
        this.ignoreChange = true;
        return this;
    }

    /**
     * 在执行copy的时候本列忽略
     *
     * @return
     */
    public Alias notCopy() {
        this.copy = false;
        return this;
    }

    public ITransfer getValTransfer() {
        if (this.valTransfer == null) {
            throw new IllegalStateException("valTransfer can not be null");
        }
        return this.valTransfer;
    }

    public static Alias alias(String name, String toName) {
        return new Alias(name, toName);
    }

    public static Alias $(String name, String toName) {
        return new Alias(name, toName);
    }

    public static Alias $(String name) {
        return new Alias(name, name);
    }

    public static Alias alias(String name) {
        return new Alias(name, name);
    }

    public String getBeanPropName() {
        return BasicPojoConsumer.removeUnderline(this.getName()).toString();
    }

    /**
     * @param name
     * @param toName
     */
    private Alias(String name, String toName) {
        super();
        this.name = name;
        this.toName = toName;
    }

    public Alias(String name) {
        this(name, name);
    }

    public String getName() {
        return name;
    }

    public String getToName() {
        return toName;
    }

    public interface ITransfer {

        /**
         * @param row
         *            整個一條記錄
         * @param t
         *            字段的值
         * @return
         */
        public Object process(IRowValueGetter row, String fieldValue);
    }
}

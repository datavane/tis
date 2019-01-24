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
package com.qlangtech.tis.manage.common.trigger;

/*
 * 每次触发的执行操作类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public enum ExecType {

    UPDATE("update"), CREATE("create"), FULLBUILD("fullbuild");

    private final String type;

    public String getType() {
        return type;
    }

    public static ExecType parse(String value) {
        if (UPDATE.type.equals(value)) {
            return UPDATE;
        } else if (CREATE.type.equals(value)) {
            return CREATE;
        } else if (FULLBUILD.type.equals(value)) {
            return FULLBUILD;
        } else {
            throw new IllegalStateException("value is illeal:" + value);
        }
    }

    private ExecType(String type) {
        this.type = type;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}

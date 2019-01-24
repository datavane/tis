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
package com.qlangtech.tis.common.data.sql;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class SimpleSqlFunction implements SqlFunction {

    /**
     * @uml.property  name="placeHolderName"
     */
    private String placeHolderName;

    /**
     * @uml.property  name="value"
     */
    private String value;

    public SimpleSqlFunction() {
    }

    public SimpleSqlFunction(String placeHolderName, String value) {
        this.placeHolderName = placeHolderName;
        this.value = value;
    }

    /**
     * @return
     * @uml.property  name="placeHolderName"
     */
    @Override
    public String getPlaceHolderName() {
        return this.placeHolderName;
    }

    /**
     * @return
     * @uml.property  name="value"
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * @param value
     * @uml.property  name="value"
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @param placeHolderName
     * @uml.property  name="placeHolderName"
     */
    public void setPlaceHolderName(String placeHolderName) {
        this.placeHolderName = placeHolderName;
    }
}

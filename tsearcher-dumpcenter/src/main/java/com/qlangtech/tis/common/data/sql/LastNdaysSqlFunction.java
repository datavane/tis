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

import java.text.SimpleDateFormat;
import java.util.Date;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LastNdaysSqlFunction implements SqlFunction {

    /**
     * @uml.property  name="n"
     */
    private int n = 30;

    private String format = "yyyy-MM-dd 00:00:00";

    private SimpleDateFormat formater;

    public void init() {
        formater = new SimpleDateFormat(format);
    }

    @Override
    public String getPlaceHolderName() {
        return "last" + n + "Days";
    }

    @Override
    public String getValue() {
        Date d = new Date();
        long l = d.getTime() - this.n * 24 * 60 * 60 * 1000l;
        d = new Date(l);
        return formater.format(d);
    }

    /**
     * @return
     * @uml.property  name="n"
     */
    public int getN() {
        return n;
    }

    /**
     * @param n
     * @uml.property  name="n"
     */
    public void setN(int n) {
        this.n = n;
    }
}

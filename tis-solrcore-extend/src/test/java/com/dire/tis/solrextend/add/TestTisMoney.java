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
package com.dire.tis.solrextend.add;

import com.qlangtech.tis.solrextend.handler.component.TisMoney;
import junit.framework.Assert;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestTisMoney extends TestCase {

    public void testValue() {
        TisMoney tisMoney = TisMoney.create("69.241");
        Assert.assertEquals(69, tisMoney.intValue());
        Assert.assertEquals(6924, tisMoney.getFen());
        Assert.assertEquals("69.24", tisMoney.format());
        tisMoney = TisMoney.create("69");
        Assert.assertEquals(69, tisMoney.intValue());
        Assert.assertEquals(6900, tisMoney.getFen());
        Assert.assertEquals("69.0", tisMoney.format());
        tisMoney = TisMoney.create("69.1");
        Assert.assertEquals(69, tisMoney.intValue());
        Assert.assertEquals(6910, tisMoney.getFen());
        Assert.assertEquals("69.10", tisMoney.format());
        tisMoney = TisMoney.create("0.02");
        Assert.assertEquals(0, tisMoney.intValue());
        Assert.assertEquals(2, tisMoney.getFen());
        Assert.assertEquals("0.02", tisMoney.format());
        tisMoney = TisMoney.create("-0.02");
        Assert.assertEquals(0, tisMoney.intValue());
        Assert.assertEquals(-2, tisMoney.getFen());
        Assert.assertEquals("-0.02", tisMoney.format());
        tisMoney = TisMoney.create("-69.241");
        Assert.assertEquals(-69, tisMoney.intValue());
        Assert.assertEquals(-6924, tisMoney.getFen());
        Assert.assertEquals("-69.24", tisMoney.format());
        tisMoney = TisMoney.create("-69");
        Assert.assertEquals(-69, tisMoney.intValue());
        Assert.assertEquals(-6900, tisMoney.getFen());
        Assert.assertEquals("-69.0", tisMoney.format());
    }
}

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

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.solrextend.handler.component.TisMoney;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestAdd extends TestCase {

    public void testMain() throws Exception {
        // MathContext BigDecimalContext = new MathContext(8,
        // RoundingMode.CEILING);
        // BigDecimal sum = new BigDecimal(132541.350227f,
        // TripleValueMapReduceComponent.BigDecimalContext);
        // System.out.println(sum.setScale(4,
        // RoundingMode.CEILING).doubleValue());
        // RoundingMode[] roundModeAry = new RoundingMode[] {
        // RoundingMode.CEILING,
        // RoundingMode.DOWN, RoundingMode.FLOOR, RoundingMode.HALF_DOWN,
        // RoundingMode.HALF_EVEN, RoundingMode.HALF_UP, RoundingMode.UP };
        // for (int i = 8; i < 12; i++) {
        // for (int j = 0; j < roundModeAry.length; j++) {
        // MathContext BigDecimalContext = MathContext.UNLIMITED;
        // MathContext BigDecimalContext = // roundModeAry[j];//.UNLIMITED;
        // MathContext.UNLIMITED;
        // new MathContext(i, roundModeAry[j]);
        // BigDecimal sum = new BigDecimal(0, BigDecimalContext);
        double sum = 0;
        TisMoney tisMoney = TisMoney.create();
        // LineIterator it = FileUtils.lineIterator(
        // new File("C:\\Users\\baisui\\Desktop\\10.46.74 (3).6"));
        LineIterator it = FileUtils.lineIterator(new File("C:\\Users\\baisui\\Desktop\\dis_solr_full.txt"));
        // StringBuffer menuids = new StringBuffer();
        String fee = null;
        TisMoney tmp = null;
        while (it.hasNext()) {
            fee = StringUtils.split(it.next(), ",")[1];
            tmp = TisMoney.create(fee);
            System.out.println("tmp:" + tmp.getFen() + ",fee:" + fee);
            tisMoney.add(tmp);
            sum += Double.parseDouble(fee);
        // if (sum != Double.parseDouble(tisMoney.format())) {
        // System.out.println(
        // "end....sum:" + sum + ",tismoney:" + tisMoney.format());
        // return;
        // }
        // sum = sum.add(
        // new BigDecimal(StringUtils.split(it.next(), ",")[1],
        // BigDecimalContext));
        // menuids.append(StringUtils
        // .trim(StringUtils.substringBefore(it.next(), "|")))
        // .append(" OR ");
        // sum = sum.add(new BigDecimal(
        // StringUtils.substringBefore(it.next(), ","),
        // BigDecimalContext));
        }
        System.out.println(sum);
        System.out.println(tisMoney);
        System.out.println(tisMoney.getFen());
    // System.out.println(menuids);
    // System.out.println("i:" + i + ",round:" + roundModeAry[j]
    // + ",sum:" + sum.floatValue() + "," + sum.doubleValue());
    // }
    // }
    }
}

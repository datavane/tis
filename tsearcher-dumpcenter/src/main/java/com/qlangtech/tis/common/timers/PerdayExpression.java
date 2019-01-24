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
package com.qlangtech.tis.common.timers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.qlangtech.tis.common.timers.Utils.Hms;

/*
 * {perday 12:00:00} 每天的12点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PerdayExpression implements TimerExpression {

    @Override
    public TimerInfo parse(String expression) throws TimerExpressionException {
        Hms hms = Utils.parseToHMS(expression);
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hms.hr);
        cal.set(Calendar.MINUTE, hms.min);
        cal.set(Calendar.SECOND, hms.sec);
        if (System.currentTimeMillis() > cal.getTimeInMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new TimerInfo(cal.getTimeInMillis() - System.currentTimeMillis(), 24 * 60 * 60 * 1000);
    }
}

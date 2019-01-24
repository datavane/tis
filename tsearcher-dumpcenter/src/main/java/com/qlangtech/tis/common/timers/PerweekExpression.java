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
import com.qlangtech.tis.common.timers.Utils.Dhms;

/*
 * {perweek 2 12:00:00} 每周2的12点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PerweekExpression implements TimerExpression {

    @Override
    public TimerInfo parse(String expression) throws TimerExpressionException {
        Dhms dhms = Utils.parseToDhms(expression);
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_WEEK, dhms.day);
        cal.set(Calendar.HOUR_OF_DAY, dhms.hr);
        cal.set(Calendar.MINUTE, dhms.min);
        cal.set(Calendar.SECOND, dhms.sec);
        long settingDay = cal.get(Calendar.DAY_OF_WEEK);
        // 当前日期
        Calendar nowCal = new GregorianCalendar();
        long nowDay = nowCal.get(Calendar.DAY_OF_WEEK);
        if (System.currentTimeMillis() > cal.getTimeInMillis()) {
            long intervalDay = settingDay + 7 - nowDay;
            cal.add(Calendar.DAY_OF_WEEK_IN_MONTH, (int) intervalDay);
        }
        long intervalTime = cal.getTimeInMillis() - System.currentTimeMillis();
        return new TimerInfo(intervalTime, 7 * 24 * 60 * 60 * 1000);
    }
}

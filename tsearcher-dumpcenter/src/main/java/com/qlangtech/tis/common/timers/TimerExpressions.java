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

import java.util.HashMap;
import java.util.Map;
import com.qlangtech.tis.common.timers.Utils.TimerExp;

/*
 * 时间表达式为 {name expression},通常情况下比如{perday 12:00:00} 代表每天12点执行，系统默认支持如下:<br>
 * <li> perday    每天的什么时候
 * <li> perweek   每周的某天的什么时候
 * <li> permonth  每月的某天的什么时候
 * <li> interval  周期性间隔进行
 * <br><br>如果用户想表达的时间不再系统默认支持的范围以内，需要用户自行实现TimerExpression,在配置时间表达式的时候配置如下形式：<br>
 * <li>{com.taobao.terminator.times.plugins.MyTimerExpression 12:12:23}
 * 会自动加载该类进行时间表达是的parse操作
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TimerExpressions implements TimerExpression {

    Map<String, TimerExpression> expressions;

    ClassLoader classLoader;

    public TimerExpressions(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.initDefaults();
    }

    public TimerExpressions() {
        this(Thread.currentThread().getContextClassLoader());
    }

    private void initDefaults() {
        if (this.expressions == null) {
            this.expressions = new HashMap<String, TimerExpression>();
        }
        expressions.put("perday", new PerdayExpression());
        expressions.put("perweek", new PerweekExpression());
        expressions.put("permonth", new PermonthExpression());
        expressions.put("interval", new IntervalExpression());
    }

    @SuppressWarnings("unchecked")
    @Override
    public TimerInfo parse(String expression) throws TimerExpressionException {
        TimerExp t = Utils.parseToExp(expression);
        TimerExpression te = this.expressions.get(t.name);
        if (te == null) {
            String className = t.name;
            try {
                Class clazz = classLoader.loadClass(className);
                te = (TimerExpression) clazz.newInstance();
            } catch (Exception e) {
                throw new TimerExpressionException("加载外部扩展的TimerExpression对象失败", e);
            }
        }
        if (te != null) {
            return te.parse(t.expression);
        } else {
            throw new TimerExpressionException("无法解析时间表达式 ==> { " + expression + " }");
        }
    }
}

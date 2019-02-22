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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
@Deprecated
public class LazyLoadTimerExpressions implements TimerExpression {

	Map<String, Holder> expressions;

	public LazyLoadTimerExpressions() {
		this.initDefaults();
	}

	private void initDefaults() {
		if (this.expressions == null) {
			this.expressions = new HashMap<String, Holder>();
		}
		expressions.put("perday", new Holder() {

			@Override
			public TimerExpression newInstance() {
				return new PerdayExpression();
			}
		});
		expressions.put("interval", new Holder() {

			@Override
			public TimerExpression newInstance() {
				return new IntervalExpression();
			}
		});
	}

	@Override
	public TimerInfo parse(String expression) throws TimerExpressionException {
		return null;
	}

	abstract class Holder {

		TimerExpression te = null;

		public TimerExpression getInstance() {
			if (te == null) {
				te = newInstance();
			}
			return te;
		}

		public abstract TimerExpression newInstance();
	}
}

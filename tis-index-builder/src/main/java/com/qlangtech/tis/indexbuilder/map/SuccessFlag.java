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
package com.qlangtech.tis.indexbuilder.map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SuccessFlag {
	private final String name;

	// 持有这个任务的task名称
	public SuccessFlag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public enum Flag {
		RUNNING, SUCCESS, KILL, FAILURE
	}

	private Flag flag = Flag.RUNNING;

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	private String msg;

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("name:").append(this.name).append(",flag:").append(flag).append(",msg:").append(this.getMsg());
		return buffer.toString();
	}

}

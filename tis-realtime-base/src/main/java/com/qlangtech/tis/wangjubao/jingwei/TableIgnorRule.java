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
package com.qlangtech.tis.wangjubao.jingwei;

import java.util.HashMap;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TableIgnorRule implements ITableIgnorRule {

	private ITableIgnorRule target;

	public boolean ignor(Map<String, String> record) {
		return target.ignor(record);
	}

	public void setGroovyScript(String tableName, int ruleIndex, String script) {
		final String wrapScript = "package " + TabField.TSEARCH_PACKAGE + ";" //
				+ "import com.qlangtech.tis.wangjubao.jingwei.ITableIgnorRule;" //
				+ "import java.util.Map;" //
				+ "class IGNOR" + tableName + ruleIndex + " implements ITableIgnorRule{"//
				+ "	public boolean ignor(Map<String, String> record) {" + script + "	}" + "}";
		try {
			TabField.loader.loadMyClass(tableName + ruleIndex, wrapScript);
			Class<?> clazz = TabField.loader.loadClass(TabField.TSEARCH_PACKAGE + ".IGNOR" + tableName + ruleIndex);
			target = (ITableIgnorRule) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		TableIgnorRule rule = new TableIgnorRule();
		rule.setGroovyScript("aaa", 1, "return !record['name'] || !record['name'].number");
		Map<String, String> record = new HashMap<String, String>();
		record.put("name", "123.8f");
		System.out.println(rule.ignor(record));
	}
}

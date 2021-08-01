/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.wangjubao.jingwei;

import com.qlangtech.tis.runtime.module.misc.IMessageHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TableIgnorRule implements ITableIgnorRule {

    private ITableIgnorRule target;

    public boolean ignor(Map<String, String> record) {
        return target.ignor(record);
    }

    public void setGroovyScript(String tableName, int ruleIndex, String script) {
        final String wrapScript = "package " + IMessageHandler.TSEARCH_PACKAGE + ";"
                + "import com.qlangtech.tis.wangjubao.jingwei.ITableIgnorRule;"
                + "import java.util.Map;" + "class IGNOR" + tableName + ruleIndex
                + " implements ITableIgnorRule{"
                + "	public boolean ignor(Map<String, String> record) {"
                + script + "	}" + "}";
        try {
            TabField.loader.loadMyClass(tableName + ruleIndex, wrapScript);
            Class<?> clazz = TabField.loader.loadClass(IMessageHandler.TSEARCH_PACKAGE + ".IGNOR" + tableName + ruleIndex);
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

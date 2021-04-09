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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TabField {

    private final String column;

    private final String tabName;

    private String groovyScript;

    public static final AliasGroovyClassLoader loader = new AliasGroovyClassLoader();

    public TabField(String tabName, String column) {
        super();
        this.column = column;
        this.tabName = tabName;
    }

    public String getStringVal(Serializable o) {
        if (o instanceof java.util.Date) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            return format.format(o);
        }
        return String.valueOf(o);
    }

    private IAliasProcess aliasProcess;

    public IAliasProcess getAliasProcess() {
        try {
            if (aliasProcess == null) {
                synchronized (this) {
                    if (aliasProcess == null) {
                        String className = "AliasFieldProcess" + this.getColumn();
                        String script = "	package com.qlangtech.tis ;" + "import java.util.Map;" + "import com.qlangtech.tis.wangjubao.jingwei.impl.AliasProcessImpl;" + "class " + className + " extends AliasProcessImpl {" + "	@Override" + "	public Object process(String value) {" + this.getGroovyScript() + "	}" + "}";
                        loader.loadMyClass(tabName + this.getColumn(), script);
                        Class<?> groovyClass = loader.loadClass("com.qlangtech.tis." + className);
                        aliasProcess = (IAliasProcess) groovyClass.newInstance();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return aliasProcess;
    }

    private String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(String groovyScript) {
        this.groovyScript = groovyScript;
    }

    public String getColumn() {
        return column;
    }

    private String solrFieldName;

    public String getSolrFieldName() {
        return solrFieldName;
    }

    public void setSolrFieldName(String solrFieldName) {
        this.solrFieldName = solrFieldName;
    }

    public static void main(String[] arg) {
        TabField field = new TabField("xxx", "aaa");
        // field.setGroovyScript(" return record['sellerId']+record['id'];");
        field.setGroovyScript(" return fmtStrTime( record['time']);");
        Map<String, String> row = new HashMap<String, String>();
        // row.put("sellerId", "aaaaaaaaa");
        row.put("id", "bbbbbbbb");
        row.put("time", "2013-11-12 11:09:11");
        System.out.println(field.getAliasProcess().process("2013-11-12 11:09:11"));
    }
}

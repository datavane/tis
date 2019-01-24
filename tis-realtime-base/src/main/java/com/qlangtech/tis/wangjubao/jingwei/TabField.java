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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TabField {

    public static final String TSEARCH_PACKAGE = "com.taobao.tsearcher";

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

    // public String[] getColunmAry() {
    // return StringUtils.split(column, "+");
    // }
    // public String getAliasName() {
    // return this.aliasName;
    // }
    // 
    // public void setAliasName(String name) {
    // this.aliasName = name;
    // }
    // public void setType(Module.Type type) {
    // this.type = type;
    // }
    // public Module.Type getType() {
    // return type;
    // }
    private IAliasProcess aliasProcess;

    public IAliasProcess getAliasProcess() {
        try {
            if (aliasProcess == null) {
                synchronized (this) {
                    if (aliasProcess == null) {
                        String className = "AliasFieldProcess" + this.getColumn();
                        String script = "	package com.taobao.tsearcher ;" + "import java.util.Map;" + "class " + className + " extends com.taobao.terminator.wangjubao.jingwei.impl.AliasProcessImpl {" + "	@Override" + "	public Object process(Map<String, String> record) {" + this.getGroovyScript() + "	}" + "}";
                        loader.loadMyClass(tabName + this.getColumn(), script);
                        Class<?> groovyClass = loader.loadClass("com.taobao.tsearcher." + className);
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
        System.out.println(field.getAliasProcess().process(row));
    }
}

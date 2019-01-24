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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AliasGroovyClassLoader extends GroovyClassLoader {

    public void loadMyClass(String name, String script) throws Exception {
        // createCompilationUnit(config,
        CompilationUnit unit = // codeSource.getCodeSource());
        new CompilationUnit();
        // GroovyCodeSource source1 = new GroovyCodeSource(script, "name",
        // "gbk");
        SourceUnit su = unit.addSource(name, script);
        ClassCollector collector = createCollector(unit, su);
        unit.setClassgenCallback(collector);
        unit.compile(Phases.CLASS_GENERATION);
        for (Object o : collector.getLoadedClasses()) {
            setClassCacheEntry((Class<?>) o);
            System.out.println(o);
        }
    }

    public static void main(String[] arg) throws Exception {
        // GroovyClassLoader
        AliasGroovyClassLoader loader = new AliasGroovyClassLoader();
        String script = "	package groovytest ;" + "import java.util.Map;" + "class AliasFieldProcess implements groovytest.Iprocess {" + "	@Override" + "	public Object process(Map<String, String> value) {" + "		def result = value.get(\"hello\");" + "     return result;" + "	}" + "}";
        loader.loadMyClass("name", script);
        Class groovyClass = loader.loadClass("groovytest.AliasFieldProcess");
    // Iprocess object = (Iprocess) groovyClass.newInstance();
    // Map<String,String> values = new HashMap<String,String>();
    // values.put("hello", "baisui");
    // System.out.println(object.process(values));
    }
}

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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class AliasGroovyClassLoader extends GroovyClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(AliasGroovyClassLoader.class);

    public void loadMyClass(String name, String script) throws Exception {
        // createCompilationUnit(config,
        // codeSource.getCodeSource());
        CompilationUnit unit = new CompilationUnit();
        SourceUnit su = unit.addSource(name, script);
        ClassCollector collector = createCollector(unit, su);
        unit.setClassgenCallback(collector);
        unit.compile(Phases.CLASS_GENERATION);
        int classEntryCount = 0;
        for (Object o : collector.getLoadedClasses()) {
            setClassCacheEntry((Class<?>) o);
            // System.out.println(o);
            classEntryCount++;
        }
        logger.info("load transfer {}, classEntryCount:{}", name, classEntryCount);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
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

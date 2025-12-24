/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.extension.util;

import com.qlangtech.tis.TIS;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-01-12 10:02
 **/
public final class CustomerGroovyClassLoader extends GroovyClassLoader {
    public CustomerGroovyClassLoader(ClassLoader parent) {
        super(new ClassLoader(parent) {
                  @Override
                  protected Class<?> findClass(String name) throws ClassNotFoundException {
                      return TIS.get().getPluginManager().uberClassLoader.findClass(name);
                  }
              }
        );
    }

    @SuppressWarnings("all")
    public void loadMyClass(String name, String script) throws Exception {
        CompilationUnit unit = new CompilationUnit(this);
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
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.extension.util.impl;


import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.util.CustomerGroovyClassLoader;
import com.qlangtech.tis.extension.util.GroovyShellEvaluate;
import com.qlangtech.tis.extension.util.GroovyShellFactory;
import groovy.lang.GroovyShell;

/**
 * 默认
 */
public class DefaultGroovyShellFactory implements GroovyShellFactory {

    private final ClassLoader parent;

    public DefaultGroovyShellFactory() {
        this(GroovyShellEvaluate.class.getClassLoader());
    }

    public DefaultGroovyShellFactory(ClassLoader parent) {
        this.parent = parent;
    }

    @Override
    public CustomerGroovyClassLoader createGroovyLoader() {
        return new CustomerGroovyClassLoader(parent);
    }

    @Override
    public GroovyShell createGroovyShell() {
        return new GroovyShell(new ClassLoader(parent) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                return TIS.get().getPluginManager().uberClassLoader.findClass(name);
            }
        });
    }

    @Override
    public boolean isInConsoleModule() {
        return false;
    }
}

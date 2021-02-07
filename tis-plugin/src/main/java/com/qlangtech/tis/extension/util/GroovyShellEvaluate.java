/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.extension.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.concurrent.ExecutionException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-06 13:27
 */
public class GroovyShellEvaluate {

    final static GroovyShell shell = new GroovyShell(new CompilerConfiguration());
    private static final LoadingCache<String, Script> scriptCache
            = CacheBuilder.newBuilder().build(new CacheLoader<String, Script>() {
        @Override
        public Script load(String key) throws Exception {
            Script parse = shell.parse(key);
            return parse;
        }
    });

    private GroovyShellEvaluate() {
    }

    public static <T> T eval(String javaScript) {
        try {
            Script script = scriptCache.get(javaScript);
            return (T) script.run();
        } catch (ExecutionException e) {
            throw new RuntimeException(javaScript, e);
        }
    }

}

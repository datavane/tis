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
package com.qlangtech.tis.extension.util;

import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-06 13:27
 */
public class GroovyShellEvaluate {
    static final boolean isInConsoleModule;

    static {
        boolean loaded = false;
        try {
            loaded = (null != Class.forName("com.qlangtech.tis.runtime.module.action.BasicModule"));
        } catch (ClassNotFoundException e) {
        }
        isInConsoleModule = loaded;
    }

    public static <T> T createParamizerScript(Class parentClazz, String className, String script) {
        try {
//        String className = parentClazz.getSimpleName() + "_SubFormIdListGetter_" + subFormField.getName();
            String pkg = parentClazz.getPackage().getName();
//        String script = "	package " + pkg + " ;"
//                + "import java.util.Map;"
//                + "import com.qlangtech.tis.coredefine.module.action.DataxAction; "
//                + "import com.qlangtech.tis.util.DescriptorsJSON.IPropGetter; "
//                + "import com.qlangtech.tis.extension.IPropertyType; "
//                + "class " + className + " implements IPropGetter {"
//                + "	@Override"
//                + "	public Object build(IPropertyType.SubFormFilter filter) {" + this.getIdListGetScript() + "	}" + "}";
            //this.getIdListGetScript()
            GroovyShellUtil.loadMyClass(className, script);
            Class<?> groovyClass = GroovyShellUtil.loadClass(pkg, className);
            return (T) groovyClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object scriptEval(String script, Function<Object, Object>... process) {
        try {
            UploadPluginMeta meta = UploadPluginMeta.parse(script, true);
            boolean unCache = meta.getBoolean(UploadPluginMeta.KEY_UNCACHE);

            Callable<Object> valGetter = () -> {
                for (Function<Object, Object> f : process) {
                    Object val = GroovyShellUtil.eval(meta.getName());
                    if (val == null) {
                        return null;
                    }
                    return f.apply(val);
                }
                return GroovyShellUtil.eval(meta.getName());
            };
            return unCache ? new JsonUtil.UnCacheString(valGetter) : valGetter.call();
        } catch (Exception e) {
            throw new RuntimeException("script:" + script, e);
        }
    }


//    final static GroovyShell shell = new GroovyShell(new ClassLoader(GroovyShellEvaluate.class.getClassLoader()) {
//        @Override
//        protected Class<?> findClass(String name) throws ClassNotFoundException {
//            // return super.findClass(name);
//            return TIS.get().getPluginManager().uberClassLoader.findClass(name);
//        }
//    });

    private GroovyShellEvaluate() {
    }

}

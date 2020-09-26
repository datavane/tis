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
package com.qlangtech.tis.util;

import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.xml.XppDriver;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class XStream2PluginInfoReader extends XStream2 {

    public Field mock;

    public XStream2PluginInfoReader(XppDriver xppDruver) {
        super(xppDruver);
    }

    @Override
    public ReflectionProvider createReflectionProvider() {
        return (ReflectionProvider) Proxy.newProxyInstance(this.getClassLoader(), new Class[] { ReflectionProvider.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("newInstance".equals(method.getName())) {
                    return new Object();
                }
                if ("getFieldOrNull".equals(method.getName())) {
                    // System.out.println("==============================" + method.getName() + ",args[1]:" + args[1]);
                    if (RobustReflectionConverter.KEY_ATT_PLUGIN.equals(args[1]) || "class".equals(args[1])) {
                        return null;
                    }
                    return getMockField();
                }
                if ("getField".equals(method.getName())) {
                    return getMockField();
                }
                if ("getFieldType".equals(method.getName())) {
                    return Object.class;
                }
                return null;
            }
        });
    // return super.createReflectionProvider();
    }

    private Object getMockField() throws NoSuchFieldException {
        return XStream2PluginInfoReader.class.getField("mock");
    }

    public static void main(String[] args) {
    // XStream2PluginInfoReader pluginInfoReader = new XStream2PluginInfoReader();
    // pluginInfoReader.un
    }
}

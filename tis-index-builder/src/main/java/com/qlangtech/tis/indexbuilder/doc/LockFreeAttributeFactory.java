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
package com.qlangtech.tis.indexbuilder.doc;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.AttributeImpl;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LockFreeAttributeFactory extends AttributeFactory {

    public static ThreadLocal<WeakHashMap<Class<? extends Attribute>, WeakReference<Class<? extends AttributeImpl>>>> tl = new ThreadLocal<WeakHashMap<Class<? extends Attribute>, WeakReference<Class<? extends AttributeImpl>>>>();

    /*
	 * private static final WeakHashMap<Class<? extends Attribute>,
	 * WeakReference<Class<? extends AttributeImpl>>> attClassImplMap = new
	 * WeakHashMap<Class<? extends Attribute>, WeakReference<Class<? extends
	 * AttributeImpl>>>();
	 */
    public LockFreeAttributeFactory() {
    }

    @Override
    public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
        try {
            return getClassForInterface(attClass).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate implementing class for " + attClass.getName());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate implementing class for " + attClass.getName());
        }
    }

    private static Class<? extends AttributeImpl> getClassForInterface(Class<? extends Attribute> attClass) {
        WeakHashMap<Class<? extends Attribute>, WeakReference<Class<? extends AttributeImpl>>> attClassImplMap = tl.get();
        synchronized (attClassImplMap) {
            final WeakReference<Class<? extends AttributeImpl>> ref = attClassImplMap.get(attClass);
            Class<? extends AttributeImpl> clazz = (ref == null) ? null : ref.get();
            if (clazz == null) {
                try {
                    // (enforce new impl for this deprecated att):
                    if (CharTermAttribute.class.equals(attClass)) {
                        clazz = CharTermAttributeImpl.class;
                    } else {
                        clazz = Class.forName(attClass.getName() + "Impl", true, attClass.getClassLoader()).asSubclass(AttributeImpl.class);
                    }
                    attClassImplMap.put(attClass, new WeakReference<Class<? extends AttributeImpl>>(clazz));
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Could not find implementing class for " + attClass.getName());
                }
            }
            return clazz;
        }
    }
}

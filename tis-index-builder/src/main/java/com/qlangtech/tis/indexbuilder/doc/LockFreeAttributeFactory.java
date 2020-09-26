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

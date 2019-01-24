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
package com.qlangtech.tis.indexbuilder.doc;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.AttributeImpl;

/* *
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

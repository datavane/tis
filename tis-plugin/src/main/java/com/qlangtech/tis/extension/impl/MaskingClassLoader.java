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
package com.qlangtech.tis.extension.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Enumeration;
import java.util.Collections;

/**
 * {@link ClassLoader} that masks a specified set of classes
 * from its parent class loader.
 * <p>
 * This code is used to create an isolated environment.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MaskingClassLoader extends ClassLoader {

    /**
     * Prefix of the packages that should be hidden.
     */
    private final List<String> masksClasses = new ArrayList<String>();

    private final List<String> masksResources = new ArrayList<String>();

    public MaskingClassLoader(ClassLoader parent, String... masks) {
        this(parent, Arrays.asList(masks));
    }

    public MaskingClassLoader(ClassLoader parent, Collection<String> masks) {
        super(parent);
        this.masksClasses.addAll(masks);
        /**
         * The name of a resource is a '/'-separated path name
         */
        for (String mask : masks) {
            masksResources.add(mask.replace(".", "/"));
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String mask : masksClasses) {
            if (name.startsWith(mask))
                throw new ClassNotFoundException();
        }
        return super.loadClass(name, resolve);
    }

    @Override
    public synchronized URL getResource(String name) {
        if (isMasked(name))
            return null;
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (isMasked(name))
            return Collections.emptyEnumeration();
        return super.getResources(name);
    }

    public synchronized void add(String prefix) {
        masksClasses.add(prefix);
        if (prefix != null) {
            masksResources.add(prefix.replace(".", "/"));
        }
    }

    private boolean isMasked(String name) {
        for (String mask : masksResources) {
            if (name.startsWith(mask))
                return true;
        }
        return false;
    }
}

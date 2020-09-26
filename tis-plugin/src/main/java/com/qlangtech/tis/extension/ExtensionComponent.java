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
package com.qlangtech.tis.extension;

import com.qlangtech.tis.util.Util;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ExtensionComponent<T> implements Comparable<ExtensionComponent<T>> {

    private static final Logger LOG = Logger.getLogger(ExtensionComponent.class.getName());

    private final T instance;

    private final double ordinal;

    public ExtensionComponent(T instance, double ordinal) {
        this.instance = instance;
        this.ordinal = ordinal;
    }

    public ExtensionComponent(T instance, TISExtension annotation) {
        this(instance, annotation.ordinal());
    }

    public ExtensionComponent(T instance) {
        this(instance, 0);
    }

    /**
     */
    public double ordinal() {
        return ordinal;
    }

    /**
     * The instance of the discovered extension.
     *
     * @return never null.
     */
    public T getInstance() {
        return instance;
    }

    /**
     * Checks if this component is a {@link Descriptor} describing the given type
     *
     * For example, {@code component.isDescriptorOf(Builder.class)}
     */
    public boolean isDescriptorOf(Class<? extends Describable> c) {
        return instance instanceof Descriptor && ((Descriptor) instance).isSubTypeOf(c);
    }

    /**
     * Sort {@link ExtensionComponent}s in the descending order of {@link #ordinal()}.
     */
    public int compareTo(ExtensionComponent<T> that) {
        double a = this.ordinal();
        double b = that.ordinal();
        if (a > b)
            return -1;
        if (a < b)
            return 1;
        // make the order bit more deterministic among extensions of the same ordinal
        if (this.instance instanceof Descriptor && that.instance instanceof Descriptor) {
            try {
                return Util.fixNull(((Descriptor) this.instance).getDisplayName()).compareTo(Util.fixNull(((Descriptor) that.instance).getDisplayName()));
            } catch (RuntimeException x) {
                LOG.log(Level.WARNING, null, x);
            } catch (LinkageError x) {
                LOG.log(Level.WARNING, null, x);
            }
        }
        return this.instance.getClass().getName().compareTo(that.instance.getClass().getName());
    }
}

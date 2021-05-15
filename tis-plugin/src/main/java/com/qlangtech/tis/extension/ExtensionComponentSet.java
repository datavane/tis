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
package com.qlangtech.tis.extension;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents the components that's newly discovered during {@link ExtensionFinder#refresh()}.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class ExtensionComponentSet {

    /**
     * Discover extensions of the given type.
     *
     * @return Can be empty but never null.
     */
    public abstract <T> Collection<ExtensionComponent<T>> find(Class<T> type);

    public final ExtensionComponentSet filtered() {
        final ExtensionComponentSet base = this;
        return new ExtensionComponentSet() {

            @Override
            public <T> Collection<ExtensionComponent<T>> find(Class<T> type) {
                // List<ExtensionComponent<T>> a = Lists.newArrayList();
                return base.find(type);
                // for (ExtensionComponent<T> c : base.find(type)) {
                // if (ExtensionFilter.isAllowed(type,c))
                // a.add(c);
                // }
                // return a;
            }
        };
    }

    /**
     * Constant that has zero component in it.
     */
    public static final ExtensionComponentSet EMPTY = new ExtensionComponentSet() {

        @Override
        public <T> Collection<ExtensionComponent<T>> find(Class<T> type) {
            return Collections.emptyList();
        }
    };

    /**
     * Computes the union of all the given delta.
     */
    public static ExtensionComponentSet union(final Collection<? extends ExtensionComponentSet> base) {
        return new ExtensionComponentSet() {

            @Override
            public <T> Collection<ExtensionComponent<T>> find(Class<T> type) {
                List<ExtensionComponent<T>> r = Lists.newArrayList();
                for (ExtensionComponentSet d : base) { r.addAll(d.find(type));}
                return r;
            }
        };
    }

    public static ExtensionComponentSet union(ExtensionComponentSet... members) {
        return union(Arrays.asList(members));
    }

    /**
     * Wraps {@link ExtensionFinder} into {@link ExtensionComponentSet}.
     */
    public static ExtensionComponentSet allOf(final ExtensionFinder f) {
        return new ExtensionComponentSet() {

            @Override
            public <T> Collection<ExtensionComponent<T>> find(Class<T> type) {
                return f.find(type, TIS.get());
            }
        };
    }
}

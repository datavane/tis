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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements memoization semantics.
 * <p>
 * Conceptually a function from K -> V that computes values lazily and remembers the results.
 * Often used to implement a data store per key.
 * @since 1.281
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class Memoizer<K, V> {

    private final ConcurrentHashMap<K, V> store = new ConcurrentHashMap<K, V>();

    public V get(K key) {
        V v = store.get(key);
        if (v != null)
            return v;
        // that represents "the value is being computed". FingerprintMap does this.
        synchronized (this) {
            v = store.get(key);
            if (v != null)
                return v;
            v = compute(key);
            store.put(key, v);
            return v;
        }
    }

    /**
     * Creates a new instance.
     */
    public abstract V compute(K key);

    /**
     * Clears all the computed values.
     */
    public void clear() {
        store.clear();
    }

    /**
     * Provides a snapshot view of all {@code V}s.
     */
    public Iterable<V> values() {
        return store.values();
    }
}

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

import com.qlangtech.tis.TIS;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ExtensionList<T> extends AbstractList<T> {

    protected final TIS tis;

    private final Class<T> extensionType;

    private volatile List<ExtensionComponent<T>> extensions;

    public static <T> ExtensionList<T> create(TIS tis, Class<T> type) {
        return new ExtensionList<T>(tis, type);
    }

    public List<ExtensionComponent<T>> getComponents() {
        return Collections.unmodifiableList(ensureLoaded());
    }

    protected ExtensionList(TIS tis, Class<T> extensionType) {
        this.tis = tis;
        this.extensionType = extensionType;
        if (tis == null) {
            extensions = Collections.emptyList();
        }
    }

    /**
     */
    public static <T> ExtensionList<T> lookup(Class<T> type) {
        TIS j = TIS.get();
        return j == null ? create(null, type) : j.getExtensionList(type);
    }

    public Class<T> getExtensionType() {
        return this.extensionType;
    }

    @Override
    public T get(int index) {
        return ensureLoaded().get(index).getInstance();
    }

    private List<ExtensionComponent<T>> ensureLoaded() {
        if (extensions != null) {
            // already loaded
            return extensions;
        }
        synchronized (getLoadLock()) {
            if (extensions == null) {
                List<ExtensionComponent<T>> r = load();
                // r.addAll(legacyInstances);
                extensions = sort(r);
            }
            return extensions;
        }
    }

    protected List<ExtensionComponent<T>> sort(List<ExtensionComponent<T>> r) {
        r = new ArrayList<ExtensionComponent<T>>(r);
        Collections.sort(r);
        return r;
    }

    protected List<ExtensionComponent<T>> load() {
        return tis.getPluginManager().getPluginStrategy().findComponents(extensionType, tis);
    }

    protected Collection<ExtensionComponent<T>> load(ExtensionComponentSet delta) {
        return delta.find(extensionType);
    }

    private final Lock lock = new Lock();

    protected Object getLoadLock() {
        return lock;
    }

    private static final class Lock {
    }

    @Override
    public int size() {
        return ensureLoaded().size();
    }
}

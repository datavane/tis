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

import com.qlangtech.tis.TIS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ExtensionList<T> extends AbstractList<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionList.class);
    protected final TIS tis;

    private final Class<T> extensionType;

    private volatile List<ExtensionComponent<T>> extensions;
    private final List<ExtensionListListener> listeners = new CopyOnWriteArrayList<>();

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
            this.extensions = Collections.emptyList();
        }
    }


    /**
     * Used during {@link 'Jenkins#refreshExtensions()'} to add new components into existing {@link ExtensionList}s.
     * Do not call from anywhere else.
     */
    public void refresh(ExtensionComponentSet delta) {
        boolean fireOnChangeListeners = false;
        synchronized (getLoadLock()) {
            if (extensions == null) {
                return;     // not yet loaded. when we load it, we'll load everything visible by then, so no work needed
            }
            Collection<ExtensionComponent<T>> found = load(delta);
            if (!found.isEmpty()) {
                List<ExtensionComponent<T>> l = new ArrayList<>(extensions);
                l.addAll(found);
                extensions = sort(l);
                fireOnChangeListeners = true;
            }
        }
        if (fireOnChangeListeners) {
            fireOnChangeListeners();
        }
    }

    private void fireOnChangeListeners() {
        for (ExtensionListListener listener : listeners) {
            try {
                listener.onChange();
            } catch (Exception e) {
                LOGGER.info("Error firing ExtensionListListener.onChange().", e);
            }
        }
    }

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

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
package com.qlangtech.tis.plugin;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.XmlFile;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CollectionPluginStore<T> extends PluginStore {

    private final String serializeFileName;

    public CollectionPluginStore(Key key) {
        super(key.pluginClass, key.getSotreFile());
        this.serializeFileName = key.getSerializeFileName();
    }

    @Override
    protected String getSerializeFileName() {
        return serializeFileName;
    }

    public static class Key {

        private final String collection;

        private final Class<? extends Describable> pluginClass;

        public Key(String collection, Class<? extends Describable> pluginClass) {
            if (StringUtils.isEmpty(collection)) {
                throw new IllegalArgumentException("param key.collection can not be null");
            }
            this.collection = collection;
            this.pluginClass = pluginClass;
        }

        protected String getSerializeFileName() {
            return collection + File.separator + pluginClass.getName();
        }

        private XmlFile getSotreFile() {
            return Descriptor.getConfigFile(getSerializeFileName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Key key = (Key) o;
            return Objects.equals(collection, key.collection) && Objects.equals(pluginClass, key.pluginClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(collection, pluginClass);
        }
    }
}

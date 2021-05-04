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
public class KeyedPluginStore<T extends Describable> extends PluginStore<T> {

    protected transient final Key key;

    public KeyedPluginStore(Key key) {
        super(key.pluginClass, key.getSotreFile());
        this.key = key;
    }

    @Override
    protected String getSerializeFileName() {
        return key.getSerializeFileName();
    }

    public static class Key<T extends Describable> {

        public final String keyVal;
        protected final String groupName;

        protected final Class<T> pluginClass;

        public Key(String groupName, String keyVal, Class<T> pluginClass) {
            if (StringUtils.isEmpty(keyVal)) {
                throw new IllegalArgumentException("param 'key' can not be null");
            }
            this.keyVal = keyVal;
            this.pluginClass = pluginClass;
            this.groupName = groupName;
        }

        protected String getSerializeFileName() {
            return this.getSubDirPath() + File.separator + pluginClass.getName();
        }

        public String getSubDirPath() {
            return groupName + File.separator + keyVal;
        }

        public XmlFile getSotreFile() {
            return Descriptor.getConfigFile(getSerializeFileName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Key key = (Key) o;
            return this.hashCode() == key.hashCode();
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyVal, pluginClass);
        }
    }

}

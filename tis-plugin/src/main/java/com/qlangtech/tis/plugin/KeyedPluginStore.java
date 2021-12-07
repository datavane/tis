/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.plugin;

import com.qlangtech.tis.datax.impl.DataxReader;
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
    public static final String TMP_DIR_NAME = ".tmp/";
    public transient final Key key;

    public static <TT extends Describable> KeyedPluginStore<TT> getPluginStore(
            DataxReader.SubFieldFormAppKey<TT> subFieldFormKey, IPluginProcessCallback<TT>... pluginCreateCallback) {
        return new KeyedPluginStore(subFieldFormKey, pluginCreateCallback);
    }

    public KeyedPluginStore(Key key, IPluginProcessCallback<T>... pluginCreateCallback) {
        super(key.pluginClass, key.getSotreFile(), pluginCreateCallback);
        this.key = key;
    }

    public interface IPluginKeyAware {
        public void setKey(Key key);
    }

    @Override
    public T getPlugin() {
        T plugin = super.getPlugin();
        if (plugin instanceof IPluginKeyAware) {
            ((IPluginKeyAware) plugin).setKey(this.key);
        }
        return plugin;
    }

    @Override
    protected String getSerializeFileName() {
        return key.getSerializeFileName();
    }

    public static class Key<T extends Describable> {

        public final KeyVal keyVal;
        protected final String groupName;

        protected final Class<T> pluginClass;

        public Key(String groupName, String keyVal, Class<T> pluginClass) {
            this(groupName, new KeyVal(keyVal), pluginClass);
        }

        public Key(String groupName, KeyVal keyVal, Class<T> pluginClass) {
            Objects.requireNonNull(keyVal, "keyVal can not be null");
            this.keyVal = keyVal;
            this.pluginClass = pluginClass;
            this.groupName = groupName;
        }

        protected String getSerializeFileName() {
            return this.getSubDirPath() + File.separator + pluginClass.getName();
        }

        public String getSubDirPath() {
            return groupName + File.separator + keyVal.getKeyVal();
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
            return Objects.hash(keyVal.getKeyVal(), pluginClass);
        }
    }

    public static class KeyVal {
        private final String val;
        private final String suffix;

        public KeyVal(String val, String suffix) {
            if (StringUtils.isEmpty(val)) {
                throw new IllegalArgumentException("param 'key' can not be null");
            }
            this.val = val;
            this.suffix = suffix;
        }

        @Override
        public String toString() {
            return getKeyVal();
        }

        public String getKeyVal() {
            return StringUtils.isBlank(this.suffix) ? val : TMP_DIR_NAME + (val + "-" + this.suffix);
        }

        public KeyVal(String val) {
            this(val, StringUtils.EMPTY);
        }

        public String getVal() {
            return val;
        }

        public String getSuffix() {
            return suffix;
        }
    }

}

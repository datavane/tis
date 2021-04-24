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

package com.qlangtech.tis.datax;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.extension.impl.IOUtils;
import org.shai.xmodifier.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-24 16:25
 **/
public interface IDataXPluginMeta {

    default DataXMeta getDataxMeta() {
        Class<?> clazz = this.getClass();
        return IOUtils.loadResourceFromClasspath(clazz, clazz.getSimpleName() + "_plugin.json", true
                , new IOUtils.WrapperResult<DataXMeta>() {
                    @Override
                    public DataXMeta process(InputStream input) throws IOException {
                        return JSON.parseObject(input, DataXMeta.class);
                    }
                });
    }

    public class DataXMeta {
        private String name;
        private String clazz;
        private String description;
        private String developer;

        public String getImplClass() {
            if (StringUtils.isEmpty(this.clazz)) {
                throw new IllegalStateException("plugin:" + name + " relevant implements class can not be null ");
            }
            return this.clazz;
        }

        public void setClass(String clazz) {
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDeveloper() {
            return developer;
        }

        public void setDeveloper(String developer) {
            this.developer = developer;
        }
    }
}

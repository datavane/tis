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
package com.qlangtech.tis.web.start;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class TISAppClassLoader extends URLClassLoader {
    private final String context;

    public TISAppClassLoader(String context, ClassLoader parent, URL[] urls) throws IOException {
        super(urls, parent);
        this.context = context;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (Throwable e) {
            URL[] urLs = this.getURLs();
            StringBuffer urlsToStr = new StringBuffer("submodule ");
            urlsToStr.append("'").append(context).append("',");
            for (URL url : urLs) {
                urlsToStr.append(String.valueOf(url)).append(",");
            }
            throw new RuntimeException(urlsToStr.toString(), e);
        }
    }
}

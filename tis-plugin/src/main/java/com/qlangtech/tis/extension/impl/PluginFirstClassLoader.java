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
package com.qlangtech.tis.extension.impl;

import org.apache.tools.ant.AntClassLoader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * classLoader which use first /WEB-INF/lib/*.jar and /WEB-INF/classes before core classLoader
 * <b>you must use the pluginFirstClassLoader true in the maven-hpi-plugin</b>
 * @since 1.371
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class PluginFirstClassLoader extends AntClassLoader implements Closeable {

    private List<URL> urls = new ArrayList<URL>();

    public void addPathFiles(Collection<File> paths) throws IOException {
        for (File f : paths) {
            urls.add(f.toURI().toURL());
            addPathFile(f);
        }
    }

    /**
     * @return List of jar used by the plugin /WEB-INF/lib/*.jar and classes directory /WEB-INF/classes
     */
    public List<URL> getURLs() {
        return urls;
    }

//    @Override
//    public void close()  {
//        cleanup();
//    }

    @Override
    protected Enumeration findResources(String arg0, boolean arg1) throws IOException {
        Enumeration enu = super.findResources(arg0, arg1);
        return enu;
    }

    @Override
    protected Enumeration findResources(String name) throws IOException {
        Enumeration enu = super.findResources(name);
        return enu;
    }

    @Override
    public URL getResource(String arg0) {
        URL url = super.getResource(arg0);
        return url;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = super.getResourceAsStream(name);
        return is;
    }
}

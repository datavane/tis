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
package com.qlangtech.tis.hdfs.client.context.impl.tsearcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import com.qlangtech.tis.solrextend.dir.loader.HdfsClassLoader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Handler extends URLStreamHandler {

    private static final String HANDLER_PKG = "com.qlangtech.tis.hdfs.client.context.impl";

    public static void setup() {
        String oldValue = System.getProperty("java.protocol.handler.pkgs");
        if (oldValue != null) {
            if (oldValue.indexOf(HANDLER_PKG) > -1)
                return;
            oldValue += "|";
        } else {
            oldValue = "";
        }
        System.getProperties().put("java.protocol.handler.pkgs", oldValue + HANDLER_PKG);
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        // byte[] content = HdfsClassLoader.getEntryStream(
        // HdfsClassLoader.hdfsprocessJarContent, u.getFile());
        // 
        // if (content == null) {
        byte[] content = HdfsClassLoader.getEntryStream(HdfsClassLoader.hadoopcoreJarContent, u.getFile());
        if (content == null) {
            return null;
        }
        final byte[] jarContent = content;
        return new URLConnection(u) {

            @Override
            public void connect() throws IOException {
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(jarContent);
            }
        };
    }

    public static void main(String[] arg) throws Exception {
        URL url = new URL(null, "tsearcher:core-default.xml", new Handler());
        System.out.println(url.openStream());
    }
    // protected URLConnection openConnection(URL u) throws IOException {
    // return new Connection(u);
    // }
    // 
    // private static class Connection extends URLConnection {
    // Connection(URL u) {
    // super(u);
    // }
    // 
    // public void connect() {
    // }
    // 
    // public InputStream getInputStream() throws IOException {
    // 
    // // String jarEntryPath = StringUtils.substringBefore(url.getFile(),
    // // "!");
    // 
    // final String className = StringUtils.substringAfterLast(url
    // .getFile(), "!/");
    // 
    // InputStream classStream = getEntryStream(
    // "tsearch_hadoop_lib/yuti2hdfstransfer-terminator-hdfsprocess-1.0.1.jar",
    // className);
    // 
    // if (classStream != null) {
    // return classStream;
    // }
    // 
    // return getEntryStream(
    // "tsearch_hadoop_lib/hadoop-core-0.20.2-cdh3u1.jar",
    // className);
    // }
    // 
    // private InputStream getEntryStream(String jarEntryPath, String className)
    // throws IOException {
    // 
    // JarInputStream inputStream = new JarInputStream(Thread
    // .currentThread().getContextClassLoader()
    // .getResourceAsStream(jarEntryPath), false);
    // ZipEntry entry = null;
    // while ((entry = inputStream.getNextEntry()) != null) {
    // if (StringUtils.equals(entry.getName(), className)) {
    // return inputStream;
    // }
    // }
    // 
    // return null;
    // }
    // 
    // public OutputStream getOutputStream() throws IOException {
    // throw new UnknownServiceException("Output is not supported");
    // }
    // }
}

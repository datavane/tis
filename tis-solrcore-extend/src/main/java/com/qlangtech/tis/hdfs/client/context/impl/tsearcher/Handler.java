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

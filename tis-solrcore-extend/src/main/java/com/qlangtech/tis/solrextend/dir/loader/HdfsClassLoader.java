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
package com.qlangtech.tis.solrextend.dir.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.solrextend.utils.Assert;
import com.qlangtech.tis.hdfs.client.context.impl.tsearcher.Handler;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsClassLoader extends ClassLoader {

    public static final byte[] hadoopcoreJarContent;

    static {
        hadoopcoreJarContent = readJarContent("tsearch_hadoop_lib/hadoop-core-1.2.1.jar");
    }

    public HdfsClassLoader(ClassLoader parent) {
        super(parent);
    }

    private static byte[] readJarContent(String jarPath) {
        InputStream jarReader = null;
        try {
            jarReader = HdfsClassLoader.class.getClassLoader().getResourceAsStream(jarPath);
            Assert.assertNotNull("jarReader can not be null", jarReader);
            return IOUtils.toByteArray(jarReader);
        } catch (Exception e) {
            throw new RuntimeException("jarPath:" + jarPath, e);
        } finally {
            IOUtils.closeQuietly(jarReader);
        }
    }

    // 
    // @Override
    // public Class<?> loadClass(String name) throws ClassNotFoundException {
    // 
    // 
    // }
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("loadClass:" + name);
        return super.loadClass(name, resolve);
    // synchronized (getClassLoadingLock(name)) {
    // // First, check if the class has already been loaded
    // Class c = null;
    // if (!StringUtils.startsWith(name, "org.apache.hadoop.")) {
    // c = findLoadedClass(name);
    // }
    // 
    // if (c == null) {
    // 
    // if (c == null) {
    // // If still not found, then invoke findClass in order
    // // to find the class.
    // c = findClass(name);
    // 
    // }
    // }
    // if (resolve) {
    // resolveClass(c);
    // }
    // return c;
    // }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] content = null;
        try {
            content = getClassInputStream(name);
            if (content != null) {
                return this.defineClass(name, content, 0, content.length, null);
            } else {
                return this.getParent().loadClass(name);
            }
        } catch (Throwable e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        } finally {
        // IOUtils.closeQuietly(classStream);
        }
    }

    public URL getResource(String name) {
        try {
            URL url;
            url = getLocalResource(name);
            if (url == null) {
                url = super.getResource(name);
            }
            return url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // @Override
    // protected URL findResource(String name) {
    // 
    // // return super.findResource(name);
    // }
    private byte[] getClassInputStream(String name) throws IOException {
        final String // StringUtils.endsWith(name, ".class") ? name
        className = StringUtils.replace(name, ".", "/") + ".class";
        // }
        return getEntryStream(hadoopcoreJarContent, className);
    }

    private static final Handler localURLStreamHandler = new Handler();

    private URL getLocalResource(String name) throws IOException {
        if (exist(hadoopcoreJarContent, name)) {
            return new URL(null, "tsearcher:" + name, localURLStreamHandler);
        }
        return null;
    }

    private static boolean exist(byte[] jarEntryContent, String className) throws IOException {
        ByteArrayInputStream arrayStream = null;
        JarInputStream inputStream = null;
        try {
            arrayStream = new ByteArrayInputStream(jarEntryContent);
            inputStream = new JarInputStream(arrayStream, false);
            ZipEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (StringUtils.equals(entry.getName(), className)) {
                    return true;
                }
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(arrayStream);
        }
        return false;
    }

    public static byte[] getEntryStream(byte[] jarEntryContent, String className) throws IOException {
        JarInputStream inputStream = null;
        ByteArrayInputStream jarfileStream = null;
        try {
            jarfileStream = new ByteArrayInputStream(jarEntryContent);
            inputStream = new JarInputStream(jarfileStream, false);
            ZipEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (StringUtils.equals(entry.getName(), className)) {
                    return IOUtils.toByteArray(inputStream);
                }
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(jarfileStream);
        }
        return null;
    }
    // public static void main(String[] arg) throws Exception {
    // String targetClass =
    // "org/apache/hadoop/security/UserGroupInformation$HadoopLoginModule.class";
    // 
    // HdfsClassLoader loader = new HdfsClassLoader();
    // 
    // loader.loadClass(targetClass, false);
    // }
}

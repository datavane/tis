/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.indexbuilder.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.indexbuilder.HdfsIndexBuilder;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServiceClassLoader extends URLClassLoader {

    public static final Logger logger = LoggerFactory.getLogger(HdfsIndexBuilder.class);

    /**
     * @param array
     * @param parent
     */
    public ServiceClassLoader(URL[] array, ClassLoader parent) {
        super(array, parent);
    }

    // TODO Auto-generated constructor stub
    public static ServiceClassLoader loadJar(String jarFullName, String dest, ClassLoader parent) throws IOException {
        /*
		 * JarFile jarFile; try { jarFile = new JarFile(fileName); }
		 * catch(IOException io) { throw new
		 * IOException("Error opening job jar: " + fileName) .initCause(io); }
		 */
        File jarFile = new File(jarFullName);
        ArrayList<URL> classPath = new ArrayList<URL>();
        if (!jarFile.isDirectory()) {
            File tmpDir = new File(dest + File.separator + "unjar");
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            /*
			 * if (!tmpDir.isDirectory()) {
			 * System.err.println("Mkdirs failed to create " + tmpDir);
			 * System.exit(-1); }
			 */
            /*
			 * final File workDir = File.createTempFile("hadoop-unjar", "", tmpDir);
			 * workDir.delete(); workDir.mkdirs(); if (!workDir.isDirectory()) {
			 * System.err.println("Mkdirs failed to create " + workDir);
			 * System.exit(-1); }
			 */
            /*
			 * Runtime.getRuntime().addShutdownHook(new Thread() { public void run()
			 * { try { FileUtil.fullyDelete(workDir); } catch (IOException e) { } }
			 * });
			 */
            unJar(jarFile, tmpDir);
            classPath.add(new File(tmpDir + "/").toURL());
            classPath.add(jarFile.toURL());
            classPath.add(new File(tmpDir, "classes/").toURL());
            File[] libs = new File(tmpDir, "lib").listFiles();
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURL());
                }
            }
            logger.warn("user_classPath:" + classPath);
            // if (loader == null) {
            return new ServiceClassLoader(classPath.toArray(new URL[0]), parent);
        // }
        } else {
            classPath.add(new File(jarFile + "/").toURL());
            classPath.add(jarFile.toURL());
            // classPath.add(new File(jarFile, "classes/").toURL());
            File[] libs = jarFile.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    // TODO Auto-generated method stub
                    return !name.endsWith(".xml");
                }
            });
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURL());
                }
            }
            libs = jarFile.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    // TODO Auto-generated method stub
                    return name.endsWith(".xml");
                }
            });
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURL());
                }
            }
            logger.warn("user_classPath:" + classPath);
            // if (loader == null) {
            return new ServiceClassLoader(classPath.toArray(new URL[0]), parent);
        // }
        }
    }

    /**
     * Unpack a jar file into a directory.
     */
    public static void unJar(File jarFile, File toDir) throws IOException {
        JarFile jar = new JarFile(jarFile);
        try {
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream in = jar.getInputStream(entry);
                    try {
                        File file = new File(toDir, entry.getName());
                        if (!file.getParentFile().mkdirs()) {
                            if (!file.getParentFile().isDirectory()) {
                                throw new IOException("Mkdirs failed to create " + file.getParentFile().toString());
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        try {
                            byte[] buffer = new byte[8192];
                            int i;
                            while ((i = in.read(buffer)) != -1) {
                                out.write(buffer, 0, i);
                            }
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            jar.close();
        }
    }
}

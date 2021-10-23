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

package com.qlangtech.tis.compiler.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-20 16:59
 **/
public class FileObjectsContext {

    public static void traversingFiles(Stack<String> childPath, File parent, FileObjectsContext result, IProcessFile fileProcess) {
        if (parent == null || !parent.exists()) {
            throw new IllegalStateException("parent is not exist:" + parent.getAbsolutePath());
        }
        File child = null;
        for (String c : parent.list()) {
            child = new File(parent, c);
            if (child.isDirectory()) {
                childPath.push(c);
                try {
                    result.dirSet.add(childPath.stream().collect(Collectors.joining("/")));
                    traversingFiles(childPath, child, result, fileProcess);
                } finally {
                    childPath.pop();
                }
            } else {
                String zipPath = childPath.stream().collect(Collectors.joining("/"));
                fileProcess.process(zipPath, child);
            }
        }
    }

    public static FileObjectsContext getFileObjects(File sourceRootDir, SourceGetterStrategy sourceGetterStrategy) {
        final FileObjectsContext result = new FileObjectsContext();
        final Stack<String> childPath = new Stack<>();
        traversingFiles(childPath, new File(sourceRootDir, sourceGetterStrategy.childSourceDir), result, (zp, child) -> {
            String className;
            ZipPath zipPath;
            if (StringUtils.endsWith(child.getName(), sourceGetterStrategy.sourceCodeExtendsion)) {
                boolean isJavaSourceCode = sourceGetterStrategy.sourceCodeExtendsion.equals(JavaFileObject.Kind.SOURCE.extension);
                className = StringUtils.substringBefore(child.getName(), ".");
                // zipPath = new ZipPath(childPath.stream().collect(Collectors.joining("/")), className, //
                // isJavaSourceCode ? JavaFileObject.Kind.SOURCE : JavaFileObject.Kind.OTHER);// + ".class";
                zipPath = new //
                        ZipPath(//
                        zp, // + ".class";
                        className, isJavaSourceCode ? JavaFileObject.Kind.SOURCE : JavaFileObject.Kind.OTHER);
                result.classMap.put(childPath.stream().collect(Collectors.joining(".")) + "." + className, sourceGetterStrategy.processMyJavaFileObject(new MyJavaFileObject(child, zipPath, sourceGetterStrategy.getSourceKind(), isJavaSourceCode)));
            }
        });
        File resourceDir = new File(sourceRootDir, "resources");
        if (sourceGetterStrategy.getResource && resourceDir.exists()) {
            traversingFiles(childPath, resourceDir, result, (zp, child) -> {
                if (StringUtils.endsWith(child.getName(), ".xml")) {
                    // result.resources.add(new ResourcesFile(
                    // new ZipPath(childPath.stream().collect(Collectors.joining("/"))
                    // , child.getName(), JavaFileObject.Kind.OTHER), child));
                    result.resources.add(new ResourcesFile(new ZipPath(zp, child.getName(), JavaFileObject.Kind.OTHER), child));
                }
            });
        }
        return result;
    }

    /**
     * @param sourceDir       Jar包保存的位置
     * @param jarFileName     Jar包的名称
     * @param fileObjectsArry 需要打包的资源文件
     * @throws Exception
     */
    public static void packageJar(File sourceDir, String jarFileName, FileObjectsContext... fileObjectsArry) throws Exception {
        try {
            final Set<String> savedEntryPaths = Sets.newHashSet();
            // 开始打包
            try (JarOutputStream jaroutput = new JarOutputStream(FileUtils.openOutputStream(new File(sourceDir, jarFileName)))) {
                for (FileObjectsContext fileObjects : fileObjectsArry) {
                    // 添加文件夹entry
                    fileObjects.dirSet.stream().forEach((p) -> {
                        try {
                            JarEntry entry = new JarEntry(p + "/");
                            entry.setTime(System.currentTimeMillis());
                            if (savedEntryPaths.add(entry.getName())) {
                                jaroutput.putNextEntry(entry);
                                jaroutput.closeEntry();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                for (FileObjectsContext fileObjects : fileObjectsArry) {
                    // 添加class
                    for (IOutputEntry f : fileObjects.classMap.values()) {
                        // class 文件
                        if (f.containCompiledClass()) {
                            writeJarEntry(jaroutput, f);
                        }
                        // 添加.java文件
                        f.processSource(jaroutput);
                    }
                }
                for (FileObjectsContext fileObjects : fileObjectsArry) {
                    // 添加xml配置文件
                    for (ResourcesFile res : fileObjects.resources) {
                        JarEntry entry = new JarEntry(res.getZipPath().getFullPath());
                        if (!savedEntryPaths.add(entry.getName())) {
                            continue;
                        }
                        entry.setTime(System.currentTimeMillis());
                        byte[] data = FileUtils.readFileToByteArray(res.getFile());
                        entry.setSize(data.length);
                        CRC32 crc = new CRC32();
                        crc.update(data);
                        entry.setCrc(crc.getValue());
                        jaroutput.putNextEntry(entry);
                        jaroutput.write(data);
                        jaroutput.closeEntry();
                    }
                }
                jaroutput.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("jarFileName:" + jarFileName, e);
        }
    }

    private static void writeJarEntry(JarOutputStream jarOutput, IOutputEntry fileObj) throws IOException, FileNotFoundException {
        ZipPath zipPath = fileObj.getZipPath();
        JarEntry entry = new JarEntry(zipPath.getFullClassPath());
        entry.setTime(System.currentTimeMillis());
        byte[] data = fileObj.getOutputStream().toByteArray();
        entry.setSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        jarOutput.putNextEntry(entry);
        jarOutput.write(data);
        jarOutput.closeEntry();
    }

    public interface IProcessFile {

        public void process(String zipPath, File child);
    }

    public Map<String, IOutputEntry> /* class name */
            classMap = Maps.newHashMap();

    Set<String> dirSet = Sets.newHashSet();

    public List<ResourcesFile> resources = Lists.newArrayList();
}

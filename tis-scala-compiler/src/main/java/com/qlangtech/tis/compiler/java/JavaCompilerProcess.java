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
import com.qlangtech.tis.plugin.ds.DBConfig;
import com.qlangtech.tis.plugin.ds.IDbMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

// import com.taobao.terminator.db.parser.domain.*;

/**
 * 将自动生成出来的java类进行编译 https://blog.csdn.net/lmy86263/article/details/59742557
 * <br>
 * <p>
 * https://stackoverflow.com/questions/31289182/compile-scala-code-to-class-file-in-java
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月6日
 */
public class JavaCompilerProcess {

    private static final Logger logger = LoggerFactory.getLogger(JavaCompilerProcess.class);

    // public static final File rootDir = new File(
    // "D:\\j2ee_solution\\import_pj\\ibator_koubei\\ibator_koubei\\targett\\src\\main\\java");
    private final File sourceRootDir;

    private final File classpathDir;

    private final IDbMeta dbConfig;

    private final File sourceDir;

    public JavaCompilerProcess(IDbMeta dbConfig, File sourceDir, File classpathDir) {
        super();
        if (sourceDir == null || !sourceDir.exists()) {
            throw new IllegalArgumentException("param sourceDir can not be null");
        }
        this.sourceDir = sourceDir;
        this.sourceRootDir = new File(sourceDir, "src/main");
        this.classpathDir = classpathDir;
        if (dbConfig == null) {
            throw new IllegalStateException("param dbConfig can not be null");
        }
        this.dbConfig = dbConfig;
    }

    public static void main(String[] args) throws Exception {
        File rootDir = new File("D:\\j2ee_solution\\import_pj\\ibator_koubei\\ibator_koubei\\targett\\src\\main");
        File classpathDir = new File("D:/j2ee_solution/tis-ibatis/target/dependency/");
        DBConfig dbConfig = new DBConfig();
        dbConfig.setName("shop");
        JavaCompilerProcess compilerProcess = new JavaCompilerProcess(dbConfig, rootDir, classpathDir);
        compilerProcess.compileAndBuildJar();
        // JarFile jarFile = new JarFile(
        // "D:\\j2ee_solution\\mvn_repository\\com\\dfire\\tis\\tis-ibatis\\2.0\\tis-ibatis-2.0.jar");
        // JarEntry next = null;
        // Enumeration<JarEntry> entries = jarFile.entries();
        // InputStream input = null;
        // while (entries.hasMoreElements()) {
        // next = entries.nextElement();
        //
        // System.out.println(next.getName() + ",input is dir:" + next.isDirectory());
        //
        // }
        // jarFile.close();
    }

    /**
     * 编译加打包DAO层代码
     *
     * @throws Exception
     */
    public void compileAndBuildJar() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<? super JavaFileObject> collector = new DiagnosticCollector<>();
        FileObjectsContext fileObjects = getFileObjects(this.sourceRootDir, JAVA_GETTER);
        // 该JavaFileManager实例是com.sun.tools.javac.file.JavacFileManager
        JavaFileManager manager = new MyJavaFileManager(compiler.getStandardFileManager(collector, null, null), fileObjects.classMap);
        try {
            // TODO: 文件夹要做到可配置化
            // File classpathDir = new
            // File("D:/j2ee_solution/tis-ibatis/target/dependency/");
            File outdir = (new File(this.sourceRootDir, "out"));
            FileUtils.forceMkdir(outdir);
            // ......
            List<String> options = Lists.newArrayList();
            options.add("-classpath");
            this.setClasspath(options);
            options.add("-target");
            options.add("1.8");
            options.add("-d");
            options.add(outdir.getAbsolutePath());
            options.add("-nowarn");
            logger.info("javac options:{}", options.stream().collect(Collectors.joining(" ")));
            List<String> classes = Lists.newArrayList();
            // 在其他实例都已经准备完毕后, 构建编译任务, 其他实例的构建见如下
            // 
            CompilationTask compileTask = // 
                    compiler.getTask(//
                            new OutputStreamWriter(System.err), //
                            manager, //
                            collector, //
                            options, //
                            classes, //
                            fileObjects.classMap.values().stream().map((r) -> r.getFileObject()).collect(Collectors.toList()));
            compileTask.call();
            collector.getDiagnostics().forEach(item -> System.out.println(item.toString()));
            // final Set<String> zipDirSet = Sets.newHashSet();
            packageJar(this.sourceDir, this.dbConfig.getDAOJarName(), fileObjects);
        } finally {
            try {
                manager.close();
            } catch (Throwable e) {
            }
        }
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
                        JarEntry entry = new JarEntry(res.zipPath.getFullPath());
                        if (!savedEntryPaths.add(entry.getName())) {
                            continue;
                        }
                        entry.setTime(System.currentTimeMillis());
                        byte[] data = FileUtils.readFileToByteArray(res.file);
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

    private void setClasspath(List<String> options) {
        if (classpathDir != null) {
            if (!classpathDir.exists()) {
                throw new IllegalStateException("path:" + classpathDir.getAbsolutePath() + " is not exist");
            }
            List<File> jars = Lists.newArrayList();
            for (String c : classpathDir.list()) {
                jars.add(new File(classpathDir, c));
            }
            options.add(jars.stream().map((r) -> r.getAbsolutePath()).collect(Collectors.joining(SystemUtils.IS_OS_UNIX ? ":" : ";")));
        } else {
            options.add(System.getProperty("java.class.path"));
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

    public static class SourceGetterStrategy {

        private final boolean getResource;

        private final String childSourceDir;

        private final String sourceCodeExtendsion;

        public SourceGetterStrategy(boolean getResource, String childSourceDir, String sourceCodeExtendsion) {
            this.getResource = getResource;
            this.childSourceDir = childSourceDir;
            this.sourceCodeExtendsion = sourceCodeExtendsion;
        }

        public MyJavaFileObject processMyJavaFileObject(MyJavaFileObject fileObj) {
            return fileObj;
        }

        public JavaFileObject.Kind getSourceKind() {
            return JavaFileObject.Kind.SOURCE;
        }
    }

    private static final SourceGetterStrategy // 
            JAVA_GETTER = new SourceGetterStrategy(true, "java", ".java");

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

    public interface IProcessFile {

        public void process(String zipPath, File child);
    }

    public static class FileObjectsContext {

        public Map<String, IOutputEntry> /* class name */
                classMap = Maps.newHashMap();

        Set<String> dirSet = Sets.newHashSet();

        public List<ResourcesFile> resources = Lists.newArrayList();
    }

    public static class ZipPath {

        private final String parentPath;

        private final String entryName;

        private final JavaFileObject.Kind sourceKind;

        public ZipPath(String parentPath, String entryName, JavaFileObject.Kind sourceKind) {
            super();
            this.parentPath = parentPath;
            this.entryName = entryName;
            this.sourceKind = sourceKind;
        }

        public String getFullSourcePath() {
            // + JavaFileObject.Kind.CLASS.extension;
            StringBuffer result = new StringBuffer(getFullPath());
            if (sourceKind == JavaFileObject.Kind.CLASS) {
                result.append(JavaFileObject.Kind.CLASS.extension);
            } else if (sourceKind == JavaFileObject.Kind.SOURCE) {
                result.append(JavaFileObject.Kind.SOURCE.extension);
            } else if (sourceKind == JavaFileObject.Kind.OTHER) {
                result.append(".scala");
            } else {
                throw new IllegalStateException("source kind:" + this.sourceKind + " is illegal");
            }
            return result.toString();
        }

        public String getFullClassPath() {
            return getFullPath() + JavaFileObject.Kind.CLASS.extension;
        }

        // 
        // public String getFullJavaPath() {
        // return getFullPath() + JavaFileObject.Kind.SOURCE.extension;
        // }
        // 
        // public String getFullScalaPath() {
        // return getFullPath() + ".scala";
        // }
        public String getFullPath() {
            return parentPath + "/" + this.entryName;
        }

        public String getParentPath() {
            return this.parentPath;
        }

        public String getEntryName() {
            return this.entryName;
        }
    }

    public static class ResourcesFile {

        private final ZipPath zipPath;

        private final File file;

        public ResourcesFile(ZipPath zipPath, File file) {
            super();
            this.zipPath = zipPath;
            this.file = file;
        }
    }
}

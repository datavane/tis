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

package com.qlangtech.tis.compiler.streamcode;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.compiler.incr.ICompileAndPackage;
import com.qlangtech.tis.compiler.java.*;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.sql.parser.IDBNodeMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import scala.tools.ScalaCompilerSupport;
import scala.tools.scala_maven_executions.LogProcessorUtils;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-20 16:35
 **/
public class CompileAndPackage implements ICompileAndPackage {

    /**
     * @param context
     * @param msgHandler
     * @param appName
     * @param dbNameMap
     * @param sourceRoot
     * @param xmlConfigs 取得spring配置文件相关resourece
     * @throws Exception
     */
    @Override
    public void process(Context context, IControlMsgHandler msgHandler
            , String appName, Map<IDBNodeMeta, List<String>> dbNameMap, File sourceRoot, FileObjectsContext xmlConfigs) throws Exception {
        if (xmlConfigs == null) {
            throw new IllegalArgumentException("param xmlConfigs can not be null");
        }
        if (StringUtils.isEmpty(appName)) {
            throw new IllegalArgumentException("param appName can not be null");
        }
        /**
         * *********************************************************************************
         * 编译增量脚本
         * ***********************************************************************************
         */
        if (this.streamScriptCompile(sourceRoot, dbNameMap.keySet())) {
            msgHandler.addErrorMessage(context, "增量脚本编译失败");
            msgHandler.addFieldError(context, "incr_script_compile_error", "error");
            return;
        }
        /**
         * *********************************************************************************
         * 对scala代码进行 打包
         * ***********************************************************************************
         */
        JavaCompilerProcess.SourceGetterStrategy getterStrategy
                = new JavaCompilerProcess.SourceGetterStrategy(false, "/src/main/scala", ".scala") {

            @Override
            public JavaFileObject.Kind getSourceKind() {
                // 没有scala的类型，暂且用other替换一下
                return JavaFileObject.Kind.OTHER;
            }

            @Override
            public MyJavaFileObject processMyJavaFileObject(MyJavaFileObject fileObj) {
                try {
                    try (InputStream input = FileUtils.openInputStream(fileObj.getSourceFile())) {
                        IOUtils.copy(input, fileObj.openOutputStream());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return fileObj;
            }
        };
        //
        FileObjectsContext fileObjects = JavaCompilerProcess.getFileObjects(sourceRoot, getterStrategy);
        final FileObjectsContext compiledCodeContext = new FileObjectsContext();
        File streamScriptClassesDir = new File(sourceRoot, "classes");
        appendClassFile(streamScriptClassesDir, compiledCodeContext, null);


        // 将stream code打包
        // indexStreamCodeGenerator.getAppDomain().getAppName() + "-incr.jar"
        JavaCompilerProcess.packageJar(// indexStreamCodeGenerator.getAppDomain().getAppName() + "-incr.jar"
                sourceRoot, StreamContextConstant.getIncrStreamJarName(appName)
                , fileObjects, compiledCodeContext, xmlConfigs);
    }

    private boolean streamScriptCompile(File sourceRoot, Set<IDBNodeMeta> dependencyDBNodes) throws Exception {
        LogProcessorUtils.LoggerListener loggerListener = new LogProcessorUtils.LoggerListener() {

            @Override
            public void receiveLog(LogProcessorUtils.Level level, String line) {
                System.err.println(line);
            }
        };
        return ScalaCompilerSupport.streamScriptCompile(sourceRoot, IDBNodeMeta.appendDBDependenciesClasspath(dependencyDBNodes), loggerListener);
    }

    private void appendClassFile(File parent, FileObjectsContext fileObjects, final StringBuffer qualifiedClassName) throws IOException {
        String[] children = parent.list();
        File childFile = null;
        for (String child : children) {
            childFile = new File(parent, child);
            if (childFile.isDirectory()) {
                StringBuffer newQualifiedClassName = null;
                if (qualifiedClassName == null) {
                    newQualifiedClassName = new StringBuffer(child);
                } else {
                    newQualifiedClassName = (new StringBuffer(qualifiedClassName)).append(".").append(child);
                }
                appendClassFile(childFile, fileObjects, newQualifiedClassName);
            } else {
                final String className = StringUtils.substringBeforeLast(child, ".");
                //
                NestClassFileObject fileObj = MyJavaFileManager.getNestClassFileObject(
                        ((new StringBuffer(qualifiedClassName)).append(".").append(className)).toString(), fileObjects.classMap);
                try (InputStream input = FileUtils.openInputStream(childFile)) {
                    IOUtils.copy(input, fileObj.openOutputStream());
                }
            }
        }
    }

}

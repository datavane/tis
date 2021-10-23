/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.qlangtech.tis.compiler.java;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月6日
 */
public class MyJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, IOutputEntry> fileObjects;

    protected MyJavaFileManager(JavaFileManager fileManager, Map<String, IOutputEntry> fileObject) {
        super(fileManager);
        this.fileObjects = fileObject;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        IOutputEntry javaFileObject = fileObjects.get(className);
        if (javaFileObject == null) {
            super.getJavaFileForInput(location, className, kind);
        }
        return javaFileObject.getFileObject();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location
            , String qualifiedClassName, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        IOutputEntry javaObj = fileObjects.get(qualifiedClassName);
        if (javaObj != null) {
            return javaObj.getFileObject();
        }
        NestClassFileObject fileObj = NestClassFileObject.getNestClassFileObject(qualifiedClassName, this.fileObjects);
        return fileObj;
    }

}

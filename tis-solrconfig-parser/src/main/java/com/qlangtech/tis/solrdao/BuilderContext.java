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
package com.qlangtech.tis.solrdao;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BuilderContext implements IBuilderContext {

    private String appName;

    private String pojoName;

    private String targetNameSpace;

    private String serverAddress;

    protected String targetDir;

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getAppName() {
        return this.appName;
    }

    public File getTargetPackageDir() {
        return new File(new File(this.getTargetDir()), StringUtils.replace(this.targetNameSpace, ".", "/"));
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getTargetNameSpace() {
        return targetNameSpace;
    }

    public void setTargetNameSpace(String targetNameSpace) {
        this.targetNameSpace = targetNameSpace;
    }

    public String getPojoName() {
        if (StringUtils.isEmpty(this.pojoName)) {
            return StringUtils.capitalize(StringUtils.substringAfter(appName, "search4"));
        }
        return pojoName;
    }

    public void setPojoName(String pojoName) {
        this.pojoName = pojoName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    
    public InputStream getResourceInputStream() throws Exception {
        URL url = new URL("http://" + getServerAddress() + ":8080/terminator-search/" + getAppName() + "-0/admin/file/?contentType=text/xml;charset=utf-8&file=schema.xml");
        System.out.println(url);
        InputStream reader = url.openStream();
        return reader;
    }

    
    public Writer getOutputStream() throws Exception {
        return new OutputStreamWriter(FileUtils.openOutputStream(this.getNewFileName(), false));
    }

    private File getNewFileName() {
        File outputDir = this.getTargetPackageDir();
        return new File(outputDir, this.getPojoName() + ".java");
    }

    public void closeWriter(PrintWriter writer) {
        IOUtils.closeQuietly(writer);
    }
}

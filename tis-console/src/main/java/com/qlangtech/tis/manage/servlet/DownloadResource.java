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
package com.qlangtech.tis.manage.servlet;

import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_SCHEMA;
import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_SOLOR;

import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;

/*
 * 在服务器端仓库中代理客户端向仓库请求
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DownloadResource {

    // private final AppPackage pack;
    private final Application application;

    private final SnapshotDomain snapshot;

    private final String resourceName;

    // private final ConfigFileReader fileReader;
    private final PropteryGetter getStrategy;

   // public static final String JAR_NAME = "jar";

    public DownloadResource(Application application, SnapshotDomain snapshot, String resourceName) {
        super();
        // this.pack = pack;
        this.application = application;
        this.snapshot = snapshot;
        this.resourceName = resourceName;
        // this.fileReader = createConfigFileReader(this);
        this.getStrategy = getGotStrategy(this);
    }

    public static final String XML_CONTENT_TYPE = "text/xml";

    public static final String JAR_CONTENT_TYPE = "application/zip";

    public String getContentType() {
        //return JAR_NAME.equals(resourceName) ? JAR_CONTENT_TYPE :
        return 	XML_CONTENT_TYPE;
    }

    public int getFileLength() {
        return this.getStrategy.getContent(snapshot).length;
    // return (int) fileReader.getFile(this.getStrategy).length();
    }

    public String getFileName() {
        return this.getStrategy.getFileName();
    }

    public String getMd5CodeValue() {
        return getStrategy.getMd5CodeValue(this.snapshot);
    }

    public byte[] read() throws Exception {
        return this.getStrategy.getContent(snapshot);
    }

    // public byte[] read() throws Exception {
    // // return fileReader.read(this.getStrategy);
    // 
    // }
    // private static ConfigFileReader createConfigFileReader(
    // DownloadResource downloadRes) {
    // final ConfigFileReader reader = new ConfigFileReader(downloadRes
    // .getSnapshot(), ConfigFileReader.getAppDomainDir(Config
    // .getLocalRepository(), downloadRes.getApplication().getBizId(),
    // downloadRes.getApplication().getAppId()));
    // return reader;
    // }
    private static PropteryGetter getGotStrategy(DownloadResource resource) {
        final String resourceName = resource.getResourceName();
//        if (JAR_NAME.equalsIgnoreCase(resourceName)) {
//            // ConfigFileReader.createJarGetter(resource.getPack());
//            return FILE_JAR;
//        }
        
        if (FILE_SOLOR.getFileName().equals(resourceName)) {
            return FILE_SOLOR;
        }
        if (FILE_SCHEMA.getFileName().equals(resourceName)) {
            return FILE_SCHEMA;
        }
        throw new IllegalArgumentException("resourceName:" + resourceName + " has not match any file pattern");
    }

    // public AppPackage getPack() {
    // return pack;
    // }
    public Application getApplication() {
        return application;
    }

    // public Snapshot getSnapshot() {
    // return snapshot;
    // }
    public String getResourceName() {
        return resourceName;
    }
}

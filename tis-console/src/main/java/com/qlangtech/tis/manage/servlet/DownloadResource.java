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
package com.qlangtech.tis.manage.servlet;

import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_SCHEMA;
import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_SOLR;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;

/**
 * 在服务器端仓库中代理客户端向仓库请求
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-30
 */
public class DownloadResource {

    // private final AppPackage pack;
    private final Application application;

    private final SnapshotDomain snapshot;

    private final String resourceName;

    // private final ConfigFileReader fileReader;
    private final PropteryGetter getStrategy;

    public static final String JAR_NAME = "jar";

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
        return JAR_NAME.equals(resourceName) ? JAR_CONTENT_TYPE : XML_CONTENT_TYPE;
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
        // }
        if (FILE_SOLR.getFileName().equals(resourceName)) {
            return FILE_SOLR;
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

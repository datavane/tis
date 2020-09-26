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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-26
 */
public class SnapshotDomain {

    private UploadResource application = new UploadResource();

    private UploadResource coreProp = new UploadResource();

    private UploadResource datasource = new UploadResource();

    private UploadResource jarFile = new UploadResource();

    private UploadResource solrSchema = new UploadResource();

    private UploadResource solrConfig = new UploadResource();

    private final Snapshot snapshot;

    public SnapshotDomain() {
        super();
        snapshot = new Snapshot();
    }

    public SnapshotDomain(Snapshot snapshot) {
        super();
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot can not be null");
        }
        this.snapshot = snapshot;
    }

    // public Integer getPackId() {
    // return snapshot.getPid();
    // }
    public Integer getAppId() {
        return snapshot.getAppId();
    }

    // public void setSnapshot(Snapshot snapshot) {
    // this.snapshot = snapshot;
    // }
    public Snapshot getSnapshot() {
        if (this.snapshot == null) {
            throw new NullPointerException("this.snapshot can not be null");
        }
        return snapshot;
    }

    public void setApplication(UploadResource application) {
        this.application = application;
    }

    public void setCoreProp(UploadResource coreProp) {
        this.coreProp = coreProp;
    }

    public void setDatasource(UploadResource datasource) {
        this.datasource = datasource;
    }

    public void setJarFile(UploadResource jarFile) {
        this.jarFile = jarFile;
    }

    public void setSolrSchema(UploadResource solrSchema) {
        this.solrSchema = solrSchema;
    }

    public void setSolrConfig(UploadResource solrConfig) {
        this.solrConfig = solrConfig;
    }

    public UploadResource getApplication() {
        return application;
    }

    public UploadResource getCoreProp() {
        return coreProp;
    }

    public UploadResource getDatasource() {
        return datasource;
    }

    public UploadResource getJarFile() {
        return jarFile;
    }

    public UploadResource getSolrSchema() {
        return solrSchema;
    }

    public UploadResource getSolrConfig() {
        return solrConfig;
    }
}

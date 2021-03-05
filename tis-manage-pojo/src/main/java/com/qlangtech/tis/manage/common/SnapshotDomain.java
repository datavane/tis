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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-26
 */
public class SnapshotDomain {

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


    /**
     * @param
     * @param coreName
     * @return
     * @throws
     */
    public void writeResource2fs(ITISFileSystem fs, String coreName, PropteryGetter getter) {
        String path = getter.getFsPath(fs, coreName);// fs.getRootDir() + "/" + coreName + "/config/" + getter.getFileName();
        IPath dst = fs.getPath(path);
        if (dst == null) {
            throw new IllegalStateException("path can not be create:" + path);
        }
        OutputStream dstoutput = null;
        try {
            dstoutput = fs.create(dst, true);
            IOUtils.write(getter.getContent(this), dstoutput);
        } catch (IOException e1) {
            throw new RuntimeException("[ERROR] Submit Service Core  Schema.xml to HDFS Failure !!!!", e1);
        } finally {
            IOUtils.closeQuietly(dstoutput);
        }
    }

    public Integer getAppId() {
        return snapshot.getAppId();
    }

    public Snapshot getSnapshot() {
        if (this.snapshot == null) {
            throw new NullPointerException("this.snapshot can not be null");
        }
        return snapshot;
    }

    public void setSolrSchema(UploadResource solrSchema) {
        this.solrSchema = solrSchema;
    }

    public void setSolrConfig(UploadResource solrConfig) {
        this.solrConfig = solrConfig;
    }


    public UploadResource getSolrSchema() {
        return solrSchema;
    }

    public UploadResource getSolrConfig() {
        return solrConfig;
    }
}

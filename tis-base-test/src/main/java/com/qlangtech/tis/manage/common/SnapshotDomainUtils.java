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

import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-04 17:39
 */
public class SnapshotDomainUtils {

    public static SnapshotDomain mockEmployeeSnapshotDomain() throws Exception {

        SnapshotDomain snapshotDomain = new SnapshotDomain();

        try (InputStream i = SnapshotDomainUtils.class.getResourceAsStream("search4employee-schema")) {
            Objects.requireNonNull(i, "schema stream can not be null");
            UploadResource schema = new UploadResource();
            schema.setContent(IOUtils.toByteArray(i));
            schema.setMd5Code(ConfigFileReader.md5file(schema.getContent()));
            snapshotDomain.setSolrSchema(schema);
        }

        try (InputStream i = SnapshotDomainUtils.class.getResourceAsStream("search4employee-solrconfig")) {
            Objects.requireNonNull(i, "solrconfig stream can not be null");
            UploadResource solrCfg = new UploadResource();
            solrCfg.setContent(IOUtils.toByteArray(i));
            solrCfg.setMd5Code(ConfigFileReader.md5file(solrCfg.getContent()));
            snapshotDomain.setSolrConfig(solrCfg);
        }


//        String collectionName = ITestDumpCommon.INDEX_COLLECTION;// ITestDumpCommon.INDEX_COLLECTION;
//        SnapshotDomain snapshotDomain = HttpConfigFileReader.getResource(
//                collectionName, 21137, RunEnvironment.getSysRuntime(), ConfigFileReader.FILE_SCHEMA, ConfigFileReader.FILE_SOLR);
//        System.out.println(snapshotDomain);
        return snapshotDomain;
    }

//    public static void main(String[] args) throws Exception {
//        mockEmployeeSnapshotDomain();
//    }
}

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
package com.qlangtech.tis;

import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.RepositoryException;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestGetAppconfig extends TestCase {

    public void testGetResource() {
        try {
            SnapshotDomain domain = HttpConfigFileReader.getResource("search4punish", 0, RunEnvironment.DAILY, ConfigFileReader.FILE_SCHEMA);
            System.out.println(domain.getSnapshot());
            UploadResource resource = domain.getSolrSchema();
            Assert.assertNotNull(resource);
            Assert.assertNotNull(resource.getContent());
            System.out.println(new String(resource.getContent()));
            resource = domain.getSolrConfig();
            Assert.assertNotNull(resource);
            Assert.assertNull(resource.getContent());
        } catch (RepositoryException e) {
            Assert.assertFalse(e.getMessage(), true);
        }
    }
}

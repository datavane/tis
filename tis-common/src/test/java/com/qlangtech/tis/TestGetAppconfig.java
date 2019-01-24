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
package com.qlangtech.tis;

import junit.framework.Assert;
import junit.framework.TestCase;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.common.TerminatorRepositoryException;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestGetAppconfig extends TestCase {

    public void testGetResource() {
        try {
            SnapshotDomain domain = HttpConfigFileReader.getResource("http://terminator.admin.taobao.org:9999", "search4punish", 0, RunEnvironment.DAILY, ConfigFileReader.FILE_SCHEMA);
            System.out.println(domain.getSnapshot());
            UploadResource resource = domain.getSolrSchema();
            Assert.assertNotNull(resource);
            Assert.assertNotNull(resource.getContent());
            System.out.println(new String(resource.getContent()));
            resource = domain.getSolrConfig();
            Assert.assertNotNull(resource);
            Assert.assertNull(resource.getContent());
        } catch (TerminatorRepositoryException e) {
            Assert.assertFalse(e.getMessage(), true);
        }
    }
}

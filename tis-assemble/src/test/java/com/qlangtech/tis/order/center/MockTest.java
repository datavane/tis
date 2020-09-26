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
package com.qlangtech.tis.order.center;

import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.offline.FileSystemFactory;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-23 16:38
 */
public class MockTest extends TestCase {

    public void testMock() {
        final String fsPath = "/user/admin/test";
        FileSystemFactory indexBuilderFileSystemFactory = EasyMock.createMock("indexBuildFileSystem", FileSystemFactory.class);
        ITISFileSystem tisFileSystem = EasyMock.createMock("tisFileSystem", ITISFileSystem.class);
        EasyMock.expect(tisFileSystem.getName()).andReturn("mocktest");
        IPath path = EasyMock.createMock("mockpath", IPath.class);
        EasyMock.expect(tisFileSystem.getPath(fsPath)).andReturn(path);
        EasyMock.expect(indexBuilderFileSystemFactory.getFileSystem()).andReturn(tisFileSystem);
        EasyMock.replay(indexBuilderFileSystemFactory, tisFileSystem, path);
        ITISFileSystem fs = indexBuilderFileSystemFactory.getFileSystem();
        assertEquals("mocktest", fs.getName());
        assertNotNull(fs.getPath(fsPath));
    }
}

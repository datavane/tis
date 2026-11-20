/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.order.center;

import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.offline.FileSystemFactory;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-23 16:38
 */
public class MockTest {

    @Test
    public void testMock() {
        final String fsPath = "/user/admin/test";
        FileSystemFactory indexBuilderFileSystemFactory = Mockito.mock(FileSystemFactory.class, Mockito.withSettings().name("indexBuildFileSystem"));
        ITISFileSystem tisFileSystem = Mockito.mock(ITISFileSystem.class, Mockito.withSettings().name("tisFileSystem"));
        Mockito.when(tisFileSystem.getName()).thenReturn("mocktest");
        IPath path = Mockito.mock(IPath.class, Mockito.withSettings().name("mockpath"));
        Mockito.when(tisFileSystem.getPath(fsPath)).thenReturn(path);
        Mockito.when(indexBuilderFileSystemFactory.getFileSystem()).thenReturn(tisFileSystem);
        ITISFileSystem fs = indexBuilderFileSystemFactory.getFileSystem();
        assertEquals("mocktest", fs.getName());
        assertNotNull(fs.getPath(fsPath));

        Mockito.verify(indexBuilderFileSystemFactory).getFileSystem();
        Mockito.verify(tisFileSystem).getName();
        Mockito.verify(tisFileSystem).getPath(fsPath);
    }
}

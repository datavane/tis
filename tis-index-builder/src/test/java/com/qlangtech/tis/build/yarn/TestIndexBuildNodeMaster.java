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
package com.qlangtech.tis.build.yarn;

import com.google.common.collect.Lists;
import com.qlangtech.tis.fs.*;
import com.qlangtech.tis.indexbuilder.merger.LuceneIndexFileNameFilter;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.FileSystemFactory;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import junit.framework.TestCase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-25 14:31
 */
public class TestIndexBuildNodeMaster extends TestCase {

    static final File tmpDir;

    static {
        tmpDir = new File("/tmp/tis");
        Config.setDataDir(tmpDir.getAbsolutePath());
    }

    @Override
    protected void setUp() throws Exception {
        this.clearMocks();
    }

    public void testIndexBuild() throws Exception {
        CommandLine commandLine = mock("commandLine", CommandLine.class);
        //
        String[] params = null;
        try (InputStream paramsFile = TestIndexBuildNodeMaster.class.getResourceAsStream("totalpay-trigger-params.txt")) {
            params = StringUtils.split(IOUtils.toString(paramsFile, TisUTF8.get()), " ");
        }
        assertNotNull(params);
        assertTrue(params.length > 0);
        for (int i = 0; i < params.length; i = i + 2) {
            EasyMock.expect(commandLine.getOptionValue(StringUtils.substring(params[i], 1))).andReturn(params[i + 1]).anyTimes();
        }
        FileSystemFactory fileSystemFactory = mock("indexBuildFileSystemFactory", FileSystemFactory.class);
        ITISFileSystem fileSystem = mock("fileSystem", ITISFileSystem.class);
        EasyMock.expect(fileSystemFactory.getFileSystem()).andReturn(fileSystem).anyTimes();
        File tmpMetaDir = new File(tmpDir, "tmp");
        IPath remoteSchemaPath = mock("remoteSchemaPath", IPath.class);
        EasyMock.expect(fileSystem.getPath("/user/admin/search4totalpay-0/config/schema.xml")).andReturn(remoteSchemaPath);
        fileSystem.copyToLocalFile(remoteSchemaPath, tmpMetaDir);
        try (InputStream schema = TestIndexBuildNodeMaster.class.getResourceAsStream("totalpay-schema.xml")) {
            FileUtils.copyToFile(schema, new File(tmpMetaDir, "schema.xml"));
        }
        IPath remoteSolrConfigPath = mock("remoteSolrConfigPath", IPath.class);
        EasyMock.expect(fileSystem.getPath("/user/admin/search4totalpay-0/config/solrconfig.xml")).andReturn(remoteSolrConfigPath);
        fileSystem.copyToLocalFile(remoteSolrConfigPath, tmpMetaDir);
        IPath totalpaySummarySource = mock("totalpaySummarySourcePath", IPath.class);
        EasyMock.expect(fileSystem.getPath("/user/admin/tis/totalpay_summary/pt=20200525134425/pmod=0")).andReturn(totalpaySummarySource);
        IFileSplit fileSplit = mock("fileSplitor", IFileSplit.class);
        EasyMock.expect(fileSplit.getStart()).andReturn(0l);
        EasyMock.expect(fileSplit.getLength()).andReturn(100l);
        IPath fileSplitorPath = mock("fileSplitorPath", IPath.class);
        MockFileSplitor fileSplitor = new MockFileSplitor(fileSplit);
        EasyMock.expect(fileSplit.getPath()).andReturn(fileSplitorPath).anyTimes();
        EasyMock.expect(fileSystem.getSplitor(totalpaySummarySource)).andReturn(fileSplitor);
        FSDataInputStream fileSplitorPathoutStream = mock("fileSplitorPathoutStream", FSDataInputStream.class);
        EasyMock.expect(fileSystem.open(fileSplitorPath)).andReturn(fileSplitorPathoutStream);
        byte[] tmpContent = new byte[8192];
        EasyMock.expect(fileSplitorPathoutStream.read(tmpContent, 0, tmpContent.length)).andReturn(-1);
        IPath indexStorePath = mock("indexStorePath", IPath.class);
        EasyMock.expect(fileSystem.getPath("/user/admin/search4totalpay/all/0/output/20200525134425")).andReturn(indexStorePath).anyTimes();
        EasyMock.expect(fileSystem.exists(indexStorePath)).andReturn(true);
        EasyMock.expect(fileSystem.mkdirs(indexStorePath)).andReturn(true);
        IPath indexStorePathIndex0 = mock("indexStorePath_index_0", IPath.class);
        EasyMock.expect(fileSystem.getPath("/user/admin/search4totalpay/all/0/output/20200525134425/index/0")).andReturn(indexStorePathIndex0);
        EasyMock.expect(fileSystem.exists(indexStorePathIndex0)).andReturn(true);
        IPathInfo indexStorePath_index_0_pathinfo = mock("indexStorePath_index_0_pathinfo", IPathInfo.class);
        EasyMock.expect(fileSystem.getFileInfo(indexStorePathIndex0)).andReturn(indexStorePath_index_0_pathinfo).times(2);
        EasyMock.expect(indexStorePath_index_0_pathinfo.isDir()).andReturn(true).times(2);
        EasyMock.expect(fileSystem.listChildren(indexStorePathIndex0, LuceneIndexFileNameFilter.getFilter())).andReturn(Collections.emptyList());
        IPath segments_l = mock("segments_l", IPath.class);
        EasyMock.expect(fileSystem.getPath(indexStorePathIndex0, "segments_l")).andReturn(segments_l);
        EasyMock.expect(fileSystem.exists(segments_l)).andReturn(false);
        EasyMock.expect(segments_l.getName()).andReturn("segments_l").anyTimes();
        // mock("segments_l_output", TISFSDataOutputStream.class);
        TISFSDataOutputStream segments_l_output = new MockFSDataOutputStream(null);
        EasyMock.expect(fileSystem.create(segments_l, true, 4096)).andReturn(segments_l_output);
        IPathInfo indexStorePathChildInfo = mock("indexStorePathChild", IPathInfo.class);
        EasyMock.expect(fileSystem.listChildren(indexStorePath)).andReturn(Collections.singletonList(indexStorePathChildInfo));
        IPath indexStorePathChildPath = mock("indexStorePathChildPath", IPath.class);
        EasyMock.expect(indexStorePathChildInfo.getPath()).andReturn(indexStorePathChildPath);
        EasyMock.expect(fileSystem.delete(indexStorePathChildPath, true)).andReturn(true);
        IndexBuilderTriggerFactory factory = new DefaultIndexBuilderTriggerFactory(fileSystemFactory);
        IndexBuildNodeMaster master = new IndexBuildNodeMaster(factory);
        replay();
        // FIXME zkClient,statusRpc can not be null
        master.run(commandLine, null, null);
        verifyAll();
    }

    private void clearMocks() {
        mocks.clear();
    }

    private void verifyAll() {
        mocks.forEach((r) -> {
            EasyMock.verify(r);
        });
    }

    private static List<Object> mocks = Lists.newArrayList();

    public <T> T mock(String name, Class<?> toMock) {
        Object mock = EasyMock.createMock(name, toMock);
        mocks.add(mock);
        return (T) mock;
    }

    public void replay() {
        mocks.forEach((r) -> {
            EasyMock.replay(r);
        });
    }
}

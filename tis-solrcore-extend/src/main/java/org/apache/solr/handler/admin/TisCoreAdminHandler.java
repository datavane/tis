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
package org.apache.solr.handler.admin;

import com.google.common.collect.Lists;
import com.qlangtech.tis.cloud.ICoreAdminAction;
import com.qlangtech.tis.fs.*;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.RepositoryException;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.manage.common.TISCollectionUtils.TisCoreName;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.solrextend.cloud.TisSolrResourceLoader;
import com.qlangtech.tis.solrextend.utils.TisIndexFetcher;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.NoMergePolicy;
import org.apache.lucene.store.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.store.blockcache.CustomBufferedIndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisCoreAdminHandler extends CoreAdminHandler {

    private static final Logger logger = LoggerFactory.getLogger(TisCoreAdminHandler.class);

    // public static final String HDFS_HOST = "hdfs_host";
    public static final String HDFS_TIMESTAMP = "hdfs_timestamp";

    private static final String CORE_RELOAD_SLEEP_TIME = "core_reload_sleep_time";


    public static final String KEY_INDEX_BACK_FLOW_STATUS = "index_back_flow_status";

    public static final Pattern INDEX_DATA_PATTERN = Pattern.compile("index(\\d{14})(_(\\d+))?");

    /**
     * @param coreContainer
     */
    public TisCoreAdminHandler(CoreContainer coreContainer) {
        super(coreContainer);
    }

    // @Override
    // protected void preCoreAdminHandlerExecute(SolrQueryRequest req, SolrQueryResponse rsp, CoreAdminOperation op) {
    // if (CoreAdminOperation.CREATE_OP == op) {
    // handleCreateAction(req, rsp);
    // }
    // }

    /**
     * Helper method to remove a task from a tracking map.
     */
    private void removeRunningTask(String taskId) {
        synchronized (getRequestStatusMap(RUNNING)) {
            getRequestStatusMap(RUNNING).remove(taskId);
        }
    }

    /**
     *
     */
    @Override
    protected void handleCustomAction(SolrQueryRequest req, SolrQueryResponse rsp) {

        // baisui add 2019/01/22
        SolrParams solrParams = req.getParams();
        String action = solrParams.get(ICoreAdminAction.EXEC_ACTION);
        try {
            if (StringUtils.equals(ICoreAdminAction.ACTION_UPDATE_CONFIG, action)) {
                // 更新配置文件
                this.updateConfig(req, rsp);
                return;
            }
            // baisui add end

            //SolrParams solrParams = req.getParams();
            //String action = solrParams.get("exec" + CoreAdminParams.ACTION);
            if (StringUtils.equals(ICoreAdminAction.ACTION_SWAP_INDEX_FILE, action)) {
                final String taskId = req.getParams().get(CommonAdminParams.ASYNC);
                final TaskObject taskObject = getRequestStatusMap(RUNNING).get(taskId);

                parallelExecutor.execute(() -> {
                    boolean exceptionCaught = false;
                    try {
                        // 执行替换全量的流程
                        this.handleSwapindexfileAction(taskObject, req, rsp);
                        taskObject.setRspObject(rsp);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        exceptionCaught = true;
                        taskObject.setRspObjectFromException(e);
                    } finally {
                        removeRunningTask(taskObject.taskId);
                        if (exceptionCaught) {
                            addTask(FAILED, taskObject, true);
                        } else {
                            addTask(COMPLETED, taskObject, true);
                        }
                    }
                });

                return;
            }
            throw new IllegalArgumentException("param exec" + CoreAdminParams.ACTION + " is not illegal");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SolrException e) {
            logger.error("", e);
            throw e;
        } catch (Exception e) {
            logger.error("", e);
            throw new SolrException(ErrorCode.SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * 百岁 add 2016/06/14 start 更新配置文件
     *
     * @param req
     * @param rsp
     * @throws Exception
     */
    private void updateConfig(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        SolrParams params = req.getParams();
        String cname = params.get(CoreAdminParams.CORE);
        String collection = params.get(CoreAdminParams.COLLECTION);
        boolean needReload = params.getBool("needReload", true);
        // TisSolrResourceLoader.getRemoteSnapshotId(collection);
        int snapshotId = params.getInt(ICoreAdminAction.TARGET_SNAPSHOT_ID, -1);
        if (snapshotId < 0) {
            throw new IllegalStateException("param " + ICoreAdminAction.TARGET_SNAPSHOT_ID + " can not be null");
        }
        updateConfig(req, rsp, collection, cname, needReload, snapshotId);
    }

    /**
     * baisui add
     *
     * @param req
     * @param rsp
     * @param collection
     * @param needReload
     * @param newSnapshotId
     * @throws RepositoryException
     * @throws IOException
     */
    protected boolean updateConfig(SolrQueryRequest req, SolrQueryResponse rsp, String collection, String cname, boolean needReload, int newSnapshotId) throws RepositoryException, IOException {
        // try (SolrCore core = coreContainer.getCore(cname)) {
        File collectionDir = TisSolrResourceLoader.getCollectionConfigDir(new File(coreContainer.getSolrHome()), collection);
        // 本地版本号
        int loalSnapshot = TisSolrResourceLoader.getConfigSnapshotId(collectionDir);
        if (loalSnapshot == newSnapshotId) {
            String errorMsg = "repository snapshot is same to local snapshotid:" + loalSnapshot + "shall not update config";
            logger.warn(errorMsg);
            SimpleOrderedMap<String> errors = new SimpleOrderedMap<String>();
            errors.add("err1", errorMsg);
            rsp.add("failure", errors);
            return false;
        }
        PropteryGetter[] getters = TisSolrResourceLoader.configFileNames.values().toArray(new PropteryGetter[0]);
        // -1,
        // -1,
        TisSolrResourceLoader.downConfigFromConsoleRepository(newSnapshotId, collection, collectionDir, getters, true);
        try {
            if (needReload) {
                // 重新加载索引,当需要重新通过full build才能生效的时候，就先不reload
                // this.handleReloadAction(req, rsp);
                this.handReloadOperation(req, rsp);
            }
        } catch (Exception e) {
            // 回滚配置文件
            TisSolrResourceLoader.saveConfigFileSnapshotId(collectionDir, loalSnapshot);
            logger.warn(e.getMessage(), e);
            SimpleOrderedMap<String> errors = new SimpleOrderedMap<String>();
            errors.add("err1", e.getMessage());
            rsp.add("failure", errors);
            rsp.setException(e);
            return false;
        }
        return true;
        // }
    }

    // 百岁 add 2016/06/14 end
    // baisui add for reload start
    public void handReloadOperation(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        final CallInfo callInfo = new CallInfo(this, req, rsp, CoreAdminOperation.RELOAD_OP);
        callInfo.call();
    }


    /**
     * 交换索引全量，每天定時全量全量之后需要将索引回流集群
     *
     * @param req
     * @param rsp
     */
    protected void handleSwapindexfileAction(TaskObject taskObject, SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        Objects.requireNonNull(taskObject, "param taskObject can not be null");
        SolrParams params = req.getParams();
        String cname = params.get(CoreAdminParams.CORE);
        // 要切换成的配置版本id,可以为空代表只是一次普通的全量更新
        final Integer newSnapshotId = params.getInt(CoreAdminParams.PROPERTY_PREFIX + "newSnapshotId");
        if (cname == null || !coreContainer.getAllCoreNames().contains(cname)) {
            throw new SolrException(ErrorCode.BAD_REQUEST, "Core with core name [" + cname + "] does not exist.");
        }
        try (SolrCore core = coreContainer.getCore(cname)) {
            if (core == null) {
                throw new IllegalStateException("core:" + cname + " can not be null");
            }
            final long hdfsTimeStamp = params.getLong(CoreAdminParams.PROPERTY_PREFIX + HDFS_TIMESTAMP);
            Long coreReloadSleepTime = params.getLong(CoreAdminParams.PROPERTY_PREFIX + CORE_RELOAD_SLEEP_TIME);
            // 将新的时间
            final File oldIndexDir = new File(core.getNewIndexDir());
            String oldIndexDirName = oldIndexDir.getName();
            logger.info("oldIndexDirName:" + oldIndexDirName + ",abstractPath:" + oldIndexDir.getAbsolutePath());
            final File indexDirParent = oldIndexDir.getParentFile();
            File newDir = new File(indexDirParent, "index" + hdfsTimeStamp);
            File childFile = null;
            Matcher m = null;
            List<File> historyDirs = Lists.newArrayList();
            int maxOrder = -1;
            int order;
            for (String child : indexDirParent.list()) {
                childFile = new File(indexDirParent, child);
                if (childFile.isDirectory() && (m = INDEX_DATA_PATTERN.matcher(childFile.getName())).matches() && m.matches()) {
                    historyDirs.add(childFile);
                    order = 0;
                    if (m.group(1).equals(String.valueOf(hdfsTimeStamp))) {
                        try {
                            order = Integer.parseInt(m.group(3));
                        } catch (Throwable e) {
                        }
                        if (order > maxOrder) {
                            maxOrder = order;
                        }
                    }
                }
            }
            if (maxOrder > -1) {
                newDir = new File(indexDirParent, "index" + hdfsTimeStamp + "_" + (maxOrder + 1));
            }
            logger.info("newDir:{}", newDir.getAbsolutePath());

            long downloadStart = System.currentTimeMillis();
            final String taskId = req.getParams().get(CommonAdminParams.ASYNC);
            // 从hdfs上将build好的索引文件拉下来
            downloadIndexFile2IndexDir(taskObject, hdfsTimeStamp, core.getName(), newDir, rsp, taskId);
            // 更新index.properties中的index属性指向到新的文件夹目录
            refreshIndexPropFile(core, newDir.getName(), indexDirParent);
            if (newSnapshotId != null) {
                logger.info("after flowback update the config:" + cname + " to snapshot:" + newSnapshotId);
                // 重新加载索引,只更新一下配置，不做reload，因为目标版本和localsnapshot如果是一致的就不加载了
                updateConfig(req, rsp, core.getCoreDescriptor().getCollectionName(), cname, false, /* needReload */ newSnapshotId);
            }
            logger.info("download index consume:" + (System.currentTimeMillis() - downloadStart) + "ms");
            if (coreReloadSleepTime != null && coreReloadSleepTime > 0) {
                logger.info("after download index ,wait for " + coreReloadSleepTime + "ms,then to reload core");
                Thread.sleep(coreReloadSleepTime);
            }
            logger.info("start to reload core");
            this.handReloadOperation(req, rsp);
            for (File delete : historyDirs) {
                try {
                    FileUtils.forceDelete(delete);
                } catch (Throwable e) {
                }
            }
        }
        CoreContainer container = this.coreContainer;
        try (SolrCore core = coreContainer.getCore(cname)) {
            // 新的core重新注册一下，使得它重新执行recovery tlog的执行
            // container.registerCoreInZk(core);
            core.getUpdateHandler().getSolrCoreState().doRecovery(container, core.getCoreDescriptor(), true);
        }
    }

    /**
     * 索引回流执行状态
     *
     * @date 2016年8月18日
     */
    public static class IndexBackflowStatus {

        // 总共要传输的bytes
        private final long allContentLength;

        // 已经从hdfs传输到本地磁盘的文件bytes
        private final AtomicLong readBytesCount;

        public static IndexBackflowStatus add2Resp(SolrQueryResponse rsp, long allLength, AtomicLong allReadBytesCount) {
            IndexBackflowStatus backflowStatus = new IndexBackflowStatus(allLength, allReadBytesCount);
            // rsp.add(KEY_INDEX_BACK_FLOW_STATUS, backflowStatus);
            NamedList<Object> toLog = rsp.getToLog();
            toLog.add(KEY_INDEX_BACK_FLOW_STATUS, backflowStatus);
//            toLog.add(KEY_INDEX_BACK_FLOW_STATUS + TISCollectionUtils.INDEX_BACKFLOW_ALL, allLength);
//            toLog.add(KEY_INDEX_BACK_FLOW_STATUS + TISCollectionUtils.INDEX_BACKFLOW_READED, allReadBytesCount);
            return backflowStatus;
        }

        public IndexBackflowStatus(long allContentLength, AtomicLong readBytesCount) {
            super();
            this.allContentLength = allContentLength;
            this.readBytesCount = readBytesCount;
        }

        @Override
        public String toString() {
            return "{" +
                    TISCollectionUtils.INDEX_BACKFLOW_ALL + ":" + allContentLength +
                    "," + TISCollectionUtils.INDEX_BACKFLOW_READED + ":" + readBytesCount.get() +
                    '}';
        }

        public long getHaveReaded() {
            return readBytesCount.get();
        }

        public long getAllContentLength() {
            return allContentLength;
        }
    }

    /**
     * index.properties 文件更新
     *
     * @param
     * @param indexDirParent
     * @throws IOException
     */
    private void refreshIndexPropFile(SolrCore core, String newDatadir, final File indexDirParent) throws IOException {
        if (!TisIndexFetcher.modifyIndexDir(core, newDatadir)) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "rename index.properties prop to " + newDatadir + " 'index' faild");
        }
    }

    // private static final String SEGMENT_FILE = "segments_1";
    // private static final Pattern coreNamePattern =
    // Pattern.compile("(search4.+?)_shard(\\d+?)_replica_n\\d+");

    /**
     * 将刚刚构建好的全量文件放置到本地目标文件夹中
     *
     * @param
     * @param
     * @param
     * @throws IOException
     */
    protected void downloadIndexFile2IndexDir(TaskObject taskObj, long hdfsTimeStamp, String solrCoreName, final File indexDir, final SolrQueryResponse rsp, String taskId) {
        final long starttime = System.currentTimeMillis();
        TisCoreName tiscoreName = TISCollectionUtils.parse(solrCoreName);
        String coreName = tiscoreName.getName();
        final int group = tiscoreName.getSharedNo() - 1;
        // - 1;
        ITISFileSystem filesystem = IndexBuilderTriggerFactory.get().getFileSystem();
        IPath hdfsPath = filesystem.getPath(filesystem.getRootDir() + "/" + coreName + "/all/" + group + "/output/" + hdfsTimeStamp + "/index");
        logger.info("load from hdfs ,path:" + hdfsPath);
        // InputStream segmentStream = null;
        IndexWriter indexWriter = null;
        try {
            FileUtils.forceMkdir(indexDir);
            AtomicLong allReadBytesCount = new AtomicLong();

            indexWriter = createIndexWriter(new TISCopy2LocalDirectory(indexDir.toPath(), NoLockFactory.INSTANCE, allReadBytesCount, (size) -> {
                taskObj.setRspObject(rsp);
            }));
            // 直接一行代码将远端hdfs上的所有索引文件拷贝到本地来
            IContentSummary summary = filesystem.getContentSummary(hdfsPath);
            // Map<String, TaskObject> taskMap = this.getRequestStatusMap(RUNNING);
            //TaskObject taskObj = null;
//            if (taskMap == null || (taskObj = taskMap.get(taskId)) == null) {
//                throw new IllegalStateException("taskId:" + taskId + " relevant TaskObject can not be null");
//            }

            IndexBackflowStatus.add2Resp(rsp, summary.getLength(), allReadBytesCount);
            taskObj.setRspObject(rsp);

            this.copy2LocalDir(indexWriter, filesystem, hdfsPath, indexDir);
            logger.info("remote hdfs [" + hdfsPath + "] copy to local[" + indexDir + "] consome:"
                    + (System.currentTimeMillis() - starttime) + " allSize:" + FileUtils.byteCountToDisplaySize(summary.getLength()));
            indexWriter.commit();
            taskObj.setRspObject(rsp);
        } catch (SolrException e) {
            throw e;
        } catch (Exception e) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e.getMessage(), e);
        } finally {
            try {
                filesystem.close();
            } catch (Throwable e) {
            }
            try {
                indexWriter.close();
            } catch (Throwable e) {
            }
        }
    }

    private static IndexWriter createIndexWriter(Directory directory) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        indexWriterConfig.setMaxBufferedDocs(Integer.MAX_VALUE);
        indexWriterConfig.setRAMBufferSizeMB(IndexWriterConfig.DISABLE_AUTO_FLUSH);
        indexWriterConfig.setMergePolicy(NoMergePolicy.INSTANCE);
        indexWriterConfig.setOpenMode(OpenMode.CREATE);
        IndexWriter addWriter = new IndexWriter(directory, indexWriterConfig);
        // 必须commit一下才会产生segment*文件，如果不commit，indexReader读会报错。
        return addWriter;
    }

    /**
     * @param indexWriter
     * @param filesystem
     * @param hdfsPath
     * @param indexDir
     * @throws IOException
     */
    private void copy2LocalDir(IndexWriter indexWriter, ITISFileSystem filesystem, IPath hdfsPath, File indexDir) throws IOException {
        List<IPathInfo> status = filesystem.listChildren(hdfsPath);
        if (status == null) {
            throw new SolrException(ErrorCode.INVALID_STATE, "hdfsPath:" + hdfsPath + " is not exist in hdfs");
        }
        Directory childDir = null;
        IPath path = null;

        for (IPathInfo stat : status) {
            try {
                path = stat.getPath();
                childDir = createChildIndexDirectory(filesystem, path);
                indexWriter.addIndexes(childDir);
            } catch (IOException e) {
                if (path != null) {
                    throw new RuntimeException("path:" + path, e);
                } else {
                    throw e;
                }
            }
        }

    }

    protected Directory createChildIndexDirectory(ITISFileSystem filesystem, IPath path) throws IOException {

        if (ITISFileSystem.NAME_LOCAL_FS.equals(filesystem.getName())) {
            File dir = path.unwrap(File.class);
            return new MMapDirectory(dir.toPath(), NoLockFactory.INSTANCE);
        } else {
            return new TISDirectory(path, filesystem);
        }
    }


    private static class TISDirectory extends BaseDirectory {


        public static final int BUFFER_SIZE = 8192;

        private static final String LF_EXT = ".lf";

        protected final IPath fsDirPath;


        private final ITISFileSystem fileSystem;

        private final Set<String> readFiles = new HashSet<>();

        public TISDirectory(IPath hdfsDirPath, ITISFileSystem fileSystem) throws IOException {
            this(hdfsDirPath, NoLockFactory.INSTANCE, fileSystem);
        }

        @Override
        public ChecksumIndexInput openChecksumInput(String name, IOContext context) throws IOException {
            return super.openChecksumInput(name, context);
        }

        @Override
        public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
            throw new UnsupportedOperationException();
        }

        private TISDirectory(IPath hdfsDirPath, LockFactory lockFactory, ITISFileSystem fileSystem) throws IOException {
            super(lockFactory);
            this.fsDirPath = hdfsDirPath;
            this.fileSystem = fileSystem;
        }

        @Override
        public void close() throws IOException {
            fileSystem.close();
            isOpen = false;
        }

        @Override
        public Set<String> getPendingDeletions() throws IOException {
            return Collections.emptySet();
        }

        /**
         * Check whether this directory is open or closed. This check may return
         * stale results in the form of false negatives.
         *
         * @return true if the directory is definitely closed, false if the
         * directory is open or is pending closure
         */
        public boolean isClosed() {
            return !isOpen;
        }

        @Override
        public IndexOutput createOutput(String name, IOContext context) throws IOException {
            return new TisFsFileWriter(getFileSystem(), getChildPath(name), name);
        }

        private String[] getNormalNames(List<String> files) {
            int size = files.size();
            for (int i = 0; i < size; i++) {
                String str = files.get(i);
                files.set(i, toNormalName(str));
            }
            return files.toArray(new String[]{});
        }

        private String toNormalName(String name) {
            if (name.endsWith(LF_EXT)) {
                return name.substring(0, name.length() - 3);
            }
            return name;
        }

        @Override
        public IndexInput openInput(String name, IOContext context) throws IOException {
            return openInput(name, BUFFER_SIZE);
        }

        private IndexInput openInput(String name, int bufferSize) throws IOException {
            return new TISFSIndexInput(name, getFileSystem(), getChildPath(name), bufferSize);
        }

        @Override
        public void deleteFile(String name) throws IOException {
            IPath path = getChildPath(name);
            getFileSystem().delete(path, false);
        }

        @Override
        public void syncMetaData() throws IOException {
        }

        @Override
        public void rename(String source, String dest) throws IOException {
            // Path sourcePath = new Path(hdfsDirPath, source);
            // Path destPath = new Path(hdfsDirPath, dest);
            // fileContext.rename(sourcePath, destPath);
        }

        // @Override
        // public void renameFile(String source, String dest) throws IOException {
        // // Path sourcePath = new Path(hdfsDirPath, source);
        // // Path destPath = new Path(hdfsDirPath, dest);
        // // fileContext.rename(sourcePath, destPath);
        // }
        @Override
        public long fileLength(String name) throws IOException {
            IPath path = getChildPath(name);
            return getLength(getFileSystem(), path);
        }

        public long fileModified(String name) throws IOException {
            IPath path = getChildPath(name);
            IPathInfo fileStatus = fileSystem.getFileInfo(path);
            return fileStatus.getModificationTime();
        }

        private IPath getChildPath(String name) {
            return this.fileSystem.getPath(fsDirPath, name);
        }

        public long getLength(ITISFileSystem fileSystem, IPath path) throws IOException {
            IPathInfo fileStatus = fileSystem.getFileInfo(path);
            return fileStatus.getLength();
        }

        @Override
        public String[] listAll() throws IOException {
            List<IPathInfo> listStatus = getFileSystem().listChildren(fsDirPath);
            // FileStatus[] listStatus = getFileSystem().listStatus(fsDirPath);
            List<String> files = new ArrayList<>();
            if (listStatus == null) {
                return new String[]{};
            }
            for (IPathInfo status : listStatus) {
                files.add(status.getPath().getName());
            }
            return getNormalNames(files);
        }

        public IPath getFsDirPath() {
            return fsDirPath;
        }

        public ITISFileSystem getFileSystem() {
            return fileSystem;
        }


        @Override
        public void sync(Collection<String> names) throws IOException {
        }

        @Override
        public int hashCode() {
            return fsDirPath.hashCode();
        }
    }

    private static class TISFSIndexInput extends CustomBufferedIndexInput {

        private final IPath path;

        private final FSDataInputStream inputStream;

        private final long length;

        private boolean clone = false;

        public TISFSIndexInput(String name, ITISFileSystem fileSystem, IPath path, int bufferSize) throws IOException {
            super(name);
            this.path = path;
            length = fileSystem.getFileInfo(path).getLength();
            inputStream = fileSystem.open(path, bufferSize);
        }

        @Override
        protected void readInternal(byte[] b, int offset, int readLength) throws IOException {
            inputStream.readFully(getFilePointer(), b, offset, readLength);
        }

        @Override
        protected void seekInternal(long pos) throws IOException {
            inputStream.seek(pos);
        }

        @Override
        protected void closeInternal() throws IOException {
            logger.debug("Closing normal index input on {}", path);
            if (!clone) {
                inputStream.close();
            }
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public IndexInput clone() {
            TISFSIndexInput clone = (TISFSIndexInput) super.clone();
            clone.clone = true;
            return clone;
        }
    }

    public static class TisFsFileWriter extends OutputStreamIndexOutput {

        public static final String HDFS_SYNC_BLOCK = "solr.hdfs.sync.block";

        public static final int BUFFER_SIZE = 16384;

        public TisFsFileWriter(ITISFileSystem fileSystem, IPath path, String name) throws IOException {
            super("fileSystem=" + fileSystem.getName() + " path=" + path, name, getOutputStream(fileSystem, path), 16384);
        }

        private static final OutputStream getOutputStream(ITISFileSystem fileSystem, IPath path) throws IOException {
            // }
            return fileSystem.create(path, true);
        }
    }

}

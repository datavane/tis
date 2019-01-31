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
package org.apache.solr.handler.admin;

// import org.apache.solr.common.params.SolrParams;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.NoMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
// import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.solrextend.cloud.TisSolrResourceLoader;
import com.qlangtech.tis.solrextend.utils.TisIndexFetcher;

import store.hdfs.TisHdfsDirectory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisCoreAdminHandler extends CoreAdminHandler {

	private static final Logger log = LoggerFactory.getLogger(TisCoreAdminHandler.class);

	// public static final String HDFS_HOST = "hdfs_host";
	public static final String HDFS_TIMESTAMP = "hdfs_timestamp";

	private static final String CORE_RELOAD_SLEEP_TIME = "core_reload_sleep_time";

	public static final String HDFS_USER = "hdfs_user";

	public static final String KEY_INDEX_BACK_FLOW_STATUS = "index_back_flow_status";

	/**
	 * @param coreContainer
	 */
	public TisCoreAdminHandler(CoreContainer coreContainer) {
		super(coreContainer);
	}

	// @Override
	private void handleCreateAction(SolrQueryRequest req, SolrQueryResponse rsp) throws SolrException {
		// OverseerCollectionProcessor.COLL_PROP_PREFIX
		// 修改本地configset/search4xxxx/config.properties 配置文件中的内容
		int snapshotid = Integer.parseInt(req.getParams().get("property.configsnapshotid"));

		final String collection = req.getParams().get("collection");
		if (StringUtils.isEmpty(collection)) {
			throw new IllegalArgumentException("param collection can not be null");
		}
		// CoreDescriptor dcore = buildCoreDescriptor(req.getParams(),
		// coreContainer);
		String name = req.getParams().get(CoreAdminParams.NAME);
		if (StringUtils.isEmpty(name)) {
			throw new RuntimeException("Missing parameter [" + CoreAdminParams.NAME + "]");
		}

		java.nio.file.Path instancePath = coreContainer.getCoreRootDirectory().resolve(name);
		// File instanceDir = new
		// checkNotEmpty(,
		// );
		File collectionDir = TisSolrResourceLoader.getCollectionConfigDir(instancePath, collection);
		TisSolrResourceLoader.saveConfigFileSnapshotId(collectionDir, snapshotid);
		// super.handleCreateAction(req, rsp);
	}

	@Override
	protected void preCoreAdminHandlerExecute(SolrQueryRequest req, SolrQueryResponse rsp, CoreAdminOperation op) {
		if (CoreAdminOperation.CREATE_OP == op) {
			handleCreateAction(req, rsp);
		}
	}

	/**
	 */
	@Override
	protected void handleCustomAction(SolrQueryRequest req, SolrQueryResponse rsp) {
		try {
			SolrParams solrParams = req.getParams();
			String action = solrParams.get("exec" + CoreAdminParams.ACTION);
			if (StringUtils.equals("swapindexfile", action)) {
				// 执行替换全量的流程
				this.handleSwapindexfileAction(req, rsp);
				return;
			}
			throw new IllegalArgumentException("param exec" + CoreAdminParams.ACTION + " is not illegal");
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SolrException e) {
			log.error("", e);
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw new SolrException(ErrorCode.SERVER_ERROR, e.getMessage(), e);
		}
	}

	private static final Pattern INDEX_DATA_PATTERN = Pattern.compile("(index\\d{14})(_(\\d+))?");

	public static void main(String[] args) {
		Matcher m = INDEX_DATA_PATTERN.matcher("index20160318001000");
		if (m.matches()) {
			System.out.println(m.group(1));
			System.out.println(m.group(2));
			System.out.println(m.group(3));
		}
		m = INDEX_DATA_PATTERN.matcher("index20160318001000_1");
		if (m.matches()) {
			System.out.println(m.group(1));
			System.out.println(m.group(2));
			System.out.println(m.group(3));
		}
	}

	/**
	 * 交换索引全量，每天定時全量全量之后需要将索引回流集群
	 *
	 * @param req
	 * @param rsp
	 */
	protected void handleSwapindexfileAction(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
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
			final String hdfsHome = core.getSolrConfig().getVal("hdfsHome", true);
			long hdfsTimeStamp = params.getLong(CoreAdminParams.PROPERTY_PREFIX + HDFS_TIMESTAMP);
			String hdfsUser = params.get(CoreAdminParams.PROPERTY_PREFIX + HDFS_USER);
			Long coreReloadSleepTime = params.getLong(CoreAdminParams.PROPERTY_PREFIX + CORE_RELOAD_SLEEP_TIME);
			// 将新的时间
			// boolean replaceSameIndexDir = false;
			final File oldIndexDir = new File(core.getNewIndexDir());
			String oldIndexDirName = oldIndexDir.getName();
			log.info("oldIndexDirName:" + oldIndexDirName + ",abstractPath:" + oldIndexDir.getAbsolutePath());
			final File indexDirParent = oldIndexDir.getParentFile();
			File newDir = new File(indexDirParent, "index" + hdfsTimeStamp);
			if (newDir.exists()) {
				log.info("newdir:" + newDir.getAbsolutePath() + " is exist,will make a new dir");
				Matcher m = INDEX_DATA_PATTERN.matcher(newDir.getName());
				if (m.matches()) {
					int order = 1;
					if (StringUtils.isNotBlank(m.group(3))) {
						order = Integer.parseInt(m.group(3));
						order++;
					}
					newDir = new File(indexDirParent, m.group(1) + "_" + order);
					log.info("newdir:" + newDir.getAbsolutePath());
				} else {
					throw new IllegalStateException("newDir is not illegal:" + newDir.getAbsolutePath());
				}
			}
			long downloadStart = System.currentTimeMillis();
			final String taskId = req.getParams().get(CommonAdminParams.ASYNC);
			// 从hdfs上将build好的索引文件拉下来
			downloadIndexFile2IndexDir(hdfsHome, hdfsTimeStamp, hdfsUser, core, newDir, rsp, taskId);
			// 更新index.properties中的index属性指向到新的文件夹目录
			refreshIndexPropFile(core, newDir.getName(), indexDirParent);
			if (newSnapshotId != null) {
				log.info("after flowback update the config:" + cname + " to snapshot:" + newSnapshotId);
				// 重新加载索引,只更新一下配置，不做reload，因为目标版本和localsnapshot如果是一致的就不加载了
				updateConfig(req, rsp, core.getCoreDescriptor().getCollectionName(), cname, false, /* needReload */
						newSnapshotId);
			}
			log.info("download index consume:" + (System.currentTimeMillis() - downloadStart) + "ms");
			if (coreReloadSleepTime != null && coreReloadSleepTime > 0) {
				log.info("after download index ,wait for " + coreReloadSleepTime + "ms,then to reload core");
				Thread.sleep(coreReloadSleepTime);
			}
			log.info("start to reload core");
			this.handReloadOperation(req, rsp);
			FileUtils.forceDelete(oldIndexDir);
		}
		CoreContainer container = (CoreContainer) this.coreContainer;
		try (SolrCore core = coreContainer.getCore(cname)) {
			// 新的core重新注册一下，使得它重新执行recovery tlog的执行
			// container.registerCoreInZk(core);
			core.getUpdateHandler().getSolrCoreState().doRecovery(container, core.getCoreDescriptor(), true);
		}
	}

	/**
	 * 索引回流执行状态
	 *
	 * @author 百岁（baisui@2dfire.com）
	 *
	 * @date 2016年8月18日
	 */
	public static class IndexBackflowStatus {

		// 总共要传输的bytes
		private final long allContentLength;

		// 已经从hdfs传输到本地磁盘的文件bytes
		private final AtomicLong readBytesCount;

		public IndexBackflowStatus(long allContentLength, AtomicLong readBytesCount) {
			super();
			this.allContentLength = allContentLength;
			this.readBytesCount = readBytesCount;
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
	 * @param hdfsTimeStamp
	 * @param indexDirParent
	 * @throws IOException
	 */
	private void refreshIndexPropFile(SolrCore core, String newDatadir, final File indexDirParent) throws IOException {
		if (!TisIndexFetcher.modifyIndexDir(core, newDatadir)) {
			throw new SolrException(ErrorCode.SERVER_ERROR,
					"rename index.properties prop to " + newDatadir + " 'index' faild");
		}
	}

	// private static final String SEGMENT_FILE = "segments_1";
	private static final Pattern coreNamePattern = Pattern.compile("(search4.+?)_shard(\\d+?)_replica\\d+");

	/**
	 * 将刚刚构建好的全量文件放置到本地目标文件夹中
	 *
	 * @param path
	 * @param lockFactory
	 * @param dirContext
	 * @throws IOException
	 */
	protected void downloadIndexFile2IndexDir(String hdfsHome, long hdfsTimeStamp, String hdfsUser, SolrCore core,
			final File indexDir, SolrQueryResponse rsp, String taskId) {
		final long starttime = System.currentTimeMillis();
		Matcher coreNameMatcher = coreNamePattern.matcher(core.getName());
		if (!coreNameMatcher.matches()) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"core name:" + core.getName() + " does not match pattern:" + coreNameMatcher);
		}
		String coreName = coreNameMatcher.group(1);
		// 需要减1
		final int group = Integer.parseInt(coreNameMatcher.group(2)) - 1;
		Path hdfsPath = new Path(
				"/user/" + hdfsUser + "/" + coreName + "/all/" + group + "/output/" + hdfsTimeStamp + "/index");
		log.info("load from hdfs:" + hdfsHome + ",path:" + hdfsPath);
		// InputStream segmentStream = null;
		FileSystem filesystem = null;
		IndexWriter indexWriter = null;
		try {
			FileUtils.forceMkdir(indexDir);
			AtomicLong allReadBytesCount = new AtomicLong();
			// indexWriter =
			// createIndexWriter(FSDirectory.open(indexDir.toPath(),
			// NoLockFactory.INSTANCE));
			indexWriter = createIndexWriter(
					new TISCopy2LocalDirectory(indexDir.toPath(), NoLockFactory.INSTANCE, allReadBytesCount));
			// 直接一行代码将远端hdfs上的所有索引文件拷贝到本地来
			filesystem = getfileSystem(hdfsHome);
			ContentSummary summary = filesystem.getContentSummary(hdfsPath);
			Map<String, TaskObject> taskMap = this.getRequestStatusMap(RUNNING);
			TaskObject taskObj = null;
			if (taskMap == null || (taskObj = taskMap.get(taskId)) == null) {
				throw new IllegalStateException("taskId:" + taskId + " relevant TaskObject can not be null");
			}
			// 设置目录下所有文件占用的size
			rsp.add(KEY_INDEX_BACK_FLOW_STATUS, new IndexBackflowStatus(summary.getLength(), allReadBytesCount));
			taskObj.setRspObject(rsp);
			this.copy2LocalDir(indexWriter, filesystem, hdfsPath, indexDir);
			log.info("remote hdfs [" + hdfsPath + "] copy to local[" + indexDir + "] consome:"
					+ (System.currentTimeMillis() - starttime));
			indexWriter.commit();
			// 将一个初始segment_1 文件放到文件夹中去
			// segmentStream = core.getResourceLoader().openResource(
			// "com/tis/" + SEGMENT_FILE);
			// FileUtils.copyInputStreamToFile(segmentStream, new File(indexDir,
			// SEGMENT_FILE));
		} catch (SolrException e) {
			throw e;
		} catch (Exception e) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e.getMessage(), e);
		} finally {
			// IOUtils.closeQuietly(segmentStream);
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
	 * @param allReadBytesCount
	 *            累积已读字节长度
	 * @throws IOException
	 */
	private void copy2LocalDir(IndexWriter indexWriter, FileSystem filesystem, Path hdfsPath, File indexDir)
			throws IOException {
		FileStatus[] status = filesystem.listStatus(hdfsPath);
		if (status == null) {
			throw new SolrException(ErrorCode.INVALID_STATE, "hdfsPath:" + hdfsPath + " is not exist in hdfs");
		}
		TisHdfsDirectory hdfsDir = null;
		Path path = null;
		for (FileStatus stat : status) {
			path = stat.getPath();
			hdfsDir = new TisHdfsDirectory(path, filesystem);
			indexWriter.addIndexes(hdfsDir);
		}
	}

	// private static FileSystem fileSystem;
	private static FileSystem getfileSystem(String hdfsHome) throws Exception {
		return TISHdfsUtils.getFileSystem(hdfsHome);
	}
}

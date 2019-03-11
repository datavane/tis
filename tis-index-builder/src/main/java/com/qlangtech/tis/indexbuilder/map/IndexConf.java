package com.qlangtech.tis.indexbuilder.map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.source.impl.HDFSReaderFactory;
import com.qlangtech.tis.manage.common.IndexBuildParam;

public class IndexConf {
	public static final Logger logger = LoggerFactory.getLogger(IndexConf.class);

	private final TaskContext context;

	public IndexConf(TaskContext context) {
		this.context = context;
	}

	public String getFsName() {
		return getSourceFsName();

	}

	public String getSourceFsName() {
		return get(IndexBuildParam.INDEXING_SOURCE_FS_NAME);
	}

	public String getOutputPath() {
		return this.get(IndexBuildParam.INDEXING_OUTPUT_PATH);
	}

	// example:search4OperationStatistic
	public String getCollectionName() {
		String collectionName = this.get(IndexBuildParam.INDEXING_SERVICE_NAME);
		if (StringUtils.isBlank(collectionName)) {
			throw new IllegalStateException("collection name:" + collectionName + " can not be null");
		}
		return collectionName;
	}

	private String get(String key) {
		return this.context.getUserParam(key);
	}

	// public String getWriteFileUser() {
	// return get("terminator.write.file.user");
	// }

	public String getSchemaName() {
		return "schema.xml";
	}

	// public MergeMode getMergeMode() {
	// return MergeMode.valueOf(get("indexing.mergemode",
	// "serial").toUpperCase());
	// }

	public String getSourcePath() {
		return get(IndexBuildParam.INDEXING_SOURCE_PATH);
	}

	// public int getHdfsReaderBufferSize() {
	// return getInt("indexing.hdfsreaderbuffersize", 1024);
	// }

	public int getFlushCountThreshold() {
		return 90000000;
	}

	public long getFlushSizeThreshold() {

		return 100 * 1024 * 1024;
	}

	public int getDocQueueSize() {
		return 1000;
	}

	public int getMinSplitSize() {
		return 128 * 1024 * 1024;
	}

	public String getCoreName() {
		return get(// "indexing.corename"
				IndexBuildParam.INDEXING_CORE_NAME);
	}

	public int getDocMakerThreadCount() {

		return 2;
	}

	public int getIndexMakerThreadCount() {
		return 2;
	}

	public int getRamDirQueueSize() {
		return 2;
	}

	public int getMaxFailCount() {
		// return getInt("indexing.maxfailcount", 100);
		return 20;
	}

	public long getOptimizeSizeThreshold() {
		// return 1000000000L;
		// return getLong("indexing.optimze.optimizeSizeThreshold", );
		return 500 * 1024 * 1024;
	}

	public int getMergeThreads() {
		return 1;
	}

	/**
	 * @return
	 */
	public String getSourceReaderFactory() {
		return HDFSReaderFactory.class.getName();
	}

	public String getIncrTime() {
		return get(// "indexing.incrtime"
				IndexBuildParam.INDEXING_INCR_TIME);
	}

}

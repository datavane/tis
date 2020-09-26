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
package com.qlangtech.tis.trigger.jst;

import com.qlangtech.tis.cloud.dump.DumpJobId;
import com.qlangtech.tis.cloud.dump.DumpJobStatus;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.ITISFileSystemFactory;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob.BuildResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.ZkStateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年6月18日
 */
public abstract class AbstractIndexBuildJob implements Callable<BuildResult> {

    public static final String SCHEMA = "schema";

    // protected final String userName;
    // protected DistributeLog log;
    private static final Logger logger = LoggerFactory.getLogger(AbstractIndexBuildJob.class);

    // protected static final ITISFileSystem fileSystem;
    protected final int groupNum;

    private final ITISFileSystemFactory indexBuildFS;

    private final SnapshotDomain appDomain;

    static int jobid = 0;

    public AbstractIndexBuildJob(IExecChainContext execContext, ImportDataProcessInfo processInfo, int group, SnapshotDomain domain) {
        this.state = processInfo;
        if (StringUtils.isEmpty(processInfo.getTimepoint())) {
            throw new IllegalArgumentException("processInfo.getTimepoint() can not be null");
        }
        this.groupNum = (group);
        this.indexBuildFS = execContext.getIndexBuildFileSystem();
        // execContext.getAppDomain();
        this.appDomain = domain;
    }

    // public DistributeLog getLog() {
    // return log;
    // }
    // 
    // public void setLog(DistributeLog log) {
    // this.log = log;
    // }
    // public String getGroupNum() {
    // return String. groupNum;
    // }
    protected final ImportDataProcessInfo state;

    public BuildResult call() throws Exception {
        try {
            return startBuildIndex();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行单组build任務
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("all")
    public final BuildResult startBuildIndex() throws Exception {
        // final String coreName = state.getIndexName() + '-' + groupNum;
        // + '-' + groupNum;
        final String coreName = state.getCoreName(groupNum);
        final String timePoint = state.getTimepoint();
        final DumpJobStatus status = new DumpJobStatus();
        // status.setUserName(userName);
        status.setTimepoint(state.getTimepoint());
        status.setDumpType("remote");
        DumpJobId dumpJobId = new DumpJobId("jtIdentifier", jobid++);
        status.setDumpJobID(dumpJobId);
        status.setCoreName(coreName);
        RunEnvironment runtime = RunEnvironment.getSysRuntime();
        long now = System.currentTimeMillis();
        final String outPath = state.getIndexBuildOutputPath((this.groupNum));
        logger.info("build out path:" + outPath);
        // SnapshotDomain domain = HttpConfigFileReader.getResource(state.getIndexName()
        // , 0, runtime, ConfigFileReader.FILE_SCHEMA, ConfigFileReader.FILE_SOLR);
        // if (domain == null) {
        // throw new IllegalStateException("index:" + state.getIndexName() + ",runtime:" + runtime + " have not prepare for confg");
        // }
        writeResource2fs(coreName, appDomain, ConfigFileReader.FILE_SCHEMA, "config");
        writeResource2fs(coreName, appDomain, ConfigFileReader.FILE_SOLR, "config");
        // writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_CORE_PROPERTIES, "config");
        // // TODO 为了兼容老的索引先加上，到时候要删除掉的
        // writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_SCHEMA, SCHEMA);
        // writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_APPLICATION, "app");
        // writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_CORE_PROPERTIES, "core");
        // TODO 为了兼容老的索引先加上，到时候要删除掉的 end
        logger.info("Excute  RemoteDumpJob: Sbumit Remote Job .....  ");
        status.setStartTime(now);
        // String[] core = this.coreName.split("-");
        String serviceName = state.getIndexName();
        // ///////////////////////////////////////////
        logger.info("Excute Remote Dump Job Status: Sbumit  ");
        return buildSliceIndex(coreName, timePoint, status, outPath, serviceName);
    }

    protected abstract BuildResult buildSliceIndex(final String coreName, final String timePoint, final DumpJobStatus status, final String outPath, String serviceName) throws Exception, IOException, InterruptedException;

    public static class BuildResult {

        public static BuildResult createFaild() {
            BuildResult buildResult = new BuildResult(Integer.MAX_VALUE, new ImportDataProcessInfo(0, null));
            return buildResult.setSuccess(false);
        }

        public static BuildResult clone(BuildResult from) {
            BuildResult buildResult = new BuildResult(from.groupIndex, from.processInfo);
            buildResult.setSuccess(true).setIndexSize(from.indexSize);
            return buildResult;
        }

        private Replica replica;

        public Replica getReplica() {
            return replica;
        }

        public final String getNodeName() {
            return this.replica.getNodeName();
        }

        public BuildResult setReplica(Replica replica) {
            this.replica = replica;
            return this;
        }

        // private final RunningJob rj;
        private boolean success;

        private final ImportDataProcessInfo processInfo;

        public String getTimepoint() {
            return this.processInfo.getTimepoint();
        }

        public boolean isSuccess() {
            return success;
        }

        public BuildResult setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        private final int groupIndex;

        // 索引磁盘容量
        private long indexSize;

        public long getIndexSize() {
            return indexSize;
        }

        public void setIndexSize(long indexSize) {
            this.indexSize = indexSize;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public static final Pattern PATTERN_CORE = Pattern.compile("search4(.+?)_shard(\\d+?)_replica(\\d+?)");

        public BuildResult(Replica replica, ImportDataProcessInfo processInfo) {
            super();
            String coreName = replica.getStr(ZkStateReader.CORE_NAME_PROP);
            Matcher matcher = PATTERN_CORE.matcher(coreName);
            if (!matcher.matches()) {
                throw new IllegalStateException("coreName:" + coreName + " is not match the pattern:" + PATTERN_CORE);
            }
            this.groupIndex = Integer.parseInt(matcher.group(2)) - 1;
            this.processInfo = processInfo;
        }

        public BuildResult(int group, ImportDataProcessInfo processInfo) {
            super();
            this.groupIndex = group;
            this.processInfo = processInfo;
        }

        public String getHdfsSourcePath() {
            return this.processInfo.getHdfsSourcePath().build(String.valueOf(groupIndex));
        }
    }

    /**
     * @param
     * @param coreName
     * @param domain
     * @return
     * @throws
     */
    private void writeResource2fs(String coreName, SnapshotDomain domain, PropteryGetter getter, String subdir) {
        String path = this.state.getRootDir() + "/" + coreName + "/" + subdir + "/" + getter.getFileName();
        ITISFileSystem fs = this.indexBuildFS.getFileSystem();
        IPath dst = fs.getPath(path);
        if (dst == null) {
            throw new IllegalStateException("path can not be create:" + path);
        }
        OutputStream dstoutput = null;
        try {
            dstoutput = this.indexBuildFS.getFileSystem().create(dst, true);
            IOUtils.write(getter.getContent(domain), dstoutput);
        } catch (IOException e1) {
            throw new RuntimeException("[ERROR] Submit Service Core  Schema.xml to HDFS Failure !!!!", e1);
        } finally {
            IOUtils.closeQuietly(dstoutput);
        }
    }
}

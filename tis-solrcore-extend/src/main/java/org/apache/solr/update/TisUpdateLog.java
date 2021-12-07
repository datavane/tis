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
package org.apache.solr.update;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import org.apache.lucene.index.IndexCommit;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.core.IndexDeletionPolicyWrapper;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisUpdateLog extends UpdateLog {

    // 最大tlog 保存最大时间跨度文件
    private int maxHoursSave;

    private boolean deleteOnClose;

    private static final Logger LOG = LoggerFactory.getLogger(TisUpdateLog.class);

    @Override
    public void init(PluginInfo info) {
        super.init(info);
        // 保存9个小时的历史文件
        this.maxHoursSave = objToInt(info.initArgs.get("maxHoursSave"), 9);
        this.deleteOnClose = objToBoolean(info.initArgs.get("deleteOnClose"));
        LOG.info("deleteOnClose:{}", deleteOnClose);
    }

    protected static boolean objToBoolean(Object obj) {
        if (obj != null) {
            return Boolean.parseBoolean(obj.toString());
        } else {
            return false;
        }
    }

    private SolrCore core;

    @Override
    public void init(UpdateHandler uhandler, SolrCore core) {
        super.init(uhandler, core);
        this.core = core;
    }

    private long latestDeleteTimestamp = System.currentTimeMillis();

    @Override
    protected synchronized void addOldLog(TransactionLog oldLog, boolean removeOld) {
        // 历史文件不要删除
        oldLog.deleteOnClose = this.deleteOnClose;
        super.addOldLog(oldLog, removeOld);
        // 每20分钟检查一次
        if (!(System.currentTimeMillis() > (latestDeleteTimestamp + 1000 * 60 * 20))) {
            return;
        }
        LOG.info("check to see " + core.getName() + " wether have expire tlog file,dir:" + tlogDir.getAbsolutePath());
        latestDeleteTimestamp = System.currentTimeMillis();
        String[] tFiles = getLogList(tlogDir);
        for (int i = 0; i < tFiles.length; i++) {
            File f = new File(tlogDir, tFiles[i]);
            if (f.lastModified() + (maxHoursSave * 60 * 60 * 1000) <= latestDeleteTimestamp) {
                // try {
                // Files.deleteIfExists(f.toPath());
                //
                // } catch (IOException e) {
                //
                // }
                deleteFile(f);
                LOG.info("deleted:" + f.getAbsolutePath());
            } else {
                return;
            }
        }
    }

    public Future<RecoveryInfo> applyBufferedUpdates(long fulldumptimePoint) {
        // recovery trips this assert under some race - even when
        // it checks the state first
        // assert state == State.BUFFERING;
        // block all updates to eliminate race conditions
        // reading state and acting on it in the update processor
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        long fulldumpTimepointFrom1972 = 0;
        try {
            fulldumpTimepointFrom1972 = (dateFormat.parse(String.valueOf(fulldumptimePoint)).getTime() - 1800 * 1000);
        } catch (ParseException e1) {
            throw new RuntimeException(e1);
        }
        IndexCommit commit = core.getDeletionPolicy().getLatestCommit();
        try {
            if (commit == null) {
                RefCounted<SolrIndexSearcher> searcherRefCount = null;
                try {
                    searcherRefCount = core.getNewestSearcher(false);
                    if (searcherRefCount != null) {
                        commit = searcherRefCount.get().getIndexReader().getIndexCommit();
                    }
                } finally {
                    if (searcherRefCount != null) {
                        searcherRefCount.decref();
                    }
                }
            }
            if (commit != null) {
                long commitTimestamp = IndexDeletionPolicyWrapper.getCommitTimestamp(commit);
                if (commitTimestamp > fulldumpTimepointFrom1972) {
                    fulldumpTimepointFrom1972 = commitTimestamp;
                }
            }
        } catch (IOException e1) {
            throw new SolrException(ErrorCode.SERVER_ERROR, e1);
        }
        LOG.info("fulldumpTimepointFrom1972:" + fulldumpTimepointFrom1972 + "(" + dateFormat.format(new Date(fulldumpTimepointFrom1972)) + ")");
        versionInfo.blockUpdates();
        try {
            cancelApplyBufferUpdate = false;
            if (state != State.BUFFERING) {
                LOG.info("state:" + state + " is not buffering this recovery be cancel");
                return null;
            }
            // operationFlags &= ~FLAG_GAP;
            // handle case when no log was even created because no updates
            // were received.
            // if (tlog == null) {
            // state = State.ACTIVE;
            // return null;
            // }
            // tlog.incref();
            state = State.APPLYING_BUFFERED;
        } finally {
            versionInfo.unblockUpdates();
        }
        String[] logs = getLogList(tlogDir);
        TransactionLog oldLog = null;
        final List<TransactionLog> replayLogs = new ArrayList<>();
        // baisui add for tlog files desc
        StringBuffer recoveryLogFileDesc = new StringBuffer("fullindexbuild replay tlogfile desc:");
        for (String oldLogName : logs) {
            File f = new File(tlogDir, oldLogName);
            // 时间过滤
            if (f.lastModified() <= fulldumpTimepointFrom1972) {
                continue;
            }
            recoveryLogFileDesc.append(f.getName()).append("(").append(dateFormat.format(new Date(f.lastModified()))).append(")").append(",");
            // oldLog =;
            try {
                oldLog = new TransactionLog(f, null, true);
                oldLog.incref();
                replayLogs.add(oldLog);
            } catch (Throwable e) {
                LOG.error(f.getAbsolutePath(), e);
            }
        // addOldLog(oldLog, false); // don't remove old logs on startup
        // since more than one may be
        // uncapped.
        // } catch (Exception e) {
        // SolrException
        // .log(log,
        // "Failure to open existing log file (non fatal) "
        // + f, e);
        // deleteFile(f);
        // }
        }
        if (recoveryExecutor.isShutdown()) {
            for (TransactionLog log : replayLogs) {
                try {
                    log.decref();
                } catch (Exception e) {
                }
            }
            throw new RuntimeException("executor is not running...");
        }
        LOG.info("replay tlog size:" + replayLogs.size());
        if (replayLogs.size() > 0) {
            LOG.info(recoveryLogFileDesc.toString());
        }
        // recoveryInfo = new RecoveryInfo();
        // ExecutorCompletionService<RecoveryInfo> cs = new
        // ExecutorCompletionService<>(
        // recoveryExecutor);
        // LogReplayer replayer = new LogReplayer(Arrays.asList(
        // new TransactionLog[] { replayLogs.get(replayLogs.size() - 1) }),
        // true);
        // return cs.submit(replayer, recoveryInfo);
        recoveryInfo = new RecoveryInfo();
        ExecutorCompletionService<RecoveryInfo> cs = new ExecutorCompletionService<>(recoveryExecutor);
        // LogReplayer replayer = new LogReplayer(translog, true);
        return cs.submit(new LogReplayer(replayLogs, true), recoveryInfo);
    // }else{
    // return cs.submit(, recoveryInfo);
    // }
    }

    private static final class CommitVersionInfo {

        public final long version;

        public final long generation;

        private CommitVersionInfo(long g, long v) {
            generation = g;
            version = v;
        }

        /**
         * builds a CommitVersionInfo data for the specified IndexCommit. Will never be
         * null, ut version and generation may be zero if there are problems extracting
         * them from the commit data
         */
        public static CommitVersionInfo build(IndexCommit commit) {
            long generation = commit.getGeneration();
            long version = 0;
            try {
                final Map<String, String> commitData = commit.getUserData();
                String commitTime = commitData.get(SolrIndexWriter.COMMIT_TIME_MSEC_KEY);
                if (commitTime != null) {
                    try {
                        version = Long.parseLong(commitTime);
                    } catch (NumberFormatException e) {
                        LOG.warn("Version in commitData was not formated correctly: " + commitTime, e);
                    }
                }
            } catch (IOException e) {
                LOG.warn("Unable to get version from commitData, commit: " + commit, e);
            }
            return new CommitVersionInfo(generation, version);
        }
    }

    @Override
    protected void ensureLog() {
        boolean tlogExist = this.tlog != null;
        super.ensureLog();
        if (!tlogExist) {
            this.tlog.deleteOnClose = false;
        }
    }
}

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
package org.apache.solr.update;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.locks.Lock;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Sort;
import org.apache.solr.cloud.ActionThrottle;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.DirectoryFactory;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.RefCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * The state in this class can be easily shared between SolrCores across
 * SolrCore reloads.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class SolrCoreState {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected boolean closed = false;

    private final Object updateLock = new Object();

    public Object getUpdateLock() {
        return updateLock;
    }

    private int solrCoreStateRefCnt = 1;

    public void increfSolrCoreState() {
        synchronized (this) {
            if (solrCoreStateRefCnt == 0) {
                throw new CoreIsClosedException("IndexWriter has been closed");
            }
            solrCoreStateRefCnt++;
        }
    }

    public boolean decrefSolrCoreState(IndexWriterCloser closer) {
        boolean close = false;
        synchronized (this) {
            solrCoreStateRefCnt--;
            assert solrCoreStateRefCnt >= 0;
            if (solrCoreStateRefCnt == 0) {
                closed = true;
                close = true;
            }
        }
        if (close) {
            try {
                log.info("Closing SolrCoreState");
                close(closer);
            } catch (Exception e) {
                log.error("Error closing SolrCoreState", e);
            }
        }
        return close;
    }

    public abstract Lock getCommitLock();

    /**
     * << 百岁添加 ，添加一个参数 恢复的时候需要忽略本身自己是否是leader
     *
     * @param cc
     * @param cd
     * @param forceRecoveryIgnoreWetherIAmLeader
     */
    public abstract void doRecovery(CoreContainer cc, CoreDescriptor cd, boolean forceRecoveryIgnoreWetherIAmLeader);

    /*>>end*/
    /**
     * Force the creation of a new IndexWriter using the settings from the given
     * SolrCore.
     *
     * @param rollback close IndexWriter if false, else rollback
     * @throws IOException If there is a low-level I/O error.
     */
    public abstract void newIndexWriter(SolrCore core, boolean rollback) throws IOException;

    /**
     * Expert method that closes the IndexWriter - you must call {@link #openIndexWriter(SolrCore)}
     * in a finally block after calling this method.
     *
     * @param core that the IW belongs to
     * @param rollback true if IW should rollback rather than close
     * @throws IOException If there is a low-level I/O error.
     */
    public abstract void closeIndexWriter(SolrCore core, boolean rollback) throws IOException;

    /**
     * Expert method that opens the IndexWriter - you must call {@link #closeIndexWriter(SolrCore, boolean)}
     * first, and then call this method in a finally block.
     *
     * @param core that the IW belongs to
     * @throws IOException If there is a low-level I/O error.
     */
    public abstract void openIndexWriter(SolrCore core) throws IOException;

    /**
     * Get the current IndexWriter. If a new IndexWriter must be created, use the
     * settings from the given {@link SolrCore}.
     *
     * @throws IOException If there is a low-level I/O error.
     */
    public abstract RefCounted<IndexWriter> getIndexWriter(SolrCore core) throws IOException;

    /**
     * Rollback the current IndexWriter. When creating the new IndexWriter use the
     * settings from the given {@link SolrCore}.
     *
     * @throws IOException If there is a low-level I/O error.
     */
    public abstract void rollbackIndexWriter(SolrCore core) throws IOException;

    /**
     * Get the current Sort of the current IndexWriter's MergePolicy..
     *
     * @throws IOException If there is a low-level I/O error.
     */
    public abstract Sort getMergePolicySort() throws IOException;

    /**
     * @return the {@link DirectoryFactory} that should be used.
     */
    public abstract DirectoryFactory getDirectoryFactory();

    public interface IndexWriterCloser {

        public void closeWriter(IndexWriter writer) throws IOException;
    }

    public abstract void doRecovery(CoreContainer cc, CoreDescriptor cd);

    public abstract void cancelRecovery();

    public abstract void close(IndexWriterCloser closer);

    /**
     * @return throttle to limit how fast a core attempts to become leader
     */
    public abstract ActionThrottle getLeaderThrottle();

    public abstract boolean getLastReplicateIndexSuccess();

    public abstract void setLastReplicateIndexSuccess(boolean success);

    public static class CoreIsClosedException extends IllegalStateException {

        public CoreIsClosedException(String s) {
            super(s);
        }
    }
}

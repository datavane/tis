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
package com.qlangtech.tis.solrextend.realtime.reader.factory;

import com.qlangtech.tis.solrextend.realtime.reader.plugin.CoreIndexReaderFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface TSearcherMergeRealTimeReaderFactory extends CoreIndexReaderFactory {
    // public MergeSubIndexTracker getMergeSubIndexTracker();
    // public TSearcherAnalysisEngine getAnalysisEngine();
    // public void setAnalysisEngine(TSearcherAnalysisEngine engine);
    // public ExecutorService getThreadPool();
    // public void removeSubIndexReader(TSearcherMergeReader reader);
    // 
    // public void setFlush();
    // 
    // public TSearcherMergeReader getRamReader();
    // 
    // 
    // public TSearcherMergeReader getMainReader();
    // 
    // 
    // public List<TSearcherMergeReader> getDiskReaders();
    // 
    // public void setRamReader(TSearcherMergeReader newRamReader);
    // 
    // 
    // public AtomicBoolean getIsAfterFull();
    // 
    // 
    // public void addDiskReader(TSearcherMergeReader newDiskReader);
    // 
    // 
    // public TSearcherMergeReader newReader(IndexReader in, boolean isFSDir,
    // boolean isMain);
    // 
    // 
    // public TSearcherMergeReader newReader(IndexReader in, boolean isFSDir,
    // boolean isMain,File indexFile,SubIndexInfoForMerge forMerger);
}

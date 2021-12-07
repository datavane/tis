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

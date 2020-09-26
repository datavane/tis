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
package com.qlangtech.tis.indexbuilder.index;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import java.io.IOException;

/**
 * Clarification: Check Points (and commits)
 * IndexWriter writes new index files to the directory without writing a new segments_N
 * file which references these new files. It also means that the state of 
 * the in memory SegmentInfos object is different than the most recent
 * segments_N file written to the directory.
 * Each time the SegmentInfos is changed, and matches the (possibly 
 * modified) directory files, we have a new "check point". 
 * If the modified/new SegmentInfos is written to disk - as a new 
 * (generation of) segments_N file - this check point is also an 
 * IndexCommit.
 * A new checkpoint always replaces the previous checkpoint and 
 * becomes the new "front" of the index. This allows the IndexFileDeleter 
 * to delete files that are referenced only by stale checkpoints.
 * (files that were created since the last commit, but are no longer
 * referenced by the "front" of the index). For this, IndexFileDeleter 
 * keeps track of the last non commit checkpoint.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SimpleIndexWriter extends IndexWriter {

    public SimpleIndexWriter(Directory d, IndexWriterConfig conf) throws CorruptIndexException, LockObtainFailedException, IOException {
        super(d, conf);
    // TODO Auto-generated constructor stub
    }
}

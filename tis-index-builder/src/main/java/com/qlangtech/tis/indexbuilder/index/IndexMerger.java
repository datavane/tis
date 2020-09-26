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

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import org.apache.lucene.store.RAMDirectory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IndexMerger extends Callable<SuccessFlag> {

    public enum MergeMode {

        SERIAL, CONCURRENT
    }

    public void setDirQueue(BlockingQueue<RAMDirectory> ramIndexQueue);

    public void setCounters(Counters counters);

    public void setMessages(Messages messages);

    public void setAtomicInteger(AtomicInteger aliveIndexMakerCount);

    public void setIndexConf(IndexConf indexConf);

    // 停止执行
    public void shutdown();
}

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
package com.tis.zookeeper;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月2日
 */
public class ZkPathUtils {

    public static final String INDEX_BACKFLOW_SIGNAL_PATH_SEQNODE_NAME = "task";

    /**
     * 索引回流的时候在,需要在zk上写一个标记位
     *
     * @param indexName
     * @return
     */
    public static String getIndexBackflowSignalPath(String indexName) {
        final String zkBackIndexSignalPath = "/tis-lock/dumpindex/index-back-" + indexName;
        return zkBackIndexSignalPath;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}

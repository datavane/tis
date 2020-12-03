/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.hdfs.client.data;

import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description 定义读写HDFS方法
 * @since 2011-8-3 上午12:27:54
 * @version 1.0
 * @param <K>
 * @param <V>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface HDFSProvider<K, V> {

    public void setDumpContext(TSearcherDumpContext dumpContext);

    /**
     * 工作前初始化工作
     *
     * @throws
     */
    public void init();



    /**
     * 业务数据导入HDFS的总入口
     *
     * @throws
     */
    public // ,int groupNum
    void importServiceData(Map map);

    public void createSuccessToken(String time) throws Exception;

    // 判断是否已经成功导入
    public boolean shallProcessDumpTask(String time, boolean force, ITaskContext context) throws Exception;
}

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
package com.qlangtech.tis.hdfs.client.service;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * @description
 * @version 1.0.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ImportGroupWatcher implements Watcher {

    private static Log log = LogFactory.getLog(ImportGroupWatcher.class);

    private ImportGroupServiceSupport support;

    private TerminatorZkClient zkClient = null;

    private String serviceName = null;

    private ImportGroupConfig groupConfig;

    public ImportGroupWatcher(String serviceName, TerminatorZkClient zkClient, ImportGroupServiceSupport support, ImportGroupConfig groupConfig) {
        this.serviceName = serviceName;
        this.zkClient = zkClient;
        this.support = support;
        this.groupConfig = groupConfig;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        EventType type = event.getType();
        log.warn("Import Group Structure have changed ,zk path ==> [" + path + "] EventType ==> " + type);
        if (type == EventType.NodeChildrenChanged || type == EventType.NodeCreated || type == EventType.NodeDataChanged) {
            byte[] data = null;
            try {
                data = zkClient.getData(path, this);
            } catch (TerminatorZKException e) {
                log.error("get zk path ==> " + path + "'s data have problem!!!!", e);
                return;
            }
            if (data != null) {
                String groupString = new String(data);
                int groupNum = 0;
                try {
                    groupNum = Integer.valueOf(groupString);
                } catch (Exception e) {
                    log.error("ZK path[" + path + "]'s data [" + groupString + "] is not number!!!!!", e);
                }
                if (groupNum > 0) {
                    log.warn("Now The New Import Group Num :\n {\n" + groupNum + "\n}\n");
                    groupConfig.setGroupNums(groupNum);
                    try {
                        ImportGroupConfig.backUp2LocalFS(groupConfig);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else {
                log.error("get zk path ==> " + path + "'s data is null,so ingore the zk push !!!!");
                return;
            }
        }
    }
}

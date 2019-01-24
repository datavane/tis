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
package com.qlangtech.tis.hdfs.hsf.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.common.TerminatorCommonUtils;
import com.qlangtech.tis.common.zk.TerminatorZKUtils;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.common.zk.lock.Lock;

/*
 * @description
 * @since  2011-9-5 01:08:02
 * @version  1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ZKLock implements Lock {

    public static Log logger = LogFactory.getLog(ZKLock.class);

    private String appId;

    private String taskName;

    /**
     * @uml.property  name="client"
     * @uml.associationEnd
     */
    public TerminatorZkClient client;

    private String zkPath;

    public ZKLock(String appId, String taskName, TerminatorZkClient client) {
        this.appId = appId;
        this.taskName = taskName;
        this.client = client;
        this.init();
    }

    private void init() {
        // if (client == null || !client.useAble() || StringUtil.isBlank(taskName))
        // return;
        // Terminator+solrName+Dumper
        String path = TerminatorZKUtils.contactZnodePaths(appId, taskName);
        path = TerminatorZKUtils.contactZnodePaths(TerminatorZKUtils.MUTEXLOCK_ROOT, path);
        path = TerminatorZKUtils.normalizePath(path);
        String ip = TerminatorCommonUtils.getLocalHostIP();
        this.zkPath = path;
        try {
            if (client.exists(path)) {
                logger.info("[注意]：组内已经有机器获取到了Master角色");
            } else {
                client.createPathIfAbsent(path, TerminatorZKUtils.toBytes(ip), false);
                if (client.exists(path)) {
                    String owner = new String(client.getData(path));
                    if (ip.equals(owner)) {
                        logger.info("[注意]：本机获取到了Master角色");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ZKLock创建BasePath失败,path ==> " + path, e);
            throw new RuntimeException("ZKLock 初始化失败.", e);
        }
    }

    public boolean isOwner() {
        // if (client == null || !client.useAble())
        // return false;
        String path = genLockPath(appId, taskName);
        String ip = TerminatorCommonUtils.getLocalHostIP();
        try {
            if (client.exists(path)) {
                String owner = new String(client.getData(path));
                if (ip.equals(owner))
                    return true;
            }
        } catch (Exception e) {
            logger.debug("- ignore this exception " + e);
        }
        return false;
    }

    public boolean tryLock() {
        // if (client == null || !client.useAble()) return false;
        String path = genLockPath(appId, taskName);
        String ip = TerminatorCommonUtils.getLocalHostIP();
        try {
            if (client.exists(path)) {
                String owner = new String(client.getData(path));
                if (ip.equals(owner))
                    return true;
            } else {
                client.createPathIfAbsent(path, TerminatorZKUtils.toBytes(ip), false);
                if (client.exists(path)) {
                    String owner = new String(client.getData(path));
                    if (ip.equals(owner))
                        return true;
                }
            }
        } catch (Exception e) {
            logger.debug("- ignore this exception " + e);
        }
        return false;
    }

    public String getPath() {
        return zkPath;
    }

    public boolean unlock() {
        // if (client == null || !client.useAble()) return false;
        String path = genLockPath(appId, taskName);
        String ip = TerminatorCommonUtils.getLocalHostIP();
        try {
            if (client.exists(path)) {
                String owner = new String(client.getData(path));
                if (ip.equals(owner)) {
                    client.delete(path);
                }
            }
        } catch (Exception e) {
            logger.debug("- ignore this exception " + e);
        }
        return true;
    }

    public static String genLockPath(String appId, String taskName) {
        String path = TerminatorZKUtils.contactZnodePaths(appId, taskName);
        path = TerminatorZKUtils.contactZnodePaths(TerminatorZKUtils.MUTEXLOCK_ROOT, path);
        path = TerminatorZKUtils.contactZnodePaths(path, TerminatorZKUtils.LOCK_OWNER);
        path = TerminatorZKUtils.normalizePath(path);
        return path;
    }
}

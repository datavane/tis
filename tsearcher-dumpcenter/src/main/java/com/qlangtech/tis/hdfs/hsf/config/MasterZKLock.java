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
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZKUtils;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * @ClassName: MasterZKLock
 * @Description: 
 * @version V1.0   
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MasterZKLock {

    public static Log logger = LogFactory.getLog(ZKLock.class);

    public TerminatorZkClient client;

    public MasterZKLock(TerminatorZkClient client) {
        this.client = client;
    }

    public String getMasterIP(String zkpath, int tryCount) {
        try {
            if (client.exists(zkpath)) {
                byte[] ips = client.getData(zkpath);
                String owner = null;
                if (ips != null) {
                    owner = new String(ips);
                    // if (ip.equals(owner)) {
                    // logger.warn(">>>>>>>>【注意】：本机[" + ip +
                    // "]本身就是Master角色<<<<<<<");
                    // 
                    // } else {
                    logger.warn(">>>>>>>>【注意】：ZK-Path[" + zkpath + "]'s IP[" + owner + "]<<<<<<<");
                    // }
                    return owner;
                } else {
                    logger.warn(">>>>>>>>【注意】：owner节点存在，但是IP不存在,继续等待尝试[" + (tryCount) + "]<<<<<<");
                    Thread.sleep(5000);
                    if (tryCount >= 0)
                        return getMasterIP(zkpath, --tryCount);
                    else {
                        return null;
                    }
                }
            } else {
                logger.warn(">>>>>>>>【注意】：Master Path[" + zkpath + "] 还没创建，继续等待尝试[" + (tryCount) + "]次<<<<<<");
                Thread.sleep(5000);
                if (tryCount >= 0)
                    return getMasterIP(zkpath, --tryCount);
                else
                    return null;
            }
        } catch (TerminatorZKException e) {
            logger.error(">>>>>>>>获取ZKMaster[" + zkpath + "] IP 失败<<<<<<", e);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String getMasterIP(String serviceName, String group) {
        String coreName = "Terminator" + serviceName + "-" + group;
        String taskId = "/Dumper/owner";
        String path = TerminatorZKUtils.contactZnodePaths(coreName, taskId);
        path = TerminatorZKUtils.contactZnodePaths(TerminatorZKUtils.MUTEXLOCK_ROOT, path);
        return this.getMasterIP(path, 5);
    }

    public static void main(String[] args) {
        String zkServerAddress = "10.232.15.46:2181,10.232.36.130:2181";
        int zkClientTimeout = 30000;
        int zkClientConnectTimeout = 180000;
        TerminatorZkClient zkClient = TerminatorZkClient.create(zkServerAddress, zkClientTimeout, null, true);
        MasterZKLock zkLock = new MasterZKLock(zkClient);
        String zkIp = zkLock.getMasterIP("search4hongzhen.lm", "0");
        System.out.println("test" + zkIp);
    }
}

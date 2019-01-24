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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.common.config.GroupConfig;
import com.qlangtech.tis.common.zk.TerminatorZKUtils;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * @description 因为导入应该在查询组之前就让客户端获取到 所以只能独立一个新的分组Config来处理导入分组变更的情况
 * @version 1.0.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ImportGroupConfig implements Serializable {

    /**
     */
    private static final long serialVersionUID = -3167055235327688059L;

    private static Log log = LogFactory.getLog(ImportGroupConfig.class);

    public static final String TERMINATOR_ROOT_PATH = "terminator";

    public static final String TERMINATOR_IMPORT_GROUP = "group-nums";

    private transient ImportGroupServiceSupport support;

    private transient TerminatorZkClient zkClient = null;

    private String serviceName = null;

    private Integer groupNum;

    public Set<String> getGroupNameSet() {
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < groupNum; i++) {
            set.add(Integer.toString(i));
        }
        return set;
    }

    public ImportGroupConfig(String serviceName, TerminatorZkClient zkClient, ImportGroupServiceSupport support) {
        this.serviceName = serviceName;
        this.zkClient = zkClient;
        this.support = support;
        groupNum = new Integer(0);
        initConfig();
    }

    public synchronized void initConfig() {
        try {
            String servicePath = TerminatorZKUtils.contactZnodePaths(TerminatorZKUtils.contactZnodePaths(TERMINATOR_ROOT_PATH, TERMINATOR_IMPORT_GROUP), serviceName);
            boolean exists = zkClient.exists(servicePath);
            if (!exists) {
                log.warn("CenterNode may be not Publish Import Group Service View to ZK,So the Service Client must be waitting util CenterNode publish the Import Service View");
            }
            ImportGroupWatcher groupWatcher = new ImportGroupWatcher(serviceName, zkClient, support, this);
            String groupString = new String("0");
            if (zkClient.exists(servicePath, groupWatcher)) {
                byte[] groupNum = zkClient.getData(servicePath);
                if (groupNum != null) {
                    /**
                     * CenterNode 已经发布了组
                     */
                    groupString = new String(groupNum);
                } else {
                    throw new RuntimeException("ZK path[" + servicePath + "] have exist,but the data have not exist!!!!");
                }
            } else {
                log.warn("CenterNode have not publish the groupNum,so it doesnot import the source dat to hdfs!!!");
            }
            try {
                groupNum = Integer.valueOf(groupString);
            } catch (Exception e) {
                throw new RuntimeException("ZK path[" + servicePath + "]'s data [" + groupString + "] is not number!!!!!", e);
            }
            log.warn("ImportGroupConfig's structure :\n{\n" + this.toString() + "\n}\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void backUp2LocalFS(ImportGroupConfig serviceConfig) throws IOException {
        ObjectOutputStream out = null;
        try {
            File file = new File("importGroupConfig.bak");
            log.warn("Back Up ImportServiceConfig Object To Local FileSystem ==> " + file.getAbsolutePath());
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(file);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(serviceConfig);
            out.flush();
        } finally {
            if (out != null)
                out.close();
        }
    }

    public static ImportGroupConfig loadFromLocalFS() throws IOException, ClassNotFoundException {
        ObjectInputStream input = null;
        try {
            File file = new File("importGroupConfig.bak");
            if (!file.exists()) {
                throw new RuntimeException("Can not found backup object file  ==> " + file.getAbsolutePath());
            }
            FileInputStream fileInput = new FileInputStream(file);
            input = new ObjectInputStream(fileInput);
            return (ImportGroupConfig) input.readObject();
        } finally {
            if (input != null)
                input.close();
        }
    }

    public void checkBySelf() {
        if (groupNum.intValue() == 0) {
            // throw new
            // RuntimeException("ServiceConfig配置对象理论上来讲有问题,没有任何的GroupConfig信息.");
            log.warn("CenterNode have not publish ZK cluster view,So it does not support Import Source Data!!!!!");
        }
    }

    public int getGroupNums() {
        return groupNum.intValue();
    }

    public void setGroupNums(int groupNums) {
        this.groupNum = groupNums;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceName).append("\n");
        sb.append("{ \n");
        sb.append("importGroup:" + groupNum);
        sb.append("} \n");
        return sb.toString();
    }
}

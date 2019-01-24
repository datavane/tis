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
package com.qlangtech.tis.cloud.server.common;

/*
 * @description 关于CoreNode中关于SolrCore的基本信息
 * @version 1.0.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CoresInfo {

    public int version;

    public int namespaceID;

    private String nodeId;

    public long cTime;

    public long getCTime() {
        return cTime;
    }

    /**
     * @return the nodeId
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * @param nodeId the nodeId to set
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public CoresInfo() {
        this(0, 0, 0L);
    }

    public CoresInfo(int version, int nsID, long cT) {
        this.version = version;
        this.namespaceID = nsID;
        this.cTime = cT;
    }

    public CoresInfo(CoresInfo from) {
        setSolrCoreInfo(from);
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the namespaceID
     */
    public int getNamespaceID() {
        return namespaceID;
    }

    /**
     * @param namespaceID the namespaceID to set
     */
    public void setNamespaceID(int namespaceID) {
        this.namespaceID = namespaceID;
    }

    /**
     * @return the cTime
     */
    public long getcTime() {
        return cTime;
    }

    /**
     * @param cTime the cTime to set
     */
    public void setcTime(long cTime) {
        this.cTime = cTime;
    }

    public void setSolrCoreInfo(CoresInfo from) {
        this.version = from.getVersion();
        this.namespaceID = from.getNamespaceID();
        this.cTime = from.getcTime();
    }

    public static String getRegistrationID(CoresInfo storage) {
        return "NS-" + Integer.toString(storage.getNamespaceID()) + "-" + Integer.toString(storage.getVersion()) + "-" + Long.toString(storage.getCTime());
    }
}

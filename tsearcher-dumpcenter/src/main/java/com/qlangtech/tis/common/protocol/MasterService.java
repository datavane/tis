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
package com.qlangtech.tis.common.protocol;

import com.qlangtech.tis.common.TerminatorMasterServiceException;

/*
 * 该接口提供全量索引、增量索引和实时索引的写入操作。
 * 全量索引开始之后，将不再接受新的增量或者实时索引写入请求。
 * 增量索引和实时索引使用同一个<code>IndexConsumer</code>，也就是说，
 * 增量索引和实时索引写入操作都将视为增量索引写入请求。它们共享同一个UUID
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface MasterService {

    /**
     * 全量索引时用来传送索引数据，传送数据是需要传入一个uuid。
     * 这个id在调用<code>startFullDump</code>方法时返回。
     * @param indexData
     */
    public boolean fullDump(String clientIp, byte[] indexData) throws TerminatorMasterServiceException;

    /**
     * 增量索引时用来传送索引数据，传送数据是需要传入一个uuid。
     * 这个id在调用<code>incrDumpFinish</code>方法时返回。
     * @param indexData
     */
    public boolean incrDump(byte[] indexData) throws TerminatorMasterServiceException;

    /**
     * 全量索引结束，需要传入uuid作为身份标识
     */
    public boolean finishFullDump(String clientIp) throws TerminatorMasterServiceException;

    /**
     * 增量索引结束，需要传入uuid作为身份标识
     */
    public boolean finishIncrDump() throws TerminatorMasterServiceException;

    /**
     * 与服务器端进行握手，询问是否可以进行一次全量索引
     * @return
     * 	如果可以进行全量，则返回一个UUID，否则返回null
     */
    public boolean startFullDump(String clientIp) throws TerminatorMasterServiceException;

    /**
     * 与服务器端进行握手，询问是否可以进行一次增量索引
     * @return
     * 	可以返回true，如果服务器端正在进行一次增量索引，返回false
     */
    public boolean startIncDump() throws TerminatorMasterServiceException;

    /**
     * 查看是否有全量索引正在进行
     *
     * @return
     * 	true=有全量索引正在进行，false=没有全量索引正在进行
     */
    public boolean isFullIndexRunning() throws TerminatorMasterServiceException;

    /**
     * 查看是否有增量索引正在进行
     *
     * @return
     * 	true=有增量索引正在进行，false=没有增量索引正在进行
     */
    public boolean isIncrIndexRunning() throws TerminatorMasterServiceException;

    /**
     * 查看是否有全量或者增量索引正在进行
     *
     * @return
     * 	true=有全量或者增量索引正在进行，false=即没有全量索引正在进行，也没有增量索引正在进行
     */
    public boolean isIndexRunning() throws TerminatorMasterServiceException;

    /**
     * 获取date时间点以后的所有的增量索引源文件的列表
     *
     * @param date
     */
    public FetchFileListResponse fetchIncrFileList(String date);

    /**
     * Slave调用此方法告知Master全量索引文件已经下载完毕
     */
    public void pullIndexFinished(String ip) throws TerminatorMasterServiceException;
}

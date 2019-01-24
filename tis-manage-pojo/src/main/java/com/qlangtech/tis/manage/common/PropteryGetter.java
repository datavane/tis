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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface PropteryGetter {

    public String getFileName();

    public String getMd5CodeValue(SnapshotDomain domain);

    public Long getFileSufix(SnapshotDomain domain);

    public byte[] getContent(SnapshotDomain domain);

    public UploadResource getUploadResource(SnapshotDomain snapshotDomain);

    /**
     * 判断文件格式是否合法
     *
     * @param domain
     * @return
     */
    public ConfigFileValidateResult validate(UploadResource resource);

    public ConfigFileValidateResult validate(byte[] resource);

    // public boolean isJar();
    /**
     * 更新配置文件的时，当更新成功之后需要创建一条新的snapshot事体对象
     *
     * @param newId
     * @param domain
     * @return
     */
    public Snapshot createNewSnapshot(Integer newResourceId, Snapshot snapshot);
}

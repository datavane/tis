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
package com.qlangtech.tis.manage;

import java.nio.charset.Charset;
import junit.framework.Assert;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class Savefilecontent {

    // <services:field name="content" displayName="文件内容">
    // <required-validator>
    // <message>请填写${displayName}</message>
    // </required-validator>
    // </services:field>
    // <services:field name="snapshotid" displayName="快照编号">
    // <required-validator>
    // <message>请选择${displayName}</message>
    // </required-validator>
    // </services:field>
    // <services:field name="filename" displayName="文件内容">
    // <required-validator>
    // <message>请填写${displayName}</message>
    // </required-validator>
    // </services:field>
    private String content;

    private Integer snapshotid;

    // 代表编辑的文件内容
    private String filename;

    public byte[] getContentBytes() {
        Assert.assertNotNull("content can not be null", content);
        return content.getBytes(Charset.forName(BasicModule.getEncode()));
    }

    /**
     * 組編號
     */
    // private Integer gid;
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSnapshotid() {
        return snapshotid;
    }

    public void setSnapshotid(Integer snapshotid) {
        this.snapshotid = snapshotid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    // public Integer getGid() {
    // return gid;
    // }
    // 
    // public void setGid(Integer gid) {
    // this.gid = gid;
    // }
}

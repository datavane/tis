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
package com.qlangtech.tis.manage;

import java.nio.charset.Charset;

import com.qlangtech.tis.manage.common.TisUTF8;
import junit.framework.Assert;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public final class Savefilecontent {

    private String content;

    private Integer snapshotid;

    // 代表编辑的文件内容
    private String filename;

    private String memo;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public byte[] getContentBytes() {
        Assert.assertNotNull("content can not be null", content);
        return content.getBytes(TisUTF8.get());
    }

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
}

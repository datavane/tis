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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface PropteryGetter {

    String KEY_PROP_CONFIG_SNAPSHOTID = "property.configsnapshotid";

    public String getFileName();

    public String getMd5CodeValue(SnapshotDomain domain);

    public Long getFileSufix(SnapshotDomain domain);

    public byte[] getContent(SnapshotDomain domain);

    public UploadResource getUploadResource(SnapshotDomain snapshotDomain);

    /**
     * 判断文件格式是否合法
     *
     * @param
     * @return
     */
    public ConfigFileValidateResult validate(UploadResource resource);

    public ConfigFileValidateResult validate(byte[] resource);

    // public boolean isJar();
    /**
     * 更新配置文件的时，当更新成功之后需要创建一条新的snapshot事体对象
     *
     * @param
     * @param
     * @return
     */
    public Snapshot createNewSnapshot(Integer newResourceId, Snapshot snapshot);
}

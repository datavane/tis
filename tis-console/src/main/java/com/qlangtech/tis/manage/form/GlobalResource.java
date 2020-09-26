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
package com.qlangtech.tis.manage.form;

import org.apache.commons.fileupload.FileItem;

/**
 * 全局依赖资源
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-6-4
 */
public class GlobalResource {

    private FileItem resource;

    // 资源备注
    private String memo;

    public FileItem getResource() {
        return resource;
    }

    public void setResource(FileItem resource) {
        this.resource = resource;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

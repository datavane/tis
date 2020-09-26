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
package com.qlangtech.tis.workflow.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TableDump implements Serializable {

    private Integer id;

    private Integer datasourceTableId;

    private String hiveTableName;

    /**
     * prop:1???ɹ?
     *     2??ʧ??
     *     3???????
     */
    private Byte state;

    private Byte isValid;

    private Date createTime;

    private Date opTime;

    /**
     * prop:??????¼dump????????dump?˶??????
     */
    private String info;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDatasourceTableId() {
        return datasourceTableId;
    }

    public void setDatasourceTableId(Integer datasourceTableId) {
        this.datasourceTableId = datasourceTableId;
    }

    public String getHiveTableName() {
        return hiveTableName;
    }

    public void setHiveTableName(String hiveTableName) {
        this.hiveTableName = hiveTableName == null ? null : hiveTableName.trim();
    }

    /**
     * get:1???ɹ?
     *     2??ʧ??
     *     3???????
     */
    public Byte getState() {
        return state;
    }

    /**
     * set:1???ɹ?
     *     2??ʧ??
     *     3???????
     */
    public void setState(Byte state) {
        this.state = state;
    }

    public Byte getIsValid() {
        return isValid;
    }

    public void setIsValid(Byte isValid) {
        this.isValid = isValid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getOpTime() {
        return opTime;
    }

    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }

    /**
     * get:??????¼dump????????dump?˶??????
     */
    public String getInfo() {
        return info;
    }

    /**
     * set:??????¼dump????????dump?˶??????
     */
    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }
}

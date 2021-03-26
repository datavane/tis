/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 单Groupbuild状态
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class BuildSharedPhaseStatus extends AbstractChildProcessStatus {

    private static final String NAME = "build";

    private long allBuildSize;

    // 已经处理的记录数
    private long buildReaded;

    private Integer taskid;
    // 分组名称
    private String sharedName;

    public String getSharedName() {
        return this.sharedName;
    }

    public void setSharedName(String sharedName) {
        this.sharedName = sharedName;
    }

    public Integer getTaskid() {
        return taskid;
    }

    public void setTaskid(Integer taskid) {
        this.taskid = taskid;
    }

    @Override
    public String getAll() {
        return FileUtils.byteCountToDisplaySize(this.allBuildSize);
    }

    @Override
    public String getProcessed() {
        return buildReaded + "r";
    }

    @Override
    public int getPercent() {
        // FIXME: buildReaded 是记录条数，allBuildSize是总文件size
        return (int) ((this.buildReaded * 1f / this.allBuildSize) * 100);
    }

    @Override
    public final String getName() {
        if (StringUtils.isEmpty(getSharedName())) {
            return StringUtils.EMPTY;
        } else {
            return getSharedName() + "-" + NAME;
        }
    }

    public void setAllBuildSize(long allBuildSize) {
        this.allBuildSize = allBuildSize;
    }

    public void setBuildReaded(long buildReaded) {
        this.buildReaded = buildReaded;
    }

    public long getAllBuildSize() {
        return this.allBuildSize;
    }

    public long getBuildReaded() {
        return this.buildReaded;
    }
}

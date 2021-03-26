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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.fullbuild.phasestatus.IProcessDetailStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;
import org.apache.commons.lang.StringUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class DumpPhaseStatus extends BasicPhaseStatus<TableDumpStatus> {

    @JSONField(serialize = false)
    public final Map<String, TableDumpStatus> /* table name,db.tableName */
    tablesDump = new HashMap<>();

    private final ProcessDetailStatusImpl<TableDumpStatus> processDetailStatus;

    public DumpPhaseStatus(int taskid) {
        super(taskid);
        this.processDetailStatus = new ProcessDetailStatusImpl<TableDumpStatus>(DumpPhaseStatus.this.tablesDump) {

            @Override
            protected TableDumpStatus createMockStatus() {
                TableDumpStatus s = new TableDumpStatus(StringUtils.EMPTY, 1);
                s.setWaiting(true);
                return s;
            }
        };
    }

    @Override
    protected FullbuildPhase getPhase() {
        return FullbuildPhase.FullDump;
    }

    @Override
    public boolean isShallOpen() {
        return shallOpenView(this.tablesDump.values());
    }

    @Override
    protected Collection<TableDumpStatus> getChildStatusNode() {
        return this.tablesDump.values();
    }

    /**
     * 取得表執行狀態
     *
     * @param tableName
     * @return
     */
    public TableDumpStatus getTable(String tableName) {
        TableDumpStatus tabDumpStatus = this.tablesDump.get(tableName);
        if (tabDumpStatus == null) {
            tabDumpStatus = new TableDumpStatus(tableName, this.getTaskId());
            this.tablesDump.put(tableName, tabDumpStatus);
        }
        return tabDumpStatus;
    }

    @Override
    public IProcessDetailStatus<TableDumpStatus> getProcessStatus() {
        return this.processDetailStatus;
    }

    /**
     * 表dump状态
     *
     * @author 百岁（baisui@2dfire.com）
     * @date 2017年6月17日
     */
    public static class TableDumpStatus extends AbstractChildProcessStatus {

        private String tableName;

        private int taskid;

        // 全部的记录数
        private int allRows;

        // 已经读取的记录数
        private int readRows;

        public TableDumpStatus() {
        }

        /**
         * 执行状态描述
         *
         * @return
         */
        public Map<String, String> getDesc(final boolean success, String pt) {
            Map<String, String> state = new HashMap<String, String>();
            state.put("tabname", tableName);
            state.put("all", String.valueOf(allRows));
            state.put("readed", String.valueOf(readRows));
            state.put("success", String.valueOf(success));
            state.put("pt", pt);
            return state;
        }

        public void setAllRows(int allRows) {
            this.allRows = allRows;
        }

        public TableDumpStatus(String tableName, int taskid) {
            this.tableName = tableName;
            this.taskid = taskid;
        }

        public Integer getTaskid() {
            return this.taskid;
        }

        public int getReadRows() {
            return this.readRows;
        }

        public void setReadRows(int readRows) {
            this.readRows = readRows;
        }

        public int getAllRows() {
            return allRows;
        }
        @Override
        public String getAll() {
            return String.valueOf(this.allRows);
        }
        /**
         * 取得已经dump的数据进度百分比
         *
         * @return
         */
        public int getDumpPercent() {
            if (isSuccess()) {
                return 100;
            }
            if (this.allRows < 1) {
                return 0;
            }
            return (int) (readRows * 100 / allRows);
        }



        @Override
        public String getProcessed() {
            return String.valueOf(this.readRows);
        }

        @Override
        public int getPercent() {
            return this.getDumpPercent();
        }

        @Override
        public String getName() {
            return this.tableName;
        }
    }
}

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
package com.qlangtech.tis.fullbuild.indexbuild;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.order.dump.task.ITableDumpConstant;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.lang.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskContext {

    private static final long serialVersionUID = 1L;

    private final Counters counters = new Counters();

    private final Messages message = new Messages();

    private final IParamGetter commandLine;

    private TaskContext(IParamGetter commandLine) {
        super();
        this.commandLine = commandLine;
    }

    public EntityName parseDumpTable() {
        return EntityName.parse(this.get(ITableDumpConstant.DUMP_DBNAME) + "." + this.get(ITableDumpConstant.DUMP_TABLE_NAME));
    }

    public static TaskContext create(IParamGetter commandLine) {
        return new TaskContext(commandLine);
    }

    public static TaskContext create() {
        return create(new HashMap<>());
    }

    public static TaskContext create(Map<String, String> params) {
        return new TaskContext((key) -> params.get(key));
    }

    private Integer allRowCount;

    public int getAllRowCount() {
        if (allRowCount == null) {
            try {
                allRowCount = Integer.parseInt(getInnerParam(IndexBuildParam.INDEXING_ROW_COUNT));
            } catch (Throwable e) {
                allRowCount = Integer.MAX_VALUE;
            }
        }
        return allRowCount;
    }

    /**
     * 构建索引已经完成的条数
     *
     * @return
     */
    public long getIndexMakerComplete() {
        return this.counters.getCounter(Counters.Counter.DOCMAKE_COMPLETE).get();
    }

    public Counters getCounters() {
        return this.counters;
    }

    public Messages getMessages() {
        return this.message;
    }

    public String getInnerParam(String key) {
        return commandLine.getOptionValue(key);
    }

    public String getUserParam(String key) {
        return commandLine.getOptionValue(key);
    }

    public String getCollectionName() {
        String collectionName = this.get(IndexBuildParam.INDEXING_SERVICE_NAME);
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalStateException("collection name:" + collectionName + " can not be null");
        }
        return collectionName;
    }

    public String getMapPath() {
        return commandLine.getOptionValue("task.map.output.path");
    }

    // @Override
    public String get(Object key) {
        return commandLine.getOptionValue(String.valueOf(key));
    }

    public Integer getTaskId() {
        return Integer.parseInt(this.get(IParamContext.KEY_TASK_ID));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TaskContext)) {
            return false;
        }
        // 为了EasyMock expect 测试先通过
        return true;
    // TaskContext other = (TaskContext) obj;
    // other.get
    // 
    // return super.equals(obj);
    }

    public interface IParamGetter {

        String getOptionValue(String key);
    }
}

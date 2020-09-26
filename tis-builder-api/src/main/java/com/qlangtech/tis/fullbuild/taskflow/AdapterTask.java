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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.exec.ExecChainContextUtils;
import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年8月9日下午12:45:31
 */
public abstract class AdapterTask extends DataflowTask {

    public static final String KEY_TASK_WORK_STATUS = "TaskWorkStatus";

    // private String content;
    private ITemplateContext context;

    private ITaskContext taskContext;

    protected final Map<IDumpTable, ITabPartition> getDumpPartition() {
        Map<IDumpTable, ITabPartition> dumpPartition = ExecChainContextUtils.getDependencyTablesPartitions(this.getContext().joinTaskContext());
        return dumpPartition;
    }

    @Override
    protected Map<String, Boolean> getTaskWorkStatus() {
        return createTaskWorkStatus(this.getContext().joinTaskContext());
    }

    public static Map<String, Boolean> createTaskWorkStatus(IJoinTaskContext chainContext) {
        Map<String, Boolean> taskWorkStatus = chainContext.getAttribute(KEY_TASK_WORK_STATUS);
        if (taskWorkStatus == null) {
            taskWorkStatus = new HashMap<>();
            chainContext.setAttribute(KEY_TASK_WORK_STATUS, taskWorkStatus);
        }
        return taskWorkStatus;
    }

    public AdapterTask(String id) {
        super(id);
    }

    public abstract String getName();

    /**
     * 取得运行时Hive连接对象
     *
     * @return
     */
    protected ITaskContext getTaskContext() {
        Objects.requireNonNull(this.taskContext, "task content can not be null");
        return this.taskContext;
    }

    // {
    // return HiveTaskFactory.getConnection(this.getContext());
    // }
    // protected abstract Connection getHiveConnection() ;// {
    // return HiveTaskFactory.getConnection(this.getContext());
    // }
    @Override
    public void run() throws Exception {
        try {
            Map<String, Object> params = Collections.emptyMap();
            String sql = mergeVelocityTemplate(params);
            executeSql(this.getName(), sql);
            this.signTaskSuccess();
        } catch (Exception e) {
            this.signTaskFaild();
            throw e;
        }
    }

    // public void exexute(Map<String, Object> params) {
    // String sql = mergeVelocityTemplate(params);
    // executeSql(this.getName(), sql);
    // }
    protected String mergeVelocityTemplate(Map<String, Object> params) {
        return this.getContent();
    // StringWriter writer = new StringWriter();
    // try {
    // velocityEngine.evaluate(createContext(params), writer, "sql", this.getContent());
    // return writer.toString();
    // } catch (Exception e) {
    // throw new RuntimeException(this.getName(), e);
    // } finally {
    // IOUtils.close(writer);
    // }
    }

    protected abstract void executeSql(String taskname, String sql);

    // protected VelocityContext createContext(Map<String, Object> params) {
    // 
    // VelocityContext velocityContext = new VelocityContext();
    // velocityContext.put("context", this.getContext());
    // 
    // for (Map.Entry<String, Object> entry : params.entrySet()) {
    // velocityContext.put(entry.getKey(), entry.getValue());
    // }
    // 
    // return velocityContext;
    // }
    public abstract String getContent();

    public ITemplateContext getContext() {
        if (context == null) {
            throw new NullPointerException("TemplateContext context can not be null");
        }
        return context;
    }

    public void setContext(ITemplateContext context, ITaskContext taskContext) {
        this.context = context;
        this.taskContext = taskContext;
    }
}

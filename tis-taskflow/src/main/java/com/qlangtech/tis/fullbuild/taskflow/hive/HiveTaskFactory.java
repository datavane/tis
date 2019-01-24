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
package com.qlangtech.tis.fullbuild.taskflow.hive;

import com.qlangtech.tis.dump.hive.HiveDBUtils;
import com.qlangtech.tis.fullbuild.taskflow.ITask;
import com.qlangtech.tis.fullbuild.taskflow.ITaskFactory;
import com.qlangtech.tis.fullbuild.taskflow.TaskConfigParser;
import com.qlangtech.tis.fullbuild.taskflow.TemplateContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveTaskFactory implements ITaskFactory {

    private final HiveDBUtils hiveDBHelper;

    private static final Logger log = LoggerFactory.getLogger(HiveTaskFactory.class);

    public HiveTaskFactory() {
        super();
        this.hiveDBHelper = HiveDBUtils.getInstance();
    }

    private static final String HIVE_JDBC_CONNECTION_KEY = "hive_jdbc_connection_key";

    public static void startTaskInitialize(TemplateContext tplContext) {
        tplContext.putContextValue(HIVE_JDBC_CONNECTION_KEY, HiveDBUtils.getInstance().createConnection());
    }

    public static Connection getConnection(TemplateContext templateContext) {
        Connection conn = templateContext.getContextValue(HIVE_JDBC_CONNECTION_KEY);
        if (conn == null) {
            throw new IllegalStateException("conn can not be null");
        }
        return conn;
    }

    @Override
    public ITask createJoinTask(String textContent, TemplateContext tplContext) {
        HiveTask task = new JoinHiveTask();
        task.setContent(textContent);
        task.setContext(tplContext);
        task.setHiveDBHelper(hiveDBHelper);
        return task;
    }

    @Override
    public ITask createUnionTask(Node node, TemplateContext tplContext) {
        UnionHiveTask task = new UnionHiveTask();
        task.setTableName(TaskConfigParser.getAttr(node, "table_name", "name", false));
        task.setPartition(TaskConfigParser.getAttr(node, "partition", null, false));
        NodeList subTasks = node.getChildNodes();
        List<String> subTaskSqls = new ArrayList<>();
        for (int i = 0; i < subTasks.getLength(); i++) {
            Node subTask = subTasks.item(i);
            if (StringUtils.equals("subTab", subTask.getNodeName())) {
                subTaskSqls.add(subTask.getTextContent().trim());
            }
        }
        task.setSubTaskSqls(subTaskSqls);
        task.setContext(tplContext);
        task.setHiveDBHelper(hiveDBHelper);
        return task;
    }

    @Override
    public ITask createTask(String textContent, TemplateContext tplContext) {
        HiveTask task = new HiveTask();
        task.setContent(textContent);
        task.setContext(tplContext);
        task.setHiveDBHelper(hiveDBHelper);
        return task;
    }

    // @Override
    // public ITask createHistoryDataRemoveTask(Node node,
    // int defaultPartitionSaveCount, TemplateContext tplContext) {
    // 
    // String tableName = TaskConfigParser.getAttr(node, "tableName", null);
    // String maxPartitionSave = TaskConfigParser.getAttr(node,
    // "partitionSaveCount", null, true);
    // 
    // boolean bindtable = Boolean
    // .parseBoolean(StringUtils.defaultIfEmpty(
    // TaskConfigParser.getAttr(node, "bindtable", null, true),
    // "true"));
    // 
    // if (StringUtils.isNotBlank(maxPartitionSave)) {
    // defaultPartitionSaveCount = Integer.parseInt(maxPartitionSave);
    // }
    // RemoveJoinHistoryDataTask task = new RemoveJoinHistoryDataTask(
    // tableName, defaultPartitionSaveCount, bindtable);
    // 
    // String pt = TaskConfigParser.getAttr(node, "pt", null, true);
    // if (StringUtils.isNotBlank(pt)) {
    // task.setPt(pt);
    // }
    // task.setContext(tplContext);
    // task.setHiveDBHelper(hiveDBHelper);
    // log.info("createHistoryDataRemoveTask table:" + tableName + ",pt:"
    // + task.getPt() + ",partitionSaveCount:"
    // + defaultPartitionSaveCount + ",bindtable:" + bindtable);
    // return task;
    // }
    @Override
    public void postReleaseTask(TemplateContext tplContext) {
        Connection conn = getConnection(tplContext);
        try {
            conn.close();
        } catch (Exception e) {
        }
    }
}

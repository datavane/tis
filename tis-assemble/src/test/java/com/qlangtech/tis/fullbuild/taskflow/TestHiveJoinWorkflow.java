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

// import com.google.common.collect.Maps;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestHiveJoinWorkflow extends TestCase {

    public void test() throws Exception {
    // 执行hiveworkflow工作流
    // InputStream joinStream = this.getClass().getResourceAsStream("join.xml");
    // int taskid = 123;
    // JoinPhaseStatus joinPhaseStatus = new JoinPhaseStatus(taskid);
    // Map<EntityName, ERRules.TabFieldProcessor> dumpNodeExtraMetaMap = Collections.emptyMap();
    // WorkflowTaskConfigParser parser = new WorkflowTaskConfigParser(
    // new HiveTaskFactory(dumpNodeExtraMetaMap), (execContext) -> {
    // try {
    // return IOUtils.toString(joinStream, "utf8");
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }, joinPhaseStatus);
    // 
    // DefaultChainContext ccontext = new DefaultChainContext(new TestParamContext());
    // ccontext.setFileSystem(TISHdfsUtils.getFileSystem());
    // //${context.date}
    // ccontext.setPs("20171117142045");
    // 
    // Map<IDumpTable, ITabPartition> pts = Maps.newHashMap();
    // pts.put(DumpTable.create("", ""), () -> "pt");
    // ExecChainContextUtils.setDependencyTablesPartitions(ccontext, pts);
    // TemplateContext tplContext = new TemplateContext(ccontext);
    // parser.startJoinSubTables(tplContext);
    }
}

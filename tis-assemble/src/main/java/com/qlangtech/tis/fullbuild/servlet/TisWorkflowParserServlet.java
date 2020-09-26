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
package com.qlangtech.tis.fullbuild.servlet;

import com.qlangtech.tis.fullbuild.taskflow.TaskWorkflow;
import com.qlangtech.tis.fullbuild.taskflow.WorkflowTaskConfigParser;
import com.qlangtech.tis.git.GitUtils;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 通过索引名称 取得索引workflow的依赖关系，提供给页面上的vis-4.21.0来绘制工作流的可视化图
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年12月4日
 */
public class TisWorkflowParserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String workflowName = req.getParameter("wfname");
            if (StringUtils.isEmpty(workflowName)) {
                throw new IllegalArgumentException("param 'workflowName' can not be null");
            }
            // 日常测试
            GitUtils.GitBranchInfo branch = GitUtils.GitBranchInfo.$(GitUtils.GitBranch.DEVELOP);
            WorkflowTaskConfigParser wfParser = WorkflowTaskConfigParser.getInstance(workflowName, branch);
            TaskWorkflow workflow = wfParser.getWorkflow();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    // try {
    // InputStream joinStream = this.getClass().getResourceAsStream("join.xml");
    // int taskid = 123;
    // JoinPhaseStatus joinPhaseStatus = new JoinPhaseStatus(taskid);
    // WorkflowTaskConfigParser parser = new WorkflowTaskConfigParser(new
    // HiveTaskFactory(), (execContext) -> {
    // try {
    // return IOUtils.toString(joinStream, "utf8");
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }, joinPhaseStatus);
    // 
    // DefaultChainContext ccontext = new DefaultChainContext(new
    // MockParamContext());
    // ccontext.setFileSystem(TISHdfsUtils.getFileSystem());
    // // ${context.date}
    // ccontext.setPs("20171117142045");
    // 
    // Map<DumpTable, Partition> pts = Maps.newHashMap();
    // pts.put(DumpTable.create("", ""), () -> "pt");
    // ExecChainContextUtils.setDependencyTablesPartitions(ccontext, pts);
    // TemplateContext tplContext = new TemplateContext(ccontext);
    // parser.startJoinSubTables(tplContext);
    // } catch (Exception e) {
    // 
    // e.printStackTrace();
    // }
    }
}

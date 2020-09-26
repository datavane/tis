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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;

/**
 * 通过taskid取全量执行状态
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年7月12日
 */
public class TaskStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int taskid = Integer.parseInt(req.getParameter("taskid"));
        // 是否要获取全部的日志信息，比如dump已經完成了，那麼只需要獲取dump之後的日志信息
        // boolean all = Boolean.parseBoolean(req.getParameter("all"));
        PhaseStatusCollection statusSet = TrackableExecuteInterceptor.taskPhaseReference.get(taskid);
        JSONObject result = new JSONObject();
        boolean success = false;
        if (statusSet != null) {
            result.put("status", statusSet);
            success = true;
        }
        result.put("success", success);
        IOUtils.write(JSON.toJSONString(result, true), res.getWriter());
    }
}

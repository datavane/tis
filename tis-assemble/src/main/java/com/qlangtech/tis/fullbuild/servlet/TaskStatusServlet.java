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
package com.qlangtech.tis.fullbuild.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.util.DescriptorsJSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 通过taskid取全量执行状态
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年7月12日
 */
public class TaskStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TaskStatusServlet.class);

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String extendPoint = null;
        try {
            if (StringUtils.isNotEmpty(extendPoint = req.getParameter(DescriptorsJSON.KEY_EXTEND_POINT))) {
                PluginStore<Describable> pluginStore = TIS.getPluginStore((Class<Describable>) Class.forName(extendPoint));
                pluginStore.cleanPlugins();
                logger.info("key of '{}' pluginStore has been clean", extendPoint);
                return;
            }
        } catch (ClassNotFoundException e) {
            throw new ServletException("clean plugin store cache faild ", e);
        }

        if (Boolean.parseBoolean(req.getParameter(TIS.KEY_ACTION_CLEAN_TIS))) {
            TIS.clean();
            logger.info(" clean TIS cache", extendPoint);
            return;
        }


        int taskid = Integer.parseInt(req.getParameter(IParamContext.KEY_TASK_ID));
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

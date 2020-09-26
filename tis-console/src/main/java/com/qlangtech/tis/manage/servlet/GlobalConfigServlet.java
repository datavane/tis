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
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-7
 */
public class GlobalConfigServlet extends BasicServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final RunEnvironment runtime = RunEnvironment.getEnum(req.getParameter("runtime"));
        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
        final Map<String, String> params = new HashMap<String, String>();
        for (ResourceParameters param : this.getContext().getResourceParametersDAO().selectByExample(criteria)) {
            params.put(param.getKeyName(), getParameterValue(param, runtime));
        }
        resp.setContentType("text/json");
        JSONObject json = new JSONObject(params);
        IOUtils.write(json.toString(), resp.getOutputStream());
    }

    public static String getParameterValue(ResourceParameters param, RunEnvironment runtime) {
        Assert.assertNotNull(param);
        Assert.assertNotNull(runtime);
        String paramValue = null;
        switch(runtime) {
            case DAILY:
                paramValue = param.getDailyValue();
                break;
            // break;
            case ONLINE:
                paramValue = param.getOnlineValue();
                break;
            default:
                throw new IllegalArgumentException("runtime:" + runtime + " is invalid");
        }
        return paramValue;
    }
}

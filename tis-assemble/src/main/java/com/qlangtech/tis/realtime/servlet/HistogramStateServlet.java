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
package com.qlangtech.tis.realtime.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.qlangtech.tis.realtime.servlet.RealtimeStatePageServlet.RowPair;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.manage.common.TISCollectionUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月10日
 */
public class HistogramStateServlet extends javax.servlet.http.HttpServlet {

    private static final long serialVersionUID = 1L;

    private Collection<IOnsListenerStatus> incrChannels;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        IndexSwapTaskflowLauncher launcherContext = IndexSwapTaskflowLauncher.getIndexSwapTaskflowLauncher(config.getServletContext());
        this.incrChannels = launcherContext.getIncrChannels();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<RowPair> stats = new ArrayList<>();
        RowPair p = null;
        for (IOnsListenerStatus stat : this.incrChannels) {
            if (p == null) {
                p = new RowPair();
            }
            if (!p.add(stat)) {
                // 放满了
                stats.add(p);
                p = null;
            }
        }
        req.setAttribute("stats", stats);
        req.getRequestDispatcher("/vm/realtime_state.vm").forward(req, resp);
    }
}

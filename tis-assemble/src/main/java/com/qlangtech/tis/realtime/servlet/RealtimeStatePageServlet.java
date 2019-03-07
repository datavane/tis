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
package com.qlangtech.tis.realtime.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.manage.common.TISCollectionUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RealtimeStatePageServlet extends javax.servlet.http.HttpServlet {

    private static final long serialVersionUID = 1L;

    private Collection<IOnsListenerStatus> incrChannels;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        IndexSwapTaskflowLauncher launcherContext = IndexSwapTaskflowLauncher.getIndexSwapTaskflowLauncher(config.getServletContext());
        this.incrChannels = launcherContext.getIncrChannels();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
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
        req.getRequestDispatcher("/vm/realtime_histogram.vm").forward(req, res);
    }

    private static final int ARRAY_LENGTH = 2;

    public static class RowPair {

        private IOnsListenerStatus[] pair = new IOnsListenerStatus[ARRAY_LENGTH];

        int index = 0;

        public boolean add(IOnsListenerStatus stat) {
            pair[index++] = stat;
            return index < ARRAY_LENGTH;
        }

        public IOnsListenerStatus getLeft() {
            return pair[0];
        }

        public IOnsListenerStatus getRight() {
            return pair[1];
        }
    }
}

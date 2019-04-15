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
package com.qlangtech.tis.checkhealth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import com.dihuo.app.common.monitor.ServletContextAware;
//import com.dihuo.app.common.monitor.StatusChecker;
//import com.dihuo.app.common.monitor.enums.StatusLevel;
//import com.dihuo.app.common.monitor.model.StatusModel;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
//public class TisMonitorFilter implements Filter {
//
//    private List<StatusChecker> statusCheckerList = new ArrayList<StatusChecker>();
//
//    @SuppressWarnings("all")
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        try {
//            ServiceLoader<StatusChecker> sl = ServiceLoader.load(StatusChecker.class);
//            for (StatusChecker statusChecker : sl) {
//                statusCheckerList.add(statusChecker);
//            }
//            Collections.sort(statusCheckerList, new Comparator<StatusChecker>() {
//
//                @Override
//                public int compare(StatusChecker o1, StatusChecker o2) {
//                    if (o1.order() < o2.order()) {
//                        return -1;
//                    } else if (o1.order() == o2.order()) {
//                        return 0;
//                    } else {
//                        return 1;
//                    }
//                }
//            });
//            for (StatusChecker statusChecker : statusCheckerList) {
//                if (statusChecker instanceof ServletContextAware) {
//                    ((ServletContextAware) statusChecker).setServletContext(filterConfig.getServletContext());
//                }
//                try {
//                    statusChecker.init();
//                } catch (Throwable e) {
//                }
//            }
//        } catch (Exception e) {
//            throw new ServletException(e);
//        }
//    }
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest request = (HttpServletRequest) req;
//        HttpServletResponse response = (HttpServletResponse) resp;
//        String requestURI = request.getRequestURI();
//        if (requestURI.endsWith("/check_health")) {
//            doCheckHealth(request, response);
//            return;
//        }
//        throw new IllegalStateException("requestURI:" + requestURI + " is invalid");
//    }
//
//    @Override
//    public void destroy() {
//    }
//
//    private void doCheckHealth(HttpServletRequest request, HttpServletResponse resp) throws IOException {
//        boolean ok = true;
//        StringBuffer errDesc = new StringBuffer();
//        for (StatusChecker sc : statusCheckerList) {
//            StatusModel model = sc.check();
//            if (model.level == StatusLevel.FAIL) {
//                ok = false;
//                resp.getWriter().print("fail, Check is " + sc.getClass().getSimpleName() + " message is " + model.message);
//            }
//            errDesc.append("StatusChecker: ").append(sc.getClass().getSimpleName());
//            errDesc.append(" Status: ").append(model.level);
//            errDesc.append(" message: ").append(model.message);
//            errDesc.append("\r\n");
//        }
//        if (ok) {
//            resp.getWriter().print("ok");
//        }
//        return;
//    }
//}

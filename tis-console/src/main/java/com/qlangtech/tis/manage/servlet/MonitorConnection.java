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
package com.qlangtech.tis.manage.servlet;

import javax.websocket.Session;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.trigger.socket.ExecuteState;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class MonitorConnection {

    public final Session session;

    MonitorConnection(Session session) {
        super();
        this.session = session;
    }

    public abstract boolean isAccept(ExecuteState stat);

    public static class FucusIndex extends MonitorConnection {

        private String serviceName;

        public FucusIndex(Session session, String serviceName) {
            super(session);
            if (!StringUtils.startsWith(serviceName, "search4")) {
                throw new IllegalArgumentException("serviceName:" + serviceName + " shall start with 'search4'");
            }
            this.serviceName = serviceName;
        }

        @Override
        public boolean isAccept(ExecuteState stat) {
            return StringUtils.equals(this.serviceName, stat.getCollectionName());
        }
    }

    public static class FocusTask extends MonitorConnection {

        private Integer taskid;

        public FocusTask(Session session, Integer taskid) {
            super(session);
            this.taskid = taskid;
        }

        @Override
        public boolean isAccept(ExecuteState stat) {
            return (taskid + 0) == stat.getTaskId();
        }
    }
}

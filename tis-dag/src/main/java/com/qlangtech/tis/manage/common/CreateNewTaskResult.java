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

package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.Application;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-03 16:42
 **/
public class CreateNewTaskResult {

    private int taskid;

    private Application app;

    public CreateNewTaskResult() {
    }

    public CreateNewTaskResult(int taskid, Application app) {
        super();
        this.taskid = taskid;
        this.app = app;
    }

    public void setTaskid(int taskid) {
        this.taskid = taskid;
    }

    public void setApp(Application app) {
        this.app = app;
    }


    public int getTaskid() {
        return taskid;
    }

    public Application getApp() {
        return app;
    }
}

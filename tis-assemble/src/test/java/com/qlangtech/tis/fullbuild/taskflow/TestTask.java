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

import java.util.Map;
import com.qlangtech.tis.fullbuild.taskflow.BasicTask;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年12月1日
 */
public class TestTask extends BasicTask {

    public TestTask(String name, String to) {
        this.setName(name);
        this.setSuccessTo(to);
    }

    @Override
    public void exexute(Map<String, Object> params) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("task " + this.getName() + " execute");
    }
}

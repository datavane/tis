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
package com.qlangtech.tis.fullbuild.taskflow.impl;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.fullbuild.taskflow.BasicTask;

/**
 * 开始节点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年11月30日
 */
public class StartTask extends BasicTask {

    public static final String NAME = "start";

    private static final Logger logger = LoggerFactory.getLogger(StartTask.class);

    public StartTask() {
        super();
        // this.setName(NAME);
        this.name = NAME;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public void exexute(Map<String, Object> params) {
        logger.info("start");
        System.out.println("start execute");
    }
}

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.fullbuild.taskflow.BasicTask;

/**
 * workflow最终结束的节点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年11月30日
 */
public class EndTask extends BasicTask {

    private static final Logger logger = LoggerFactory.getLogger(EndTask.class);

    public EndTask() {
        super();
        this.countDown = new CountDownLatch(1);
    }

    private final CountDownLatch countDown;

    @Override
    public void exexute(Map<String, Object> params) {
        logger.info("end task execute");
        System.out.println("end task execute");
        this.countDown.countDown();
    }

    @Override
    public void setSuccessTo(String successTo) {
    }

    public void await() throws InterruptedException {
        this.countDown.await(1, TimeUnit.HOURS);
    }
}

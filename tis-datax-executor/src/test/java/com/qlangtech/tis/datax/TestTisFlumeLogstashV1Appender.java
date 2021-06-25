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

package com.qlangtech.tis.datax;

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.order.center.IParamContext;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-25 15:04
 **/
public class TestTisFlumeLogstashV1Appender extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(TestTisFlumeLogstashV1Appender.class);

    public void testSendMsg() throws Exception {
        Config.setTest(false);
        MDC.put(IParamContext.KEY_TASK_ID, String.valueOf(999));
        MDC.put(TISCollectionUtils.KEY_COLLECTION, "baisui");
        int i = 0;
        while (true) {
            logger.info("i am so hot:" + (i++));
            Thread.sleep(1000l);
            System.out.println("send turn:" + i);
        }
    }
}

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
package com.qlangtech.tis.manage;

import com.qlangtech.tis.manage.common.CenterResource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-23 09:49
 */
public class ConsoleInitilizeListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CenterResource.setNotFetchFromCenterRepository();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}

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
package com.qlangtech.tis.spring;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月6日 下午5:01:48
 */
public class ResourceXmlApplicationContext extends AbstractXmlApplicationContext {

    private final Resource[] resouce;

    public ResourceXmlApplicationContext(Resource[] resouce) {
        super();
        this.resouce = resouce;
        this.refresh();
    }

    @Override
    protected Resource[] getConfigResources() {
        return this.resouce;
    }
}

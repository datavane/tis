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
package com.qlangtech.tis.runtime.module.screen;

import com.alibaba.citrus.turbine.Context;

/**
 * 全局扩展资源配置，例如，solrcore中为某个应用配置了个性化分词器，<br>
 * 打成了jar包，以往需要通过手工将这个jar包部署到solrcore 服务器上<br>
 * 现在，事先将这个分词jar包部署到终搜后台中
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-6-1
 */
public class GlobalResource {

    /**
     */
    private static final long serialVersionUID = 1L;

    public static final String UPLOAD_RESOURCE_TYPE_GLOBAL = "global_res";
    // @Override
    // public void execute(Context context) throws Exception {
    // }
}

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
package com.qlangtech.tis.cloud;

/**
 * 工厂，创建 ITisCloudClient
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-03-13 11:48
 */
public interface ITisCloudClientFactory {

    int TEST_MOCK = 3;

    int REAL_TIME_ROCKETMQ = 1;

    ITisCloudClient create();

    /**
     * 优先级
     * @return
     */
    int getTypeCode();
}

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

/**
 * 打宽表瀑布执行流
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年8月9日上午10:47:26
 */
public interface ITask {

    /**
     * 执行
     */
    public void exexute(Map<String, Object> params);

    // public void exexute();
    /**
     * 节点名称
     * @return
     */
    public String getName();
}

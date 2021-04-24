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

import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:36
 */
public interface IDataxWriter extends IDataXPluginMeta {
    public String getTemplate();

    /**
     * 取得子任务
     *
     * @return
     */
    default IDataxContext getSubTask() {
        return getSubTask(Optional.empty());
    }

    /**
     * 取得子任务
     *
     * @return
     */
    IDataxContext getSubTask(Optional<IDataxProcessor.TableMap> tableMap);
}

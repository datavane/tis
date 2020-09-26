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
package com.qlangtech.tis.collectinfo.api;

import com.qlangtech.tis.CoreStatisticsReportHistory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年10月9日上午9:57:33
 */
public interface ICoreService {

    CoreStatisticsReportHistory getCoreStatisticsReportHistory();
}

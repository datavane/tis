/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.datax;

import java.io.File;

/**
 * DataX 执行器取得指定的本地执行输出日志的路径
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-09-02 11:28
 **/
public interface ISpecifiedLocalLogger {
    /**
     * 取得指定的本地执行输出日志的路径
     *
     * @return
     * // @see com.qlangtech.tis.manage.common.Config# EXEC_LOCAL_LOGGER_FILE_PATH
     */
    public default File getSpecifiedLocalLoggerPath() {
        return null;
    }
}

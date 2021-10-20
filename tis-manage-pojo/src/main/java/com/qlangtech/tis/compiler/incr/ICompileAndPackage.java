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

package com.qlangtech.tis.compiler.incr;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.compiler.java.FileObjectsContext;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.sql.parser.IDBNodeMeta;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 在主项目中对模块生成的代码进行隔离，可以在TIS自定义插件中进行扩展
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-20 17:01
 **/
public interface ICompileAndPackage {

    /**
     * @param context
     * @param msgHandler
     * @param appName
     * @param dbNameMap
     * @param sourceRoot
     * @param xmlConfigs 取得spring配置文件相关resourece
     * @throws Exception
     */
    void process(Context context, IControlMsgHandler msgHandler
            , String appName, Map<IDBNodeMeta, List<String>> dbNameMap, File sourceRoot, FileObjectsContext xmlConfigs) throws Exception;
}

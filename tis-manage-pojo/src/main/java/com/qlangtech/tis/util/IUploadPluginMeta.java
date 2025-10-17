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

package com.qlangtech.tis.util;

/**
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-10 09:34
 **/
public interface IUploadPluginMeta {
    String KEY_JSON_MANIPULATE_TARGET = "manipulateTarget";
    String KEY_JSON_MANIPULATE_BOOL_UPDATE_PROCESS = "updateProcess";
    /**
     * 是否执行删除
     */
    String KEY_JSON_MANIPULATE_BOOL_DELETE_PROCESS = "deleteProcess";
    public IUploadPluginMeta putExtraParams(String key, String val);
}

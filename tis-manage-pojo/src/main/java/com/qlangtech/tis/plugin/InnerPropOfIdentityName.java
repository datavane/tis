/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin;

/**
 * 一个内嵌的属性实体，在一个IdentityName 插件内部，运行时会将Container Plugin的IdentityName 设置到内部属性类上
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-04 11:28
 **/
public interface InnerPropOfIdentityName {

    public void setIdentity(IdentityName id);
}

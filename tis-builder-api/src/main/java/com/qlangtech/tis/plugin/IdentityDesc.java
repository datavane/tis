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
 * 在pluginAction中保存plguin内容之后，默认向页面返回identity值，在某些场景下需要向页面中返回特定的Bean内容信息，例如TISLicense 保存后需要向页面返回validate结果：HasExpire
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-11-18 18:17
 * //@see TISLicense
 **/
public interface IdentityDesc<T> {
    public T describePlugin();
}

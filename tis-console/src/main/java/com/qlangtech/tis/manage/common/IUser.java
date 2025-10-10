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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年7月26日下午5:10:29
 */
public interface IUser extends ILoginUser {

  public UsrDptRelation getUsr();

  public boolean hasLogin();

  public abstract String getEmail();

  public abstract void setEmail(String email);

  public abstract String getId();

  public IAppsFetcher getAppsFetcher();

  public boolean hasGrantAuthority(String func);

  public Integer getDepartmentid();
}

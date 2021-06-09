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
package com.qlangtech.tis.manage.common;

import com.alibaba.citrus.turbine.Context;
import com.opensymphony.xwork2.ActionContext;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import junit.framework.Assert;

import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MockContext implements Context {

  public static final MockContext instance = new MockContext();

  public static AjaxValve.ActionExecResult getActionExecResult() {
    return new AjaxValve.ActionExecResult(instance).invoke();
  }

  // private final ActionContext actionContext;
  //
  // public MockContext() {
  // super();
  // this.actionContext = ActionContext.getContext();
  // }
  @Override
  public boolean containsKey(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(String key) {
    Assert.assertNotNull("ActionContext.getContext() can not be null", ActionContext.getContext());
    return ActionContext.getContext().get(key);
  }

  @Override
  public boolean hasErrors() {

    if (this.get(IFieldErrorHandler.ACTION_ERROR_FIELDS) != null && !((List<Object>) this.get(IFieldErrorHandler.ACTION_ERROR_FIELDS)).isEmpty()) {
      return true;
    }

    if (this.get(IMessageHandler.ACTION_ERROR_MSG) != null && !((List<String>) this.get(IMessageHandler.ACTION_ERROR_MSG)).isEmpty()) {
      return true;
    }

    return false;
  }

  @Override
  public Set<String> keySet() {
    return null;
  }

  @Override
  public void put(String key, Object value) {

//    if (value instanceof DatasourceDb) {
//      throw new IllegalStateException(value.toString());
//    }

    ActionContext.getContext().put(key, value);
  }

  @Override
  public void remove(String key) {
  }
}

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
package com.qlangtech.tis;

import com.google.common.collect.Lists;
import org.apache.struts2.StrutsSpringTestCase;
import org.easymock.EasyMock;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-06 18:04
 */
public class BasicActionTestCase extends StrutsSpringTestCase {

  private static List<Object> mocks = Lists.newArrayList();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.clearMocks();
  }

  protected void clearMocks() {
    this.mocks.clear();
  }

  protected void verifyAll() {
    this.mocks.forEach((r) -> {
      EasyMock.verify(r);
    });
  }

  public <T> T mock(String name, Class<?> toMock) {
    Object mock = EasyMock.createMock(name, toMock);
    this.mocks.add(mock);
    return (T) mock;
  }

  public void replay() {
    mocks.forEach((r) -> {
      EasyMock.replay(r);
    });
  }

  @Override
  protected String[] getContextLocations() {
    return new String[]{"classpath:/tis.application.context.xml", "classpath:/tis.test.context.xml"};
  }
}

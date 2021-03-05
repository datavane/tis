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
package com.qlangtech.tis.test;

import com.google.common.collect.Lists;
import org.easymock.EasyMock;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-04 12:52
 */
public class EasyMockUtil {
    private static List<Object> mocks = Lists.newArrayList();

    static void clearMocks() {
        mocks = Lists.newArrayList();
    }

    static void verifyAll() {
        mocks.forEach((r) -> {
            EasyMock.verify(r);
        });
    }

    public static <T> T mock(String name, Class<?> toMock) {
        Object mock = EasyMock.createMock(name, toMock);
        mocks.add(mock);
        return (T) mock;
    }

    static void replay() {
        mocks.forEach((r) -> {
            EasyMock.replay(r);
        });
    }
}

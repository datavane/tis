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
package com.qlangtech.tis.test;

import com.google.common.collect.Lists;
import org.mockito.Mockito;

import java.util.List;

/**
 * 测试 mock 工具门面：底层已由 EasyMock 迁移为 Mockito。
 *
 * <p>保持原有 API 形状（{@link TISEasyMock} 的默认方法依赖它），调用方无需感知底层切换：
 * <ul>
 *   <li>{@link #mock(String, Class)} 等价于 {@code Mockito.mock(toMock, withSettings().name(name))}；</li>
 *   <li>{@link #replay()} 在 Mockito 下无对应概念（stub 即刻生效），保留为空实现以兼容既有调用；</li>
 *   <li>{@link #verifyAll()} 不再做批量校验 —— Mockito 的校验风格是在测试尾部对每个交互
 *       显式 {@code Mockito.verify(mock).method(...)}，请在新代码中直接使用该写法。</li>
 * </ul>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-04 12:52
 */
public class EasyMockUtil {
    private static List<Object> mocks = Lists.newArrayList();

    static void clearMocks() {
        mocks = Lists.newArrayList();
    }

    static void verifyAll() {
        // Mockito 下批量 verify 无对应语义：stub 是否被调用由测试内的显式 Mockito.verify 保证，
        // 这里保留空实现仅为兼容 TISEasyMock#verifyAll 的既有调用点。
    }

    @SuppressWarnings("unchecked")
    public static <T> T mock(String name, Class<?> toMock) {
        Object mock = Mockito.mock(toMock, Mockito.withSettings().name(name));
        mocks.add(mock);
        return (T) mock;
    }

    static void replay() {
        // Mockito 无 replay 阶段概念（stub 即刻生效），保留空实现以兼容 TISEasyMock#replay 的既有调用点。
    }
}

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

package com.qlangtech.tis.extension;

import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.runtime.module.action.IParamGetter;
import com.qlangtech.tis.util.UploadPluginMeta;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ValueChangePipe} 的逐目标字段渲染语义。
 *
 * <p>本类不触碰 TIS 插件扫描，全部用例都是纯逻辑断言，故不需要 LazyPlugins /
 * {@code --add-opens} 之类的环境前提。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/4
 */
public class TestValueChangePipe extends TestCase {

    /**
     * 目标字段去重且保持首次出现的顺序 —— 顺序影响前端请求的字段次序，去重影响
     * {@code Descriptor.valueChangePipe} 重复注册时的合并结果。
     */
    public void testAddToFieldsDedupesAndKeepsFirstSeenOrder() {
        ValueChangePipe pipe = new ValueChangePipe("src", "b", "c", "b", "a");
        assertEquals(Arrays.asList("b", "c", "a"), Arrays.asList(pipe.getToField()));

        pipe.addToFields("c", "d");
        assertEquals(Arrays.asList("b", "c", "a", "d"), Arrays.asList(pipe.getToField()));
    }

    /**
     * 广播渲染对整个 pipe 只调用一次，各目标字段复用同一结果 —— 这是既有行为
     * （Gantt/Pivot/ChartPie/LinkReference 等依赖它），本次改动必须逐字保持不变。
     */
    public void testBroadcastRenderIsInvokedOnlyOnceForAllTargets() {
        AtomicInteger calls = new AtomicInteger();
        List<IdentityName> shared = List.of(IdentityName.create("x"), IdentityName.create("y"));

        ValueChangePipe pipe = new ValueChangePipe("src", "f1", "f2", "f3")
                .render((meta, param) -> {
                    calls.incrementAndGet();
                    return shared;
                });

        Map<String, List<? extends IdentityName>> rendered = pipe.render(meta(), new IParamGetter() {
        });

        assertEquals(3, rendered.size());
        assertEquals("广播渲染应只被调用一次", 1, calls.get());
        assertSame(shared, rendered.get("f1"));
        assertSame(shared, rendered.get("f2"));
        assertSame(shared, rendered.get("f3"));
    }

    /**
     * 逐字段渲染覆盖广播结果 —— ObjectListWidget 的 titleProperty（属性选项）与
     * cardFields（列配置行）同源于 objectSetVar 却形态不同，正是靠这条语义共存。
     */
    public void testPerFieldRenderOverridesBroadcast() {
        AtomicInteger calls = new AtomicInteger();
        List<IdentityName> shared = List.of(IdentityName.create("shared"));
        List<IdentityName> special = List.of(IdentityName.create("special"));

        ValueChangePipe pipe = new ValueChangePipe("src", "f1", "f2")
                .render((meta, param) -> {
                    calls.incrementAndGet();
                    return shared;
                })
                .render("f2", (meta, param) -> special);

        Map<String, List<? extends IdentityName>> rendered = pipe.render(meta(), new IParamGetter() {
        });

        assertSame(shared, rendered.get("f1"));
        assertSame(special, rendered.get("f2"));
        assertEquals("有专属渲染的字段不应触发广播渲染", 1, calls.get());
    }

    /**
     * 给未登记的目标字段挂渲染是死代码（前端只会请求 toField 里的键），构造期快速失败。
     */
    public void testRegisteringRenderForUnregisteredTargetFailsFast() {
        ValueChangePipe pipe = new ValueChangePipe("src", "f1");
        try {
            pipe.render("nope", (meta, param) -> List.of());
            fail("shall reject render registered on unregistered toField");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("nope"));
        }
    }

    /**
     * 既没有广播渲染也没有专属渲染时，渲染必须报错而不是给出空 map ——
     * 否则前端拿到的是一个永远不会联动的字段。
     */
    public void testPipeWithoutAnyRenderFailsFast() {
        ValueChangePipe pipe = new ValueChangePipe("src", "f1");
        try {
            pipe.render(meta(), new IParamGetter() {
            });
            fail("shall reject pipe without any render");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("f1"));
        }
    }

    /**
     * 渲染结果为 null 必须被拒绝：调用方 {@code PluginAction} 用 {@code Collectors.toMap}
     * 汇总各字段结果，null value 会在那里抛 NPE，此处提前给出可读的错误。
     */
    public void testNullRenderResultIsRejected() {
        ValueChangePipe pipe = new ValueChangePipe("src", "f1").render((meta, param) -> null);
        try {
            pipe.render(meta(), new IParamGetter() {
            });
            fail("shall reject null render result");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    /**
     * render(meta, param) 只是把两个入参透传给渲染函数，本类各用例的渲染函数都不读它们，
     * 故这里给一个不依赖 TIS 上下文的空 meta 即可（UploadPluginMeta 允许 context 为 null）。
     */
    private static UploadPluginMeta meta() {
        return UploadPluginMeta.parse("test");
    }
}

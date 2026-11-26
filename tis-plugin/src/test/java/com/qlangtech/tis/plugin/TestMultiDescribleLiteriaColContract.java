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

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.datax.transformer.PluginLiteria;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.util.DescribableJSON;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@code colsConfig()} 中 {@link MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig#literiaDescCol()}
 * literia 虚拟列的接口契约：声明该列的元素类必须同时实现 {@link PluginLiteria}（列值来源
 * {@code getLiteria()}）与 {@link DescribableJSON.AppendPayloadAttribute}（载荷下发通道
 * {@code createPayloadProps}），缺一个 {@code getExtractProps} 就强制抛
 * {@code IllegalStateException}。
 *
 * <h3>为什么必须有这个测试</h3>
 * 该列是虚拟列（titleKey 为哨兵值 {@code $literia}，不对应任何 {@code @FormField} 字段），
 * 漏配不会有任何编译期信号：列值前端取不到也只是静默渲染成空白列。接口契约本身已在
 * {@code getExtractProps} 里强制校验，本测试钉住的是<b>校验确实会被触发</b>——
 * 校验代码自身被删掉或挪进永远不会走的分支时，这里首先红。
 *
 * <p>夹具仿 {@code TestDescribableJSONParentChain}：descriptor 必须是元素类的<b>嵌套</b>类，
 * {@code clazz} 由 enclosing class 推断（见 {@code Descriptor#Descriptor()}），放平级会抛
 * {@code Outer class ... is not assignable ...}。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/25
 * @see MultiDescribleElementSetSelector.MultiDescribleElementDescriptor#getExtractProps(boolean)
 */
public class TestMultiDescribleLiteriaColContract {

    /**
     * 哨兵键的形状：{@code $} + {@link PluginLiteria#KEY_LITERIA}。与
     * {@code ColConfig.literiaDescCol().getTitleKey()} 逐字对齐，前端按它识别虚拟列。
     */
    @Test
    public void literiaSentinelKeyShape() {
        assertEquals("$" + PluginLiteria.KEY_LITERIA,
                MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig
                        .literiaDescCol().getTitleKey());

        MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig literiaCol
                = MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig.literiaDescCol();
        assertTrue("literiaDescCol() 产物必须被 isLiteriaDescCol() 认出", literiaCol.isLiteriaDescCol());

        MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig plainCol
                = new MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig("name");
        assertFalse("普通字段列不得被误判成 literia 虚拟列", plainCol.isLiteriaDescCol());
    }

    /**
     * 双接口齐备：{@code getExtractProps} 放行，colsCfg 照常下发（含 clickable 列与 literia 虚拟列）。
     */
    @Test
    public void conformingElementPublishesLiteriaCol() {
        Map<String, Object> props = new LiteriaElement.Desc().getExtractProps();

        @SuppressWarnings("unchecked")
        List<MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig> colsCfg
                = (List<MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig>) props.get("colsCfg");
        assertEquals("两列都要下发", 2, colsCfg.size());
        assertTrue("literia 虚拟列必须在 colsCfg 里",
                colsCfg.stream().anyMatch(MultiDescribleElementSetSelector
                        .MultiDescribleElementDescriptor.ColConfig::isLiteriaDescCol));
    }

    /**
     * 只实现 {@link PluginLiteria}（缺载荷下发通道）：列值算出来了也送不到前端，必须拦下。
     */
    @Test
    public void missingAppendPayloadAttributeIsRejected() {
        try {
            new LiteriaOnlyElement.Desc().getExtractProps();
            fail("缺 DescribableJSON.AppendPayloadAttribute 必须抛 IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue("异常消息要指名缺失的接口，实际:" + e.getMessage(),
                    e.getMessage().contains(DescribableJSON.AppendPayloadAttribute.class.getSimpleName()));
        }
    }

    /**
     * 只实现 {@link DescribableJSON.AppendPayloadAttribute}（缺列值来源）：载荷里没有 literia 键，
     * 同样必须拦下。
     */
    @Test
    public void missingPluginLiteriaIsRejected() {
        try {
            new PayloadOnlyElement.Desc().getExtractProps();
            fail("缺 PluginLiteria 必须抛 IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue("异常消息要指名缺失的接口，实际:" + e.getMessage(),
                    e.getMessage().contains(PluginLiteria.class.getSimpleName()));
        }
    }

    // ------------------------------------------------------------------
    //  夹具
    // ------------------------------------------------------------------

    /**
     * 合规元素：双接口齐备，Table 视图 + clickable 列 + literia 虚拟列。
     */
    public static class LiteriaElement implements Describable<LiteriaElement>
            , IPluginStore.MultiDescribleElement, PluginLiteria, DescribableJSON.AppendPayloadAttribute {

        @Override
        public String identityValue() {
            return "literia-element";
        }

        @Override
        public Descriptor<LiteriaElement> getDescriptor() {
            return new Desc();
        }

        @Override
        public List<UDFDesc> getLiteria() {
            return List.of();
        }

        @Override
        public Map<String, Object> createPayloadProps(List<Describable<?>> parent) {
            return Map.of(PluginLiteria.KEY_LITERIA, getLiteria());
        }

        public static class Desc
                extends MultiDescribleElementSetSelector.MultiDescribleElementDescriptor<LiteriaElement> {
            public Desc() {
                super();
            }

            @Override
            public String getDisplayName() {
                return "合规元素";
            }

            @Override
            public String shortComment() {
                return "双接口齐备";
            }

            @Override
            public MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ViewStyle viewStyle() {
                return MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ViewStyle.Table;
            }

            @Override
            public List<MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig> colsConfig() {
                return List.of( //
                        new MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig("name")
                                .setClickable() //
                        , MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig
                                .literiaDescCol());
            }
        }
    }

    /**
     * 缺载荷下发通道：只实现 {@link PluginLiteria}。
     */
    public static class LiteriaOnlyElement implements Describable<LiteriaOnlyElement>
            , IPluginStore.MultiDescribleElement, PluginLiteria {

        @Override
        public String identityValue() {
            return "literia-only";
        }

        @Override
        public Descriptor<LiteriaOnlyElement> getDescriptor() {
            return new Desc();
        }

        @Override
        public List<UDFDesc> getLiteria() {
            return List.of();
        }

        public static class Desc
                extends MultiDescribleElementSetSelector.MultiDescribleElementDescriptor<LiteriaOnlyElement> {
            public Desc() {
                super();
            }

            @Override
            public String getDisplayName() {
                return "缺载荷通道";
            }

            @Override
            public String shortComment() {
                return "只实现 PluginLiteria";
            }

            @Override
            public List<MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig> colsConfig() {
                return List.of(MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig
                        .literiaDescCol());
            }
        }
    }

    /**
     * 缺列值来源：只实现 {@link DescribableJSON.AppendPayloadAttribute}。
     */
    public static class PayloadOnlyElement implements Describable<PayloadOnlyElement>
            , IPluginStore.MultiDescribleElement, DescribableJSON.AppendPayloadAttribute {

        @Override
        public String identityValue() {
            return "payload-only";
        }

        @Override
        public Descriptor<PayloadOnlyElement> getDescriptor() {
            return new Desc();
        }

        @Override
        public Map<String, Object> createPayloadProps(List<Describable<?>> parent) {
            return Map.of();
        }

        public static class Desc
                extends MultiDescribleElementSetSelector.MultiDescribleElementDescriptor<PayloadOnlyElement> {
            public Desc() {
                super();
            }

            @Override
            public String getDisplayName() {
                return "缺列值来源";
            }

            @Override
            public String shortComment() {
                return "只实现 AppendPayloadAttribute";
            }

            @Override
            public List<MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig> colsConfig() {
                return List.of(MultiDescribleElementSetSelector.MultiDescribleElementDescriptor.ColConfig
                        .literiaDescCol());
            }
        }
    }
}

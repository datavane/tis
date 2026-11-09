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
package com.qlangtech.tis.util;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link DescribableJSON#getParent()} 的链路契约：祖先链必须是<b>扁平</b>的一层
 * {@code List<Describable>}，从最外层到直接父级，且<b>不含</b>当前实例本身。
 *
 * <h3>为什么必须有这个测试</h3>
 * 这条链路出错不会有任何编译期信号：
 * <ul>
 *   <li>产生 {@link DescribableJSON} 的地方常用<b>原始类型</b>（如
 *       {@code RootFormProperties} 里 {@code d} 是 raw {@code Describable}），泛型擦除后
 *       构造函数形参退化成 {@code List}，写成 {@code Lists.newArrayList(parent, instance)}
 *       （可变参数 → 两个元素 → 套娃）javac 一声不响；</li>
 *   <li>消费方按 {@code Describable} 取元素时在 index 0 收到 {@code ArrayList} 才 CCE，
 *       而取不到祖先本身往往只是「载荷少了一点」，不报错。</li>
 * </ul>
 * 所以这里既断言<b>形状</b>（元素全是 Describable），也断言<b>内容与顺序</b>。
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2026/11/5
 * @see DescribableJSON#appendParent(List, Describable)
 */
public class TestDescribableJSONParentChain extends TestCase {

    /**
     * 单纯验证 {@link DescribableJSON#appendParent} 的语义：追加、不嵌层、不改原链。
     */
    public void testAppendParentProducesFlatChain() {
        Root root = newRoot();
        Child child = newChild();

        List<Describable<?>> empty = Collections.emptyList();
        List<Describable<?>> level1 = DescribableJSON.appendParent(empty, root);
        List<Describable<?>> level2 = DescribableJSON.appendParent(level1, child);

        assertEquals("第一层只有当前实例", 1, level1.size());
        assertSame("level1[0] 必须是实例本身，不能是包装后的列表", root, level1.get(0));

        assertEquals(2, level2.size());
        assertSame(root, level2.get(0));
        assertSame(child, level2.get(1));

        // 每个元素都必须是 Describable —— 套娃写法会让 index 0 变成 ArrayList
        for (Object elem : level2) {
            assertTrue("祖先链元素必须是 Describable，实际:" + elem.getClass().getName(),
                    elem instanceof Describable);
        }
        // 原链不可变且未被就地修改
        assertTrue("appendParent 不能改动传入的链", empty.isEmpty());
        assertEquals(1, level1.size());
    }

    /**
     * 端到端：三层嵌套 {@code Describable} 字段，最内层 {@link DescribableJSON.AppendPayloadAttribute}
     * 收到的 parent 必须是 {@code [root, child]}。
     */
    public void testNestedDescribableReceivesFullAncestorChain() throws Exception {
        Root root = newRoot();
        RootFormProperties props = new RootFormProperties(root.getDescriptor(),
                Map.of("child", propertyType(Root.class, "child")));

        props.getInstancePropsJson(root, Collections.emptyList());

        assertNotNull("leaf 的 appendPayload 没有被调用", Leaf.capturedParent);
        assertEquals("leaf 的祖先链应为 [root, child]", 2, Leaf.capturedParent.size());
        assertSame("祖先链最外层应是 root", root, Leaf.capturedParent.get(0));
        assertSame("祖先链末位应是直接父级 child", root.child, Leaf.capturedParent.get(1));
        for (Object elem : Leaf.capturedParent) {
            assertTrue("祖先链元素必须是 Describable，实际:" + elem.getClass().getName(),
                    elem instanceof Describable);
        }
    }

    /**
     * {@code MULTI_DESCRIBLE_PLUGIN} 的每个元素拿到的链与单值 {@code Describable} 一致，
     * 且同级多个元素共享同一条链（不能互相 append）。
     */
    public void testMultiDescribableItemsShareAncestorChain() throws Exception {
        Root root = newRoot();
        root.children = List.of(newChild("c1"), newChild("c2"));
        RootFormProperties props = new RootFormProperties(root.getDescriptor(),
                Map.of("children", propertyType(Root.class, "children")));

        Leaf.capturedAll.clear();
        props.getInstancePropsJson(root, Collections.emptyList());

        assertEquals("两个同级元素各调用一次 appendPayload", 2, Leaf.capturedAll.size());
        for (List<Describable<?>> chain : Leaf.capturedAll) {
            assertEquals(2, chain.size());
            assertSame(root, chain.get(0));
            assertFalse("同级元素的祖先链里不能出现另一个同级元素",
                    chain.get(1) instanceof Root);
        }
        assertNotSame("两个同级元素应是不同的实例", root.children.get(0), root.children.get(1));
    }

    private static Root newRoot() {
        Root root = new Root();
        root.name = "root";
        root.child = newChild();
        return root;
    }

    private static Child newChild() {
        return newChild("child");
    }

    private static Child newChild(String name) {
        Child child = new Child();
        child.name = name;
        child.leaf = new Leaf();
        return child;
    }

    private static com.qlangtech.tis.extension.impl.PropertyType propertyType(Class<?> owner, String field) {
        try {
            return new com.qlangtech.tis.extension.impl.PropertyType(owner, owner.getField(field),
                    owner.getField(field).getAnnotation(FormField.class));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------
    //  夹具：Root → Child → Leaf 三层，只有 Leaf 关心 parent
    // ------------------------------------------------------------------

    public static class Root implements Describable<Root> {
        @FormField(ordinal = 0, type = FormFieldType.INPUTTEXT)
        public String name;

        @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT)
        public Child child;

        @FormField(ordinal = 2, type = FormFieldType.MULTI_DESCRIBLE_PLUGIN, desClazz = Child.class)
        public List<Child> children;

        @Override
        public Descriptor<Root> getDescriptor() {
            return new RootDesc();
        }
    }

    public static class RootDesc extends Descriptor<Root> {
        public RootDesc() {
            super(Root.class);
        }
    }

    /**
     * 同时实现 {@link com.qlangtech.tis.plugin.IPluginStore.MultiDescribleElement}：
     * {@code MULTI_DESCRIBLE_PLUGIN} 字段的构造期会校验元素类型，
     * 见 {@code PropertyType#createFieldListElementClazz}。
     */
    public static class Child implements Describable<Child>, com.qlangtech.tis.plugin.IPluginStore.MultiDescribleElement {
        @FormField(ordinal = 0, identity = true, type = FormFieldType.INPUTTEXT)
        public String name;

        @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT)
        public Leaf leaf;

        @Override
        public String identityValue() {
            return name;
        }

        @Override
        public Descriptor<Child> getDescriptor() {
            return new ChildDesc();
        }

        /**
         * 必须是 {@link Child} 的<b>嵌套</b>类：{@code MultiDescribleElementDescriptor} 只有无参
         * 构造器，{@code clazz} 由「descriptor 类的 enclosing class」推断
         * （见 {@code Descriptor#Descriptor()}），放平级会抛
         * {@code Outer class ... is not assignable to ... Perhaps wrong outer class?}。
         */
        public static class ChildDesc
                extends com.qlangtech.tis.plugin.IPluginStore.MultiDescribleElementDescriptor<Child> {
            public ChildDesc() {
                super();
            }

            @Override
            public String shortComment() {
                return "测试子项";
            }
        }
    }

    /**
     * 最内层：把自己收到的 parent 记下来供断言。记录行为发生在
     * {@link DescribableJSON#getItemJson} 调 {@code createPayloadProps(parent)} 时。
     */
    public static class Leaf implements Describable<Leaf>, DescribableJSON.AppendPayloadAttribute {

        static List<Describable<?>> capturedParent;
        static final java.util.List<List<Describable<?>>> capturedAll = new java.util.ArrayList<>();

        @FormField(ordinal = 0, type = FormFieldType.INPUTTEXT)
        public String name;

        @Override
        public Descriptor<Leaf> getDescriptor() {
            return new LeafDesc();
        }

//        @Override
//        public Map<String, Object> createPayloadProps() {
//            return Map.of("empty", true);
//        }

        @Override
        public Map<String, Object> createPayloadProps(List<Describable<?>> parent) {
            capturedParent = parent;
            capturedAll.add(parent);
            return Map.of("size", parent.size());
        }
    }

    public static class LeafDesc extends Descriptor<Leaf> {
        public LeafDesc() {
            super(Leaf.class);
        }
    }
}

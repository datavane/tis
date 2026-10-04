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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.manage.common.Option;

import java.util.List;

/**
 * 插件的表单属性抽象。
 * <p>
 * 注意：本接口位于 tis-manage-pojo 模块，而 { com.qlangtech.tis.extension.impl.PropertyType} 位于 tis-plugin 模块
 * （tis-plugin 依赖 tis-manage-pojo），因此这里只能声明参数/返回值类型在本模块可见的方法，
 * 类似 Validator、FormField、EnumFieldMode、MultiItemsViewType 等 tis-plugin 侧的专有类型不能出现在本接口中。
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 12:11
 */
public interface IPropertyType {
    String KEY_UNIT = "unit";
    int CONST_UNIT_INTEGER_FIELD = 12;

    /**
     * 对应的property 是否是集合属性
     *
     * @return
     */
    boolean isCollectionType();

    /**
     * 是否是主键
     *
     * @return
     */
    boolean isIdentity();

    /**
     * 字段成员名称说明
     *
     * @return
     */
    String propertyName();

    /**
     * 属性最终要显示在前端页面上的值
     */
    Object getFrontendOutput(Object instance);

    /**
     * 属性的类型是否是 Describable 类型的插件
     */
    boolean isDescribable();

    /**
     * 属性的额外属性配置
     */
    JSONObject getExtraProps();

    //    /**
    //     * 前端表单渲染时的字段排序号
    //     */
    default int ordinal() {
        return Integer.MIN_VALUE;
    }

    /**
     * 是否是必填字段
     */
    boolean isInputRequired();

    /**
     * 字段类型的标识，参见 FormFieldType#getIdentity()
     */
    int typeIdentity();

    /**
     * 属性定义的默认值
     */
    Object dftVal();

    /**
     * 是否属于前端表单的「高级」分组
     */
    boolean advance();

    /**
     * 将前端提交的值设置到实例上
     */
    void setVal(Object instance, Object val);

    /**
     * 追加属性类型相关的外部属性
     */
    void appendExternalProp(JSONObject attrVal);

    /**
     * 枚举类型的候选选项
     */
    List<Option> getEnumPropOptions();

    /**
     * 属性所属的字段类型（用于取字段上的注解等）
     */
    Class getFieldClazz();
}

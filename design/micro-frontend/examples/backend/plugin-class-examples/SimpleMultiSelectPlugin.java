package com.qlangtech.tis.plugin.example;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单多选插件示例
 *
 * 演示最基础的MULTI_SELECTABLE字段使用Web Component
 */
public class SimpleMultiSelectPlugin extends Descriptor<SimpleMultiSelectPlugin> {

    @FormField(
        ordinal = 1,
        type = FormFieldType.MULTI_SELECTABLE,
        validate = {Validator.require}
    )
    public List<String> selectedItems;

    /**
     * 提供可选项列表
     */
    public static List<String> getItems() {
        List<String> items = new ArrayList<>();
        items.add("Option A");
        items.add("Option B");
        items.add("Option C");
        return items;
    }

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<SimpleMultiSelectPlugin> {

        @Override
        public String getDisplayName() {
            return "简单多选组件示例";
        }
    }
}

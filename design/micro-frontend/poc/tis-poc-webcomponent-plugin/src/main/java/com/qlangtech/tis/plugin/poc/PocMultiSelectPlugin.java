package com.qlangtech.tis.plugin.poc;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * POC插件：验证Web Component微前端架构
 *
 * 这是一个简化的测试插件，用于验证：
 * 1. 前端Web Component的构建和打包
 * 2. 插件资源的HTTP访问
 * 3. 动态加载和渲染机制
 * 4. 数据双向绑定
 *
 * @author TIS Team
 * @date 2026-06-11
 */
public class PocMultiSelectPlugin extends Descriptor<PocMultiSelectPlugin> {

    /**
     * JDBC类型多选字段
     *
     * 使用Web Component渲染（配置在JSON文件中）
     */
    @FormField(
        ordinal = 1,
        type = FormFieldType.MULTI_SELECTABLE,
        validate = {Validator.require}
    )
    public List<JdbcTypeItem> jdbcTypes;

    /**
     * 获取可用的JDBC类型列表（初始数据）
     */
    public static List<JdbcTypeItem> getAvailableTypes() {
        List<JdbcTypeItem> types = new ArrayList<>();

        types.add(new JdbcTypeItem("VARCHAR", "VARCHAR", true, "可变长度字符串"));
        types.add(new JdbcTypeItem("INTEGER", "INTEGER", true, "整数类型"));
        types.add(new JdbcTypeItem("BIGINT", "BIGINT", false, "长整数类型"));
        types.add(new JdbcTypeItem("DECIMAL", "DECIMAL", false, "精确数值类型"));
        types.add(new JdbcTypeItem("TIMESTAMP", "TIMESTAMP", true, "时间戳类型"));
        types.add(new JdbcTypeItem("DATE", "DATE", false, "日期类型"));
        types.add(new JdbcTypeItem("BOOLEAN", "BOOLEAN", false, "布尔类型"));

        return types;
    }

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<PocMultiSelectPlugin> {

        @Override
        public String getDisplayName() {
            return "POC: Web Component多选组件";
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            // 简化的验证逻辑
            return true;
        }
    }
}
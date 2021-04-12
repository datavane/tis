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
package com.qlangtech.tis.util;

import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.IPropertyType;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析提交的plugin元数据信息，如果plugin为"xxxplugin:require" 则是在告诉服务端，该plugin必须要有输入内容，该plugin不可缺省
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-20 11:00
 */
public class UploadPluginMeta {

    private static final String ATTR_KEY_VALUE_SPLIT = "_";

    private static final Pattern PATTERN_PLUGIN_ATTRIBUTE = Pattern.compile("[" + ATTR_KEY_VALUE_SPLIT + "\\w]+");

    public static final Pattern PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR
            = Pattern.compile("([^" + ATTR_KEY_VALUE_SPLIT + "]+?)" + ATTR_KEY_VALUE_SPLIT + "(" + PATTERN_PLUGIN_ATTRIBUTE.pattern() + ")");

    private static final Pattern PATTERN_PLUGIN_META = Pattern.compile("(.+?)(:(,?(" + PATTERN_PLUGIN_ATTRIBUTE + "))+)?");

    public static final String KEY_REQUIRE = "require";
    private final String name;

    // plugin form must contain field where prop required is true
    private boolean required;
    // 除去 required 之外的其他参数
    private Map<String, String> extraParams = new HashMap<>();

    public static void main(String[] args) throws Exception {

        Matcher matcher = PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR.matcher("dsname_dsname_yuqing_zj2_bak");

        System.out.println(matcher.matches());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));

//        Matcher matcher = PATTERN_PLUGIN_ATTRIBUTE.matcher("_3sfgG");
////        if(matcher.matches()){
////
////        }
//        System.out.println(matcher.matches());
    }

    public static List<UploadPluginMeta> parse(String[] plugins) {
        if (plugins == null || plugins.length < 1) {
            throw new IllegalArgumentException("plugin size:" + plugins.length + " length can not small than 1");
        }
        List<UploadPluginMeta> metas = Lists.newArrayList();
        for (String plugin : plugins) {
            metas.add(parse(plugin));
        }
        if (plugins.length != metas.size()) {
            throw new IllegalStateException("param plugins length:" + plugins.length + " must equal with metaSize:" + metas.size());
        }
        return metas;
    }

    /**
     * @param plugin
     * @return
     */
    public static UploadPluginMeta parse(String plugin) {
        Matcher matcher, attrKVMatcher;
        UploadPluginMeta pmeta;
        Matcher attrMatcher;
        String attr;
        matcher = PATTERN_PLUGIN_META.matcher(plugin);
        if (matcher.matches()) {
            pmeta = new UploadPluginMeta(matcher.group(1));
            if (matcher.group(2) != null) {
                attrMatcher = PATTERN_PLUGIN_ATTRIBUTE.matcher(matcher.group(2));
                while (attrMatcher.find()) {
                    attr = attrMatcher.group();
                    switch (attr) {
                        case KEY_REQUIRE:
                            pmeta.required = true;
                            break;
                        default:
                            attrKVMatcher = PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR.matcher(attr);
                            if (!attrKVMatcher.matches()) {
                                throw new IllegalStateException("attr:" + attr + " is not match:" + PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR.pattern());
                            }
//                            String[] pair = StringUtils.split(attr, ATTR_KEY_VALUE_SPLIT);
//                            if (pair.length != 2) {
//                                throw new IllegalStateException("attr:" + attr + " is illegal");
//                            }
                            pmeta.extraParams.put(attrKVMatcher.group(1), attrKVMatcher.group(2));
                    }
                }
            }
            return pmeta;
            //metas.add(pmeta);
        } else {
            throw new IllegalStateException("plugin:'" + plugin + "' is not match the pattern:" + PATTERN_PLUGIN_META);
        }
    }

    public HeteroEnum getHeteroEnum() {
        return HeteroEnum.of(this.getName());
    }


    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }


    public Optional<IPropertyType.SubFormFilter> getSubFormFilter() {

        String targetDesc = this.getExtraParam(IPropertyType.SubFormFilter.PLUGIN_META_TARGET_DESCRIPTOR_NAME);
        String subFormField = this.getExtraParam(IPropertyType.SubFormFilter.PLUGIN_META_SUB_FORM_FIELD);
        if (StringUtils.isNotEmpty(targetDesc)) {
            return Optional.of(new IPropertyType.SubFormFilter(targetDesc, subFormField));
        }
        return Optional.empty();
    }

    public String getExtraParam(String key) {
        return this.extraParams.get(key);
    }

    private UploadPluginMeta(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UploadPluginMeta{" + "name='" + name + '\'' + ", required=" + required + '}';
    }

    public HeteroList<?> getHeteroList(IPluginContext pluginContext) {
        HeteroEnum hEnum = getHeteroEnum();
        HeteroList<?> hList = new HeteroList<>(this);
        hList.setCaption(hEnum.caption);
        hList.setExtensionPoint(hEnum.extensionPoint);
        hList.setItems(hEnum.getPlugins(pluginContext, this));
        hList.setDescriptors(hEnum.descriptors());
        hList.setSelectable(hEnum.selectable);
        return hList;
    }
}

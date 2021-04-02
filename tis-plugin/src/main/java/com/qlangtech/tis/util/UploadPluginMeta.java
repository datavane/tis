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
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final Pattern PATTERN_PLUGIN_META = Pattern.compile("(.+?)(:(,?(" + PATTERN_PLUGIN_ATTRIBUTE + "))+)?");

    public static final String KEY_REQUIRE = "require";
    private final String name;

    private boolean required;
    // 除去 required 之外的其他参数
    private Map<String, String> extraParams = new HashMap<>();


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
     *
     * @param plugin
     * @return
     */
    public static UploadPluginMeta parse(String plugin) {
        Matcher matcher;
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
                            String[] pair = StringUtils.split(attr, ATTR_KEY_VALUE_SPLIT);
                            if (pair.length != 2) {
                                throw new IllegalStateException("attr:" + attr + " is illegal");
                            }
                            pmeta.extraParams.put(pair[0], pair[1]);
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


    public static void main(String[] args) {
        String[] plugins = new String[]{"test_plugin:require"};
        List<UploadPluginMeta> pluginMetas = parse(plugins);
        for (UploadPluginMeta m : pluginMetas) {
            System.out.println(m);
        }
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
}

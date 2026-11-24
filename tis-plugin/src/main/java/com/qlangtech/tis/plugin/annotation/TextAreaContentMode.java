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
package com.qlangtech.tis.plugin.annotation;

import com.qlangtech.tis.extension.DescriptorUseableShortComment;

/**
 * 多行文本域（TEXTAREA）的内容形态。
 *
 * <p>原先嵌在 ontology 插件的 {@code TextAreaWidget} 里，因 {@code codeMirrorToken}
 * 是与具体插件无关的展示形态词汇（前端 CodeMirror 按 token 高亮），收敛到 core
 * 注解包供更多 TEXTAREA 场景复用。
 *
 * <p>常量名即表单选项值 / 落盘值；中文说明经 {@link #shortComment()} 作为选项 help
 * 下发（{@code OptionWithEndType.createEnumOptions}）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/23
 */
public enum TextAreaContentMode implements DescriptorUseableShortComment {

    VelocityTemplate("text/velocity", "Velocity 模板，支持变量占位与循环") //
    , Json("application/ld+json", "JSON 格式，适用于结构化数据")//
    , Xml("application/xml", "XML 格式，适用于标记语言数据")//
    , Yaml("text/yaml", "YAML 格式，适用于配置类数据")//
    , markdown("text/markdown", "Markdown 格式，适用于富文本描述");

    public final String codeMirrorToken;
    private final String shortComment;

    private TextAreaContentMode(String codeMirrorToken, String shortComment) {
        this.codeMirrorToken = codeMirrorToken;
        this.shortComment = shortComment;
    }

    @Override
    public String shortComment() {
        return this.shortComment;
    }
}

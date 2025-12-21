---
description: 为TIS插件生成简介文档
---

# TIS插件简介文档生成器

此命令为指定的TIS插件类生成一份简洁的使用说明文档。

## 使用方法

```
/gen-plugin-doc <插件类的绝对路径>
```

示例：
```
/gen-plugin-doc /Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/proxy/HttpRequestProxy.java
```

## 任务说明

为指定的TIS插件类生成markdown格式的简介文档。该文档帮助TIS用户快速了解插件的核心作用和工作机制。

## 输入参数

- **插件类绝对路径**：插件Java源文件的完整路径（从项目根目录到.java文件）
  - 示例：`/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/proxy/HttpRequestProxy.java`

## 文档素材

使用以下素材信息生成有针对性的说明文档：

1. **插件类文件**：
   - 读取指定的Java文件
   - 识别带有 `@FormField` 标注的字段，了解插件向用户暴露的属性
   - 分析类中的业务执行逻辑

2. **插件元数据文件（JSON）**：
   - 路径规则：将 `src/main/java/com/foo/bar/PluginClass.java` 转换为 `src/main/resources/com/foo/bar/PluginClass.json`
   - 包含插件字段的markdown格式元数据说明（默认值、placeholder、label、help使用说明）

3. **插件描述文件（Markdown，可选）**：
   - 路径规则：将 `src/main/java/com/foo/bar/PluginClass.java` 转换为 `src/main/resources/com/foo/bar/PluginClass.md`
   - 包含插件字段的markdown格式说明（每个字段以 `## fieldName` 起头）
   - 此文件可能不存在

4. **插件描述类**：
   - 查找插件类中带有 `@TISExtension` 标注的内部类（如：`DefaultDescriptor`）
   - 以 `validate` 或 `verify` 为前缀的方法包含插件的校验约束规则

## 内容要求

- **目标读者**：可能包括非技术人员
- **语言**：清晰简洁的中文
- **字数**：约300字左右
- **避免**：代码片段、过多技术细节
- **格式**：结构清晰的markdown格式
- **编码**：确保中文编码正确，不要出现乱码

## 文档结构建议

1. **插件简介**
   - 核心作用和应用场景
   - 为什么需要这个插件及实际应用价值及主要功能（1-2个要点）

2. **使用建议**
   - 配置提示
   - 最佳实践

## 输出文件路径

在resources目录下生成文档，遵循以下规则：
- 如果插件类是：`src/main/java/com/foo/bar/PluginClass.java`
- 则输出文件为：`src/main/resources/com/foo/bar/PluginClass_introduce.md`

## 重要提示

- **不要包含**字段配置的详细说明（会单独提供详细文档）
- 聚焦于插件的用途和工作原理
- 使用简单语言，适合非技术人员阅读
- 内容要有针对性，避免泛泛而谈

## 执行步骤

1. 从提供的绝对路径中提取包路径和类名
2. 读取插件类文件
3. 计算并读取对应的 .json 元数据文件
4. 尝试读取对应的 .md 描述文件（如果存在）
5. 从代码中分析插件的核心功能
6. 生成结构清晰的简介文档（约600字）
7. 保存到对应的resources目录，文件名添加 `_introduce.md` 后缀
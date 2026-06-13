# Build配置示例

本目录包含各种构建配置示例。

## Maven Frontend Plugin

`maven-frontend-plugin/` 目录包含：
- `basic-pom.xml` - 基础Maven配置
- 集成npm构建到Maven生命周期

使用方法：
```xml
<!-- 复制<plugin>...</plugin>到你的pom.xml中 -->
```

## Webpack配置

`webpack-configs/` 目录包含：
- `basic.config.js` - 基础配置
- `with-externals.config.js` - 外部化依赖配置

使用方法：
```bash
# 复制配置文件
cp webpack-configs/basic.config.js your-plugin/webapp/webpack.config.js

# 根据需要调整路径和配置
```

## 注意事项

1. 调整输出路径到你的资源目录
2. 根据项目需要选择是否外部化依赖
3. 生产环境启用压缩和优化

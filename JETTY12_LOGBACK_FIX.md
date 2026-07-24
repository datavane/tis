# Jetty 12 日志系统修复说明

## 问题描述

升级到 Jetty 12 后，通过 `web.xml` 中的 `<env-entry>` 配置的 JNDI 环境变量（用于 logback 上下文分离）不再生效，导致日志系统无法正常工作。

## 根本原因

### 1. Jetty 12 的 JNDI 支持变化
- **Jetty 9.4**: JNDI 支持是默认启用的
- **Jetty 12**: JNDI 支持需要显式添加依赖和配置

### 2. 缺少必要的配置
- 缺少 `jetty-ee11-plus` 依赖包（提供 JNDI 支持）
- 缺少 `EnvConfiguration` 和 `PlusConfiguration`（处理 `web.xml` 中的 `<env-entry>`）
- WebAppContext 的配置数组没有正确设置

### 3. Logback 配置问题
- logback 配置文件中缺少 `<contextName>` 标签
- `TisApp` 类中已经设置了 JNDI ContextSelector，但配置文件不完整

## 解决方案

### 1. 添加 Jetty JNDI 依赖

在 `tis-web-start/pom.xml` 中添加：

```xml
<!-- JNDI support for env-entry in web.xml -->
<dependency>
    <groupId>org.eclipse.jetty.ee11</groupId>
    <artifactId>jetty-ee11-plus</artifactId>
    <version>${jetty.version}</version>
    <scope>compile</scope>
</dependency>
```

### 2. 修改 JettyTISRunner.java

#### 2.1 添加必要的导入
```java
import org.eclipse.jetty.ee11.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.ee11.plus.webapp.PlusConfiguration;
```

#### 2.2 显式配置 WebAppContext
在 `JettyTISRunner.java` 的 `addContext` 方法中，设置完整的配置数组：

```java
// Set configurations explicitly - EnvConfiguration is required for JNDI env-entry support
webAppContext.setConfigurations(new Configuration[]{
    new EnvConfiguration(),              // MUST be first - handles JNDI env-entry from web.xml
    new PlusConfiguration(),             // Provides JNDI support infrastructure
    new WebInfConfiguration(),
    new WebXmlConfiguration(),
    new MetaInfConfiguration(),
    new FragmentConfiguration(),
    new AnnotationConfiguration(),       // Handle servlet 3.0+ annotations and listeners
    new JettyWebXmlConfiguration()
});
```

**关键点**:
- `EnvConfiguration` 必须放在第一位，它负责处理 `web.xml` 中的 `<env-entry>`
- `PlusConfiguration` 提供 JNDI 基础设施支持

### 3. 更新 Logback 配置文件

#### 3.1 logback-console.xml
添加上下文名称：
```xml
<configuration debug="false">
    <contextName>console</contextName>
    ...
</configuration>
```

#### 3.2 logback-assemble.xml
添加上下文名称：
```xml
<configuration debug="false">
    <contextName>assemble</contextName>
    ...
</configuration>
```

## 工作原理

### Jetty 9.4 的工作方式
1. JNDI 支持是默认启用的
2. `web.xml` 中的 `<env-entry>` 自动被处理
3. Logback 通过 JNDI 查找 `java:comp/env/logback/context-name` 获取上下文名称

### Jetty 12 的工作方式
1. 需要显式添加 `jetty-ee11-plus` 依赖
2. 需要在 WebAppContext 的 Configuration 数组中包含 `EnvConfiguration` 和 `PlusConfiguration`
3. `EnvConfiguration` 会解析 `web.xml` 中的 `<env-entry>` 并注册到 JNDI
4. Logback 通过 JNDI ContextSelector 根据 `logback/context-name` 选择不同的日志上下文

### 完整的调用链
```
TisApp.main() 
  └─> static { setLogbackContextSelector() }  // 设置 JNDI selector
  └─> launchTISApp()
      └─> new TisApp()
          └─> new JettyTISRunner()
              └─> addContext()  // 为 tis-assemble 和 tjs (console) 创建 context
                  └─> new WebAppContext()
                      └─> setConfigurations([EnvConfiguration, ...])  // 处理 env-entry
                      
启动后:
  tis-assemble: JNDI查找 -> "assemble" -> 使用 logback-assemble.xml
  tjs:          JNDI查找 -> "console"  -> 使用 logback-console.xml
```

## 验证方法

1. **编译项目**:
```bash
mvn clean compile -pl tis-web-start -am -Dmaven.test.skip=true
```

2. **运行应用并检查日志**:
```bash
mvn clean package -Dmaven.test.skip=true
# 启动应用
# 检查是否生成了独立的日志文件：
# - ${log.dir}/console.log (console context)
# - ${log.dir}/assemble.log (assemble context)
```

3. **验证 JNDI 是否工作**:
   - 在应用启动时，两个 servlet context 应该各自使用独立的 logback 上下文
   - console context 的日志应该输出到 console.log
   - assemble context 的日志应该输出到 assemble.log

## 参考资料

- [Jetty 12 Migration Guide](https://eclipse.dev/jetty/documentation/jetty-12/programming-guide/index.html#pg-migration-11-to-12)
- [Jetty 12 JNDI Support](https://eclipse.dev/jetty/documentation/jetty-12/operations-guide/index.html#og-jndi)
- [Logback JNDI Context Selector](https://logback.qos.ch/manual/contextSelector.html)

## 修改文件清单

1. `tis-web-start/pom.xml` - 添加 jetty-ee11-plus 依赖
2. `tis-web-start/src/main/java/com/qlangtech/tis/web/start/JettyTISRunner.java` - 添加 JNDI 配置支持
3. `tis-console/src/main/resources/logback-console.xml` - 添加 contextName
4. `tis-assemble/src/main/resources/logback-assemble.xml` - 添加 contextName

## 注意事项

1. **配置顺序很重要**: `EnvConfiguration` 必须在 Configuration 数组的第一位
2. **依赖作用域**: `jetty-ee11-plus` 的 scope 必须是 `compile`，不能是 `provided`
3. **上下文名称匹配**: logback 配置文件中的 `<contextName>` 必须与 `web.xml` 中的 `<env-entry-value>` 一致

## 已知问题

无

## 更新日期

2026-07-24
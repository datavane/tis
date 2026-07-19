# Jetty 12 WebSocket 问题 - 最终修复方案（第三版）

## 问题症状
- Jetty 9.4.31 → 12.1.7 升级后
- LogFeedbackServlet 的 `@OnWebSocketOpen` 方法不触发
- WebSocket 连接无法建立

## 深度分析后的核心发现

### 🎯 根本原因

**自定义类加载器 (TISAppClassLoader) + setConfigurationDiscovered(true) 的组合导致 WebSocket Configuration 加载失败**

#### 问题链条：

1. **自定义类加载器**：
   ```java
   TISAppClassLoader contextCloassLoader = new TISAppClassLoader(context, this.parentLoader, jarfiles);
   webAppContext.setClassLoader(contextCloassLoader);
   ```

2. **自动配置发现**：
   ```java
   webAppContext.setConfigurationDiscovered(true);  // 尝试通过 ServiceLoader 自动发现
   ```

3. **ServiceLoader 失败**：
   - ServiceLoader 在自定义类加载器中无法找到 `JettyWebSocketConfiguration`
   - META-INF/services 文件读取失败或路径不正确
   - 即使手动 `addConfiguration()`，自动发现也可能覆盖或干扰

4. **结果**：
   - WebSocket 组件没有被初始化
   - `JettyWebSocketServlet` 的 `configure()` 方法可能被调用，但 WebSocket 升级机制没有建立
   - `@WebSocket` 注解的处理器没有注册

### 🔍 为什么之前的方案不工作

#### 尝试 1: 使用 JettyWebSocketServletContainerInitializer
```java
JettyWebSocketServletContainerInitializer.configure(webAppContext, null);  // ❌ 错误
```
- **错误原因**：这是给 Jakarta WebSocket API (@ServerEndpoint) 用的
- LogFeedbackServlet 使用的是 Jetty 原生 API (@WebSocket)

#### 尝试 2: 只添加 JettyWebSocketConfiguration
```java
webAppContext.addConfiguration(new JettyWebSocketConfiguration());  // ❌ 不够
webAppContext.setConfigurationDiscovered(true);  // 这行会导致问题
```
- **问题**：自动发现机制可能覆盖或与手动添加的配置冲突
- 在自定义类加载器环境下，ServiceLoader 行为不可预测

## ✅ 最终正确方案

### 核心策略：**完全禁用自动发现，手动控制所有 Configuration**

### 代码修改

**文件**: `tis-web-start/src/main/java/com/qlangtech/tis/web/start/JettyTISRunner.java`

#### 1. 添加必要的导入（第 23-33 行）

```java
import org.eclipse.jetty.ee11.webapp.Configuration;
import org.eclipse.jetty.ee11.webapp.FragmentConfiguration;
import org.eclipse.jetty.ee11.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.ee11.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.ee11.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee11.webapp.WebXmlConfiguration;
import org.eclipse.jetty.ee11.websocket.server.config.JettyWebSocketConfiguration;
```

#### 2. 修改 addContext() 方法（第 189-207 行）

```java
webAppContext.setDescriptor(new File(webappDir, TisApp.PATH_WEB_XML).getAbsolutePath());
webAppContext.setDisplayName(context);

// CRITICAL FIX for Jetty 12 WebSocket with custom ClassLoader
// Disable auto-discovery and manually set all configurations including WebSocket
webAppContext.setConfigurationDiscovered(false);  // 关键：禁用自动发现
webAppContext.setConfigurations(new Configuration[]{
    new WebInfConfiguration(),           // 处理 WEB-INF
    new WebXmlConfiguration(),           // 解析 web.xml
    new MetaInfConfiguration(),          // 处理 META-INF
    new FragmentConfiguration(),         // 处理 web-fragment.xml
    new JettyWebSocketConfiguration(),   // ⭐ WebSocket 支持
    new JettyWebXmlConfiguration()       // 处理 jetty-web.xml
});

webAppContext.setParentLoaderPriority(true);
webAppContext.setThrowUnavailableOnStartupException(true);
webAppContext.addServlet(CheckHealth.class, "/check_health");

this.addContext(webAppContext);
```

### 关键点解释

1. **`setConfigurationDiscovered(false)`**：
   - 完全禁用 ServiceLoader 的自动发现机制
   - 避免与自定义类加载器的冲突
   - 确保配置加载的可预测性

2. **`setConfigurations()`**：
   - 显式指定所有需要的 Configuration
   - 按正确的顺序排列（很重要！）
   - 确保 `JettyWebSocketConfiguration` 被加载

3. **Configuration 顺序的重要性**：
   - `WebInfConfiguration` 必须在前（设置资源路径）
   - `WebXmlConfiguration` 解析 servlet 定义
   - `JettyWebSocketConfiguration` 在中间（依赖前面的配置）
   - `JettyWebXmlConfiguration` 在最后（可选的 Jetty 特定配置）

## 工作原理

### JettyWebSocketConfiguration 的作用

当 `JettyWebSocketConfiguration` 被正确加载时，它会：

1. **注册 ServletContainerInitializer**：
   - 添加 WebSocket 升级过滤器
   - 初始化 WebSocket 组件容器

2. **扫描 @WebSocket 注解**：
   - 识别继承自 `JettyWebSocketServlet` 的 servlet
   - 为每个 servlet 设置 WebSocket 工厂

3. **配置 WebSocket 生命周期**：
   - 管理 WebSocket 会话
   - 处理连接升级请求
   - 调用 `@OnWebSocketOpen`、`@OnWebSocketMessage` 等回调

### 为什么这个方案有效

1. **绕过 ServiceLoader**：
   - 不依赖类加载器的 ServiceLoader 机制
   - 直接实例化所需的 Configuration

2. **确定性配置**：
   - 配置顺序明确
   - 没有自动发现的不确定性

3. **兼容自定义类加载器**：
   - Configuration 类在父类加载器中（tis-web-start 的依赖）
   - servlet 类在子类加载器中（TISAppClassLoader）
   - 两者可以正确交互

## 验证步骤

### 1. 重新编译
```bash
mvn clean compile -pl tis-web-start -am -Dmaven.test.skip=true
```

### 2. 启动应用并检查日志

查找以下日志确认 WebSocket 初始化：
```
- "JettyWebSocketConfiguration" 
- "WebSocket components initialized"
- "LogFeedbackServlet" 初始化日志
```

### 3. 测试 WebSocket 连接

```javascript
// 浏览器控制台
const ws = new WebSocket('ws://localhost:4200/tjs/download/logfeedback?taskid=3072&logtype=build_status_metrics');
ws.onopen = () => console.log('Connected!');
ws.onmessage = (e) => console.log('Message:', e.data);
ws.onerror = (e) => console.error('Error:', e);
```

### 4. 调试验证

在 `LogFeedbackServlet.java` 的以下位置设置断点：
- `configure()` 方法（第 112 行）
- `LogSocket.onOpen()` 方法（第 140 行）

两个断点都应该能触发。

## 其他注意事项

### 1. 如果还是不工作

添加更详细的调试日志：

```java
@Override
public void configure(JettyWebSocketServletFactory factory) {
    logger.info("========================================");
    logger.info("LogFeedbackServlet.configure() called");
    logger.info("Factory: " + factory.getClass().getName());
    logger.info("========================================");
    
    factory.setIdleTimeout(java.time.Duration.ofMillis(240000));
    factory.setCreator((req, rep) -> {
        logger.info("WebSocket creator invoked for: " + req.getRequestURI());
        return new LogSocket();
    });
    // ...
}
```

### 2. 其他 WebAppContext 的处理

如果有多个 WebAppContext，每个都需要同样的处理：

```java
// 对每个 context
webAppContext.setConfigurationDiscovered(false);
webAppContext.setConfigurations(new Configuration[]{ /* 同样的配置 */ });
```

### 3. web.xml 升级（可选但推荐）

长期来看，建议升级 web.xml 到 Jakarta EE：

```xml
<web-app version="6.0" 
         xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd">
```

但这不是必需的，当前方案应该能工作。

### 4. 性能考虑

手动配置 Configuration 不会影响性能，反而可能略微提升启动速度（避免了 ServiceLoader 扫描）。

## 技术细节总结

| 方面 | Jetty 9 | Jetty 12 (自动发现) | Jetty 12 (本方案) |
|------|---------|---------------------|-------------------|
| Configuration 加载 | 自动 | ServiceLoader | 手动指定 |
| 自定义类加载器 | 兼容 | 可能有问题 | 完全兼容 |
| WebSocket 初始化 | 自动 | 依赖 ServiceLoader | 显式配置 |
| 可预测性 | 高 | 中 | 高 |

## 修复历史

- **2026-07-03 15:09** - 尝试 1: JettyWebSocketServletContainerInitializer (错误)
- **2026-07-03 15:36** - 尝试 2: addConfiguration + setConfigurationDiscovered(true) (不完整)
- **2026-07-03 15:43** - ✅ 最终方案: setConfigurationDiscovered(false) + 手动 setConfigurations()

## 关键教训

1. **自定义类加载器 + 自动发现 = 潜在问题**
2. **Jetty 12 的 Configuration 机制需要显式控制**
3. **不同的 WebSocket API (Jetty vs Jakarta) 有不同的配置方式**
4. **配置顺序很重要**
5. **深入理解框架的类加载和初始化机制至关重要**

---

**请重新测试并告知结果！** 如果这个方案还不工作，我需要看到完整的启动日志来进一步诊断。

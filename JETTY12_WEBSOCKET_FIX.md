# Jetty 12 WebSocket 配置问题修复（第二版 - 正确方案）

## 问题描述

升级 Jetty 从 9.4.31.v20200723 到 12.1.7 后，WebSocket 连接无法正常工作：
- WebSocket URL: `ws://localhost:4200/tjs/download/logfeedback?taskid=3072&logtype=build_status_metrics`
- `LogFeedbackServlet` 中的 `@OnWebSocketOpen` 方法不会被触发
- 单步调试 `onOpen()` 方法无法进入

## 根本原因

**Jetty 12 的重大变更 - WebSocket Configuration 机制改变**：

### 关键发现

在 Jetty 12 中，有**两套不同的 WebSocket API**：

1. **Jakarta WebSocket API (JSR 356)**：
   - 使用 `@ServerEndpoint` 注解
   - 配置方法：`JettyWebSocketServletContainerInitializer.configure()`
   - 包路径：`jakarta.websocket.*`

2. **Jetty 原生 WebSocket API** ⭐：
   - 使用 `@WebSocket` 注解（**我们的情况**）
   - 配置方法：**添加 `JettyWebSocketConfiguration` 到 WebAppContext**
   - 包路径：`org.eclipse.jetty.websocket.api.*`

### 真正的问题

`LogFeedbackServlet` 继承自 `JettyWebSocketServlet` 并使用 `@WebSocket` 注解，这属于 **Jetty 原生 WebSocket API**。

在 Jetty 9 中，这些配置是自动的；但在 Jetty 12 中：
- **必须**显式添加 `JettyWebSocketConfiguration` 到 WebAppContext
- 这个 Configuration 类会扫描和处理 `@WebSocket` 注解
- 它会为 `JettyWebSocketServlet` 设置正确的 WebSocket 组件

### ❌ 错误的尝试

之前尝试使用 `JettyWebSocketServletContainerInitializer.configure()` 是**错误的**，因为：
- 那个方法是给 Jakarta WebSocket (JSR 356) 用的
- 它不会处理 Jetty 原生的 `@WebSocket` 注解
- 两套 API 的配置机制完全不同

## ✅ 正确解决方案

### 修改文件：`tis-web-start/src/main/java/com/qlangtech/tis/web/start/JettyTISRunner.java`

#### 1. 添加正确的导入（第 26 行）

```java
import org.eclipse.jetty.ee11.websocket.server.config.JettyWebSocketConfiguration;
```

**⚠️ 注意**：不是 `JettyWebSocketServletContainerInitializer`！

#### 2. 在 `addContext()` 方法中添加 WebSocket Configuration（第 196-197 行）

```java
webAppContext.addServlet(CheckHealth.class, "/check_health");

// Enable Jetty WebSocket support for Jetty 12
// Must add JettyWebSocketConfiguration to enable @WebSocket annotations
webAppContext.addConfiguration(new JettyWebSocketConfiguration());

this.addContext(webAppContext);
```

## 技术细节

### JettyWebSocketConfiguration 的作用

这个 Configuration 类会：
1. 在 WebAppContext 启动时初始化 Jetty WebSocket 组件
2. 扫描并处理 `@WebSocket` 注解的类
3. 为 `JettyWebSocketServlet` 设置正确的工厂和升级机制
4. 配置 WebSocket 的会话管理和生命周期

### 两套 WebSocket API 的区别

| 特性 | Jetty 原生 API | Jakarta WebSocket API |
|------|----------------|----------------------|
| 注解 | `@WebSocket` | `@ServerEndpoint` |
| Servlet 基类 | `JettyWebSocketServlet` | 无（使用注解） |
| 配置方式 | `addConfiguration(new JettyWebSocketConfiguration())` | `JettyWebSocketServletContainerInitializer.configure()` |
| 包路径 | `org.eclipse.jetty.websocket.api.*` | `jakarta.websocket.*` |
| 使用场景 | Jetty 特定应用，需要更底层控制 | 标准化应用，跨容器兼容 |

### 为什么 LogFeedbackServlet 使用 Jetty 原生 API？

查看代码导入：
```java
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServlet;
```

所有 WebSocket 相关的类都来自 `org.eclipse.jetty.websocket.api.*`，这是 Jetty 原生 API。

## 验证步骤

1. **重新编译项目**：
   ```bash
   mvn clean compile -pl tis-web-start -am -Dmaven.test.skip=true
   ```

2. **启动应用**：
   ```bash
   # 使用你的正常启动方式
   ```

3. **测试 WebSocket 连接**：
   - 在浏览器开发者工具或 WebSocket 客户端访问：
     `ws://localhost:4200/tjs/download/logfeedback?taskid=3072&logtype=build_status_metrics`
   - 在 `LogFeedbackServlet.LogSocket.onOpen()` 设置断点（第 140 行）
   - 验证断点能够正常触发

4. **检查日志**：
   - 应该能看到 `taskid:3072,appname:...,typies:...` 的日志输出
   - 没有 WebSocket 相关的错误或警告

## 相关参考

- [Jetty 12 Migration Guide - WebSocket](https://eclipse.dev/jetty/documentation/jetty-12/programming-guide/index.html#pg-migration-12-websocket)
- [Jetty WebSocket API vs Jakarta WebSocket](https://eclipse.dev/jetty/documentation/jetty-12/programming-guide/index.html#pg-server-websocket)
- [JettyWebSocketConfiguration JavaDoc](https://javadoc.io/doc/org.eclipse.jetty.ee11.websocket/jetty-ee11-websocket-jetty-server/latest/index.html)

## 其他需要注意的点

### 1. 依赖检查

确保 `tis-web-start/pom.xml` 包含：
```xml
<dependency>
    <groupId>org.eclipse.jetty.ee11.websocket</groupId>
    <artifactId>jetty-ee11-websocket-jetty-server</artifactId>
    <scope>compile</scope>
</dependency>
```

### 2. 如果使用 Jakarta WebSocket (@ServerEndpoint)

如果将来要使用 Jakarta WebSocket API，配置方式完全不同：
```java
// 对于 @ServerEndpoint 注解的类
JettyWebSocketServletContainerInitializer.configure(webAppContext, (context, container) -> {
    container.setDefaultMaxTextMessageBufferSize(65535);
    container.addEndpoint(MyWebSocketEndpoint.class);
});
```

### 3. 多 WebAppContext 的情况

每个需要 WebSocket 的 WebAppContext 都必须单独添加 `JettyWebSocketConfiguration`：
```java
// Context 1
webAppContext1.addConfiguration(new JettyWebSocketConfiguration());

// Context 2
webAppContext2.addConfiguration(new JettyWebSocketConfiguration());
```

### 4. 配置顺序很重要

`addConfiguration()` 必须在 `addContext()` 之前调用，且在 `setDescriptor()` 之后：
```java
webAppContext.setDescriptor(...);        // 1. 设置 web.xml
webAppContext.setDisplayName(...);       // 2. 配置其他属性
webAppContext.addConfiguration(...);     // 3. 添加 WebSocket 配置
this.addContext(webAppContext);          // 4. 最后添加到服务器
```

## Jetty 9 → 12 迁移检查清单

- [x] 更新 Jetty 版本到 12.1.7
- [x] 导入更新为 `org.eclipse.jetty.ee11.*`
- [x] Jakarta EE API 替换（`javax.*` → `jakarta.*`）
- [x] **添加 `JettyWebSocketConfiguration`**（本次修复）
- [ ] 测试所有 WebSocket 端点
- [ ] 检查 WebSocket 超时和缓冲区配置
- [ ] 验证生产环境兼容性

## 修复时间线

- **2026-07-03 15:09** - 初次尝试（错误）：使用 `JettyWebSocketServletContainerInitializer`
- **2026-07-03 15:36** - 正确修复：使用 `JettyWebSocketConfiguration`

## 修复人员

Claude Code

---

**重要提示**：如果这个修复仍然不工作，请检查：
1. 是否有自定义的类加载器干扰了 WebSocket 组件的加载
2. web.xml 中是否有冲突的 listener 或 filter 配置
3. 查看完整的启动日志，寻找 WebSocket 相关的警告或错误

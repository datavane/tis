# WebSocket 问题深度诊断和修复方案

## 当前状况

已尝试的修复：
1. ✓ 添加 `JettyWebSocketConfiguration`
2. ✓ 调整配置顺序（在 `setConfigurationDiscovered(true)` 之前）

**但问题仍然存在！**

## 深度分析后的发现

### 1. 古老的 web.xml 版本
```xml
<web-app version="2.4" xmlns="http://java.sun.com/xml/ns/j2ee"
```

- **问题**：使用 Servlet 2.4 规范（2003年）
- **影响**：在 Jetty 12 + Jakarta EE 环境下可能导致：
  - ServletContainerInitializer 不被执行
  - 注解扫描被禁用
  - WebSocket 组件无法正确初始化

### 2. 自定义类加载器
```java
TISAppClassLoader contextCloassLoader = new TISAppClassLoader(context, this.parentLoader, jarfiles);
webAppContext.setClassLoader(contextCloassLoader);
```

- **问题**：可能导致 WebSocket 类在不同类加载器中
- **影响**：WebSocket 组件无法找到 servlet 类

### 3. 依赖 scope 不一致
- `tis-console`: `scope=provided`
- `tis-web-start`: `scope=compile`

可能导致类加载冲突。

## 推荐的诊断步骤

### 步骤 1: 添加调试日志

在 `LogFeedbackServlet.java` 中添加：

```java
@Override
public void init() throws ServletException {
    super.init();
    logger.info("========= LogFeedbackServlet init() called =========");
}

@Override
public void configure(JettyWebSocketServletFactory factory) {
    logger.info("========= LogFeedbackServlet configure() called =========");
    factory.setIdleTimeout(java.time.Duration.ofMillis(240000));
    factory.setCreator((req, rep) -> {
        logger.info("========= LogSocket creator called =========");
        return new LogSocket();
    });
    this.zkGetter = BasicServlet.getBeanByType(ZooKeeperGetter.class);
    this.wfDao = BasicServlet.getBeanByType(IWorkflowDAOFacade.class);
}
```

**验证**：
- 如果 `init()` 没调用 → servlet 没被加载
- 如果 `configure()` 没调用 → WebSocket 配置没生效
- 如果 `creator` 没调用 → WebSocket 升级失败

### 步骤 2: 检查启动日志

查找以下关键字：
```
- "JettyWebSocketConfiguration"
- "WebSocket"
- "LogFeedbackServlet"
- 任何异常或错误
```

### 步骤 3: 验证 HTTP 请求

使用浏览器开发者工具或 curl 检查：
```bash
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: test" \
  http://localhost:4200/tjs/download/logfeedback?taskid=3072&logtype=build_status_metrics
```

**期望**：HTTP 101 Switching Protocols
**实际**：可能是 404、405、或其他错误

## 可能的修复方案

### 方案 A: 升级 web.xml（推荐）

将 web.xml 升级到 Jakarta EE 9+ (Servlet 5.0+):

```xml
<web-app version="5.0" 
         xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
                             https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd">
```

### 方案 B: 禁用 setConfigurationDiscovered

如果自动发现有问题，完全手动控制：

```java
webAppContext.setConfigurationDiscovered(false);  // 禁用自动发现

// 手动添加所有需要的 Configuration
webAppContext.setConfigurations(new Configuration[]{
    new org.eclipse.jetty.ee11.webapp.WebInfConfiguration(),
    new org.eclipse.jetty.ee11.webapp.WebXmlConfiguration(),
    new org.eclipse.jetty.ee11.webapp.MetaInfConfiguration(),
    new org.eclipse.jetty.ee11.webapp.FragmentConfiguration(),
    new org.eclipse.jetty.ee11.websocket.server.config.JettyWebSocketConfiguration(),
    new org.eclipse.jetty.ee11.webapp.JettyWebXmlConfiguration()
});
```

### 方案 C: 使用 Jetty 的兼容模式

在 WebAppContext 上添加：

```java
// 允许旧版本 web.xml
webAppContext.setDefaultsDescriptor(null);
// 或者设置兼容属性
webAppContext.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*\\.jar$");
```

### 方案 D: 直接在代码中配置 WebSocket (绕过 web.xml)

```java
// 在 addContext() 中，在 addContext(webAppContext) 之前
webAppContext.addServlet(new ServletHolder("logfeedback", LogFeedbackServlet.class), "/download/logfeedback");
```

### 方案 E: 修复类加载器问题

确保 WebSocket 相关的 jar 在父类加载器中：

```java
// 检查 tis-web-start 的依赖 scope
// 确保 jetty-ee11-websocket-jetty-server 是 compile scope（已经是）
// 可能需要调整 TISAppClassLoader 的父类加载器优先级
```

## 我的推荐执行顺序

1. **立即执行**：添加调试日志（步骤 1）→ 重启 → 查看日志
2. **根据日志结果**：
   - 如果 `init()` 没调用 → 使用方案 D（直接代码配置）
   - 如果 `configure()` 没调用 → 使用方案 B（禁用自动发现）
   - 如果 `creator` 没调用 → 检查 HTTP 请求（步骤 3）
3. **长期方案**：升级 web.xml 到 Jakarta EE（方案 A）

## 快速验证脚本

创建一个测试 servlet 来验证 WebSocket 基础设施：

```java
// 添加到 JettyTISRunner.java 的 addContext() 中
webAppContext.addServlet(new ServletHolder("ws-test", new JettyWebSocketServlet() {
    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        logger.info("===== TEST WebSocket servlet configure() called =====");
        factory.addMapping("/", (req, resp) -> {
            logger.info("===== TEST WebSocket creator called =====");
            return new Object() {
                @OnWebSocketOpen
                public void onOpen(Session session) {
                    logger.info("===== TEST WebSocket onOpen() called =====");
                    try {
                        session.getRemote().sendString("TEST OK");
                    } catch (Exception e) {
                        logger.error("Error", e);
                    }
                }
            };
        });
    }
}), "/ws-test");
```

访问: `ws://localhost:4200/ws-test`

如果这个测试 servlet 工作，说明 WebSocket 基础设施正常，问题在 LogFeedbackServlet 本身。
如果也不工作，说明 WebSocket 配置根本没生效。

## 下一步行动

请执行：
1. 添加调试日志
2. 重启应用
3. 尝试 WebSocket 连接
4. 将完整的启动日志和连接时的日志发给我

我需要看到：
- servlet 是否被初始化
- configure() 是否被调用
- 有没有任何异常或警告

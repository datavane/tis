TIS Pipeline Chat Agent 实现计划

     一、整体架构设计

     采用前后端分离架构，基于Plan-And-Execute Agent模式，实现自然语言驱动的数据管道创建功能。

     二、前端实现计划

     1. 完善ChatPipelineComponent组件

     - 基础UI布局：
       - 左侧：历史任务列表面板（宽度20%）
       - 右侧：主对话区域（宽度80%）
       - 底部：输入框和快捷模板按钮
       - 顶部：Token消耗显示
     - 核心功能实现：
       - SSE连接管理：处理服务端推送的消息流
       - 打字机效果：逐字显示服务端响应
       - JSON识别和渲染：自动识别JSON内容并调用PluginsComponent渲染
       - 历史任务管理：保存、展示和恢复历史对话
     - 交互组件：
       - 任务模板快捷按钮（MySQL to Paimon等常用场景）
       - 实时Token计数器
       - 任务执行状态指示器

     三、后端实现计划

     1. AI Agent核心架构

     包结构：
     com.qlangtech.tis.aiagent/
     ├── core/
     │   ├── TISPlanAndExecuteAgent.java  # 主Agent控制器
     │   ├── AgentContext.java            # Agent执行上下文
     │   └── TaskExecutor.java            # 任务执行器
     ├── plan/
     │   ├── TaskPlan.java                # 任务计划定义
     │   ├── TaskStep.java                # 任务步骤定义
     │   └── PlanGenerator.java           # 计划生成器
     ├── execute/
     │   ├── StepExecutor.java            # 步骤执行器接口
     │   ├── PluginCreateExecutor.java    # 插件创建执行器
     │   └── PipelineExecutor.java        # 管道执行器
     ├── llm/
     │   ├── LLMProvider.java             # 大模型接口抽象
     │   ├── DeepSeekProvider.java        # DeepSeek实现
     │   └── QianWenProvider.java         # 通义千问实现
     ├── template/
     │   ├── PromptTemplate.java          # 提示词模板
     │   └── TaskTemplateRegistry.java    # 任务模板注册表
     └── controller/
         └── ChatPipelineAction.java      # HTTP/SSE控制器

     2. 核心类实现

     TISPlanAndExecuteAgent：
     - 解析用户自然语言输入
     - 生成执行计划
     - 协调任务执行
     - 处理错误恢复
     - 管理与用户的交互（询问缺失参数）

     任务执行流程：
     1. 接收用户输入 → 调用LLM解析意图
     2. 匹配任务模板 → 生成执行计划
     3. 逐步执行任务 → 实时反馈进度
     4. 验证必需参数 → 询问缺失信息
     5. 创建插件实例 → 配置数据管道
     6. 触发任务执行 → 监控执行状态

     3. 插件实例创建流程

     对于每个插件实例创建：
     1. 调用DescriptorsJSONForAIPromote.desc()获取插件描述
     2. 将描述JSON发送给LLM，结合用户输入解析参数
     3. 验证必需字段，缺失则向用户询问
     4. 生成插件实例JSON配置
     5. 调用TIS插件系统API创建实例

     4. SSE推送实现

     ChatPipelineAction.doChat()：
     public void doChat(Context context) {
         String userInput = getString("input");
         String sessionId = getString("sessionId");
         
         // 设置SSE响应头
         getResponse().setContentType("text/event-stream");
         getResponse().setCharacterEncoding("UTF-8");
         
         PrintWriter writer = getResponse().getWriter();
         
         // 创建Agent执行上下文
         AgentContext ctx = new AgentContext(sessionId, writer);
         
         // 执行Agent任务
         TISPlanAndExecuteAgent agent = new TISPlanAndExecuteAgent(ctx);
         agent.execute(userInput);
     }

     四、任务模板示例

     MySQL to Paimon模板：
     1. 检查并安装插件
        - MySQL Reader插件
        - Paimon Writer插件
        
     2. 创建MySQL数据源
        - 插件：MySQLV5DataSourceFactory
        - 参数：host, port, username, password, database
        
     3. 创建MySQL Reader
        - 插件：DataxMySQLReader
        - 参数：dbName, splitPk, fetchSize
        
     4. 创建HDFS配置
        - 插件：HdfsFileSystemFactory
        
     5. 创建Hive数据源
        - 插件：Hiveserver2DataSourceFactory
        
     6. 创建Paimon Writer
        - 插件：DataxPaimonWriter
        
     7. 选择同步表
        
     8. 触发批量同步（可选）
        
     9. 配置增量同步（可选）
        - MySQL Binlog监听：FlinkCDCMySQLSourceFactory
        - Paimon实时写入：PaimonPipelineSinkFactory

     五、提示词模板设计

     你是TIS数据集成平台的智能助手。你的任务是帮助用户创建数据同步管道。

     当前任务：解析用户需求并生成数据管道配置

     用户输入：{user_input}

     可用插件列表：
     {plugin_descriptions}

     请按以下格式返回：
     1. 识别的源端类型
     2. 识别的目标端类型
     3. 提取的配置参数
     4. 缺失的必需参数

     输出JSON格式：
     {
       "source_type": "mysql",
       "target_type": "paimon",
       "extracted_params": {...},
       "missing_params": [...]
     }

     六、实施步骤

     1. 第一阶段：基础架构搭建
       - 创建AI Agent包结构
       - 实现LLM接口抽象层
       - 搭建SSE通信框架
     2. 第二阶段：前端UI开发
       - 完善ChatPipelineComponent
       - 实现打字机效果
       - 集成PluginsComponent
     3. 第三阶段：Agent核心实现
       - 实现TISPlanAndExecuteAgent
       - 开发任务执行器
       - 创建插件实例生成器
     4. 第四阶段：集成测试
       - MySQL to Paimon场景测试
       - 错误恢复测试
       - 用户交互测试
     5. 第五阶段：优化完善
       - 添加更多任务模板
       - 优化提示词
       - 完善错误处理

     七、关键技术点

     1. 插件描述JSON生成：使用DescriptorsJSONForAIPromote
     2. SSE实时通信：支持长时任务执行反馈
     3. Plan-And-Execute模式：任务分解和错误恢复
     4. LLM集成：支持多种大模型切换
     5. 插件系统集成：动态创建和配置插件实例

     这个计划将分阶段实施，确保每个组件都能正确工作并相互配合。

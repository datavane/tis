
## 需求
由于当用户通过大模型的REST-API向服务端提交请求后，在客户端需要等待比较长时间，服务端向客户端反馈结果。为了防止用户在等待过程中可以明确了解处在服务端反馈的阶段，需要在客户端展示loading加载的执行状态。

## 执行生命周期
用户向LLM REST-API 服务端提交请求后会经历三个阶段，1.Start, 2.ERROR, 3.Complete （这三个状态定义在了枚举中：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/aiagent/llm/LLMProvider.java 文件的 LLMChatPhase枚举对象）

## 实现思路

用户提交请求之后的调用执行链路

### 后端
LLMProvider.chat()或chatJson() -> DefaultExecuteLog.setPostParams() -> IAgentContext.sendLLMStatus(Start,prompt) 向客户端发送开始请求，开始显示加载状态 
-> DefaultExecuteLog.setError(JSONObject) 如果REST-API服务端执行出错，会调用该方法 -> IAgentContext.sendLLMStatus(LLMProvider.LLMChatPhase.ERROR, errMessage) 向客户端发执行出错信息，停止显示加载状态并显示错误(如errMessage参数不空)
-> DefaultExecuteLog.summary() 无论REST-API服务端执行成功失败都会调用 -> IAgentContext.sendLLMStatus(LLMProvider.LLMChatPhase.Complete, null) 如果之前链路中没有发生错误的情况下，客户端显示成功的状态显示并且停止加载状态显示

### 前端
/Users/mozhenghua/j2ee_solution/project/tis-console/src/runtime/chat.pipeline.component.ts，在 connectSSE() 方法中需要订阅 服务端 SSERunnable.SSEEventType.AI_AGNET_LLM_CHAT_STATUS 枚举与之对应的消息处理流程

## 相关脚本文件

1. LLMProvider类对应的 java文件：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/aiagent/llm/LLMProvider.java
2. DefaultExecuteLog类对应的java文件：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/llm/log/DefaultExecuteLog.java
3. IAgentContext的实现类 AgentContext.sendLLMStatus(LLMProvider.LLMChatPhase llmChatPhase, String detailInfo) 实现方法所在位置：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-console/src/main/java/com/qlangtech/tis/aiagent/core/AgentContext.java#L248
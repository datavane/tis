## 需求
用户点击`发送`按钮后，客户端与服务端通过SSE协议进行交互，用户在发送请求后可以终止服务端执行流程，可以避免长时间无效等待和服务端浪费计算资源。

## 需求细化

### 前端
相关文件`chat.pipeline.component.ts`中点击`发送`按钮后，当isTyping属性变成false说明服务端已经开始响应开始处理请求，随即`发送`按钮需要切换模式，变成等待响应终止服务端执行的模式（按钮外观请自行决定，与其他组件风格保持一致即可），
当用户点击按钮后需要向服务端发送终止任务执行请求。当得到服务端成功的反馈后需要在前端调用 `BasicFormComponent.successNotify(msg: string, duration?: number): NzNotificationRef() `向用户告知，"当前执行任务已经停止"。
完成后，按钮又切换成等待触发服务端任务的`发送`模式

### 后端
前端向后端发送停止任务执行的请求，响应的Action为ChatPipelineAction中的doCancelCurrentTask() 方法，需要让TaskPlan中对应的step都不会执行，当前正在执行的step如果正在执行就让它执行完毕（如果需要强制终止当前正在执行的step恐怕会让程序的执行逻辑变复杂）


## 资源详细

* chat.pipeline.component.ts文件：/Users/mozhenghua/j2ee_solution/project/tis-console/src/runtime/chat.pipeline.component.ts
* ChatPipelineAction 类对应的文件：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-console/src/main/java/com/qlangtech/tis/coredefine/module/action/ChatPipelineAction.java
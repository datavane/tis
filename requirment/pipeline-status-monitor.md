# 总体需求说明
本需求需要监控TIS集群运行中的实时增量实例，当某个实例因为未知错误终止，或者用户手动终止。会自动触发TIS中定义的报警机制通知用户，能够及时处理TIS Flink 机器中的异常job任务。

# 具体实现

本需求借鉴[Apache StreamPark](https://github.com/apache/streampark)中Flink Job状态监听实现的思路实现。
当StreamPark启动时，在Spring容器中会启动一个Flink Job状态轮询器（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/watcher/FlinkAppHttpWatcher.java），内有
入口为start()函数，按照 @Scheduled 标注定义为 5秒中执行一次，主要功能为收集集群中Flink Job的任务状态，按照任务状态发送报警。当然 FlinkAppHttpWatcher内部功能还不仅仅是监控状态&报警，还包括metric指标收集、savepoint记录等等（这些功能在本需求迭代中并不需要，选择忽略）。

## 报警消息监听发现

TIS中需要复刻 FlinkAppHttpWatcher.java中与flink job监控关键执行点，着重关注每次Flink Job状态轮询结果处理，在满足何种状态下触发报警，这部分代码放在tis-console（/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-console）
的 com/qlangtech/tis/alert 包路径下（类名就叫 `FlinkJobsMonitor`），配置在Spring配置文件中：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-console/src/main/resources/tis.application.context.xml 
，FlinkJobsMonitor.executeTask() 方法（它对等FlinkAppHttpWatcher.watch()方法，我注意到每次watch()方法是提交到watchExecutor执行器会开启异步线程，这是否必要？如果每次轮询都在异步线程中执行，那么是否会发生前一次轮询还未结束后一次的轮询已经开始了，这样任务很容易就堆积了）定时执行

FlinkAppHttpWatcher在执行轮询过程中会对使用yarn模式部署的Flink实例使用特别的rest api获取flink job的状态，由于TIS中flink job 完全摒弃了yarn模式部署，所以FlinkAppHttpWatcher中有对yarn模式的特殊处理不用处理直接跳过。

## 报警消息发送

在FlinkAppHttpWatcher.java中发现Flink的任务有变化，并且满足触发报警消息条件会调用doAlert() 方法，最终会调用 AlertServiceImpl.java（ 路径为：/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/AlertServiceImpl.java）
的 triggerAlert() 方法，这里实现的思路是轮询枚举类型AlertTypeEnum（路径：/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/enums/AlertTypeEnum.java）的所有告警渠道（alert Channel）枚举实例。
在枚举实例上定义了对应alert Channel消息发送的Service，如下：
1. 邮件 email：EmailAlertNotifyServiceImpl（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/EmailAlertNotifyServiceImpl.java），
   对应smtp配置参数可参照[EmailConfig.java](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/EmailConfig.java)
2. 钉钉 DING_TALK：DingTalkAlertNotifyServiceImpl（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/DingTalkAlertNotifyServiceImpl.java）
3. 企业微信 WE_COM： WeComAlertNotifyServiceImpl（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/WeComAlertNotifyServiceImpl.java）
4. 原始Http回调 HTTP_CALLBACK：HttpCallbackAlertNotifyServiceImpl（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/HttpCallbackAlertNotifyServiceImpl.java）
5. 飞书 LARK： LarkAlertNotifyServiceImpl（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/LarkAlertNotifyServiceImpl.java）

本次需求迭代中TIS实现的报警渠道也需要实现以上这5个渠道，已经在TIS中定义了报警渠道扩展点 alertChannel（/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/alert/alertChannel.java）目前只是一个骨架需要添加业务方法，内有一个抽象方法sendAlert() 用于发送报警消息。

alertChannel的5个报警渠道插件的实现类文件请放置在 插件工程tis-realtime-flink（/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink）中，具体类就放置在 pacakge： com/qlangtech/plugins/incr/flink/alert下即可（类名的后缀为`alertChannel`,如：钉钉'DingTalkalertChannel'）
以下是几个报警渠道插件类的说明：

1. 邮件 EmailalertChannel
   对应smtp配置参数可参照[EmailConfig.java](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/EmailConfig.java)
   通过EmailConfig类可以获得发送邮件需要的smtp配置参数，这些参数需要转化并且映射成EmailalertChannel中公有的成员属性，具体映射方式请查看以下 `插件成员属性生成规则`段落
2. 钉钉 DingTalkalertChannel
   对应配置类可参考类[AlertDingTalkParams.java](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/AlertDingTalkParams.java)
3. 企业微信 WeComalertChannel
   对应配置类可参考类[AlertWeComParams.java](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/AlertWeComParams.java)
4. Http回调 HttpCallbackalertChannel
   对应配置类可参考类[AlertHttpCallbackParams.java](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/AlertHttpCallbackParams.java)
5. 飞书 LarkalertChannel
   对应配置类可参考类[AlertLarkParams.java](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/AlertLarkParams.java)

以上每个发送渠道插件(都需要继承于alertChannel类)，在发送告警消息过程中会通过当前Flink Job的状态实例对象[AlertTemplate](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/bean/AlertTemplate.java)
结合各自对应的freemark模版（例如：邮件发送渠道使用模版[alert-email.ftl](/opt/misc/streampark/streampark-console/streampark-console-service/src/main/resources/alert-template/alert-email.ftl)），不过TIS中不使用freemark，而是使用velocity模版引擎的，所以claude需要你把对应的freemark模版转换成velocity模版
放到各自的namespace的resources目录下（例如：邮件发送渠道使用模版 alert-email.vm 放置在 /Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/resources/com/qlangtech/plugins/incr/flink/alert，保持和EmailalertChannel类在通过package空间下）

利用velocity渲染模版需要调用 [DataXCfgGenerator](/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/datax/impl/DataXCfgGenerator.java)类的 evaluateTemplate()方法来进行渲染

用户在配置报警渠道插件时，需要可以各自设置报警信息的velocity模版，所以我已经在alertChannel中添加了alertTpl属性，用于保存每个渠道报警信息模版。alertTpl属性前端展示的样式，已经在alertChannel.json文件中定义了。
另外，由于每一个报警渠道的模版默认内容是不同的，需要在各自报警渠道插件中添加public static的方法用于获取默认模版的方法，以`EmailalertChannel`为例：
```java
public class EmailalertChannel extends alertChannel {
    public static String loadDefaultTpl() {
        // TODO 加载Email发送渠道的默认模版
        return null;
    }
}
```
然后需要将loadDefaultTpl方法注册到EmailalertChannel.json(属性描述文件)中，内容如下：
```json
{
  "alertTpl": {
    "dftVal": "com.qlangtech.plugins.incr.flink.alert.EmailAlertChannel.loadDefaultTpl()"
  }
}
```

### 插件成员属性生成规则
这里以`EmailalertChannel`为例，讲述内部smtp配置参数相关的配置参数生成规则，其他报警发送渠道可以以此为例举一反三推而广之。

首选，通过EmailConfig类可以获得smtp配置相关的参数为以下这些参数：
1. smtpHost
2. smtpPort
3. from
4. userName
5. password
6. ssl
以上属性都是有明确强类型申明的，这样可以直接映射到EmailalertChannel类中，以属性smtpHost为例：

```java
public class EmailalertChannel extends alertChannel {

    private static final String KEY_DISPLAY_NAME = "Email";

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.hostWithoutPort})
    public String smtpHost;

    @Override
    public void sendAlert() {
        //TODO：参照： EmailAlertNotifyServiceImpl（/opt/misc/streampark/streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/service/alert/impl/EmailAlertNotifyServiceImpl.java）
    }


    @TISExtension()
    public static class Desc extends alertChannelDescDesc {
        @Override
        public String getDisplayName() {
            return KEY_DISPLAY_NAME;
        }
    }
}
```
在EmailalertChannel 添加 smtpHost属性，必须为public，String类型，非常重要的需要设置`@FormField`（/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/FormField.java） annotation，以下对@FormField各个属性的说明：
1. identity：该属性值为该插件的唯一值，当定义多个同类型的插件实例，可以使用该值代表该插件的主键id，EmailalertChannel的主键属性定义在alertChannel中name属性
2. ordinal：前端UI表单中放置的顺序，多个属性需要按照重要级排序，ordinal越小代表优先级越高，会被放置在表单的前部
3. advance：插件中有些参数是比较少设置的属于高级设置，且优先级比较低（必须要有默认值），不用的时候可以选择隐藏
4. validate：前端提交时需要按照定义的validate的规则进行校验，[Validator](/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/Validator.java)
   1. require： 必须输入
   2. user_name：满足用户名的规则要求，必须由小写字母，大写字母，数字、下划线、点、减号组成
   3. email：满足邮箱规则
   4. forbid_start_with_number：禁止以数字开头
   5. identity：满足主键规则要求
   6. integer：满足数字规则
   7. host：满足互联网域名规则，可以带端口，如：`192.168.28.200:7070`(合法)
   8. hostWithoutPort：满足互联网域名规则，不可以带端口
   9. url：满足互联网域名规则，以https或者http开头
   10. db_col_name：满足数据库列名规则
   11. relative_path：满足文件系统相对路径规则
   12. absolute_path：满足`unix`文件系统绝对路径规则
   13. none_blank：输入内容不能为空
   
   5. type 字段类型[FormFieldType](/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/FormFieldType.java)

      1. MULTI_SELECTABLE: 支持多选，对应属性类型为List<IdentityName>或者 List<String>
      2. INPUTTEXT：对应属性为String，属性内容为单行内容，例如：用户名
      3. SELECTABLE：支持多选，在前端展示使用selector控件，下拉可选项，在插件的Descriptor中通过调用 registerSelectOptions()方法设置，例如：
         [AliayunJindoFSFactory](/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-aliyun-jindo-sdk-extends/tis-datax-hdfs-aliyun-emr-plugin/src/main/java/com/qlangtech/tis/hdfs/impl/AliayunJindoFSFactory.java)
          ``` java
          public class AliayunJindoFSFactory extends HdfsFileSystemFactory {
            public static final String FIELD_ENDPOINT = "endpoint";

           @FormField(ordinal = 2, type = FormFieldType.SELECTABLE, validate = {Validator.require})
           public String endpoint;

           @TISExtension
           public static class DefaultDescriptor extends HdfsFileSystemFactory.DefaultDescriptor {
             public DefaultDescriptor() {
               super();
               registerSelectOptions(FIELD_ENDPOINT, () -> ParamsConfig.getItems(IHttpToken.KEY_FIELD_ALIYUN_TOKEN));
             }
           }
          }
         ```
      4. PASSWORD：输入密码类型
      5. FILE：支持文件上传，属性类型为 String，一个表单只能有一个文件上传输入项，所在的插件类需要实现ITmpFileStore(/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/ITmpFileStore.java)接口，可以参照[UploadKrb5Res](/Users/mozhenghua/j2ee_solution/project/plugins/tis-kerberos-plugin/src/main/java/com/qlangtech/tis/kerberos/impl/UploadKrb5Res.java)中的file属性
      6. TEXTAREA：支持多行文件输入，例如需要在前端页面中设置SQL脚本，XML脚本等
      7. DATE：支持日期类型
      8. JDBCColumn：支持jdbc列类型
      9. INT_NUMBER：支持整型数字输入
      10. ENUM：支持多个可选项输入，例如：前端页面中下拉列表支持 ‘是’‘否’单选
      11. DateTime：日期精确到秒，对应属性可以为long，也可以为java.util.Date，对应保存的值代表UTC的时间
      12. DECIMAL_NUMBER：浮点数字
      13. DURATION_OF_SECOND：时间跨度，单位：秒
      14. DURATION_OF_MINUTE：时间跨度，单位：分钟
      15. DURATION_OF_HOUR：时间跨度，单位：小时
      16. MEMORY_SIZE_OF_BYTE：代表存储大小（磁盘，内存等），单位：byte字节
      17. MEMORY_SIZE_OF_KIBI：代表存储大小（磁盘，内存等），单位：kb 千字节
      18. MEMORY_SIZE_OF_MEGA：代表存储大小（磁盘，内存等），单位：mb 兆字节

EmailalertChannel插件需要在同一个package的 resources目录中（/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/resources/com/qlangtech/plugins/incr/flink/alert/）添加EmailalertChannel.json（文件名与插件类名保持一致，文件扩展名为json）
，该文件内保存插件中由@FormField 标注的属性，在前端展示的一些额外属性，如：默认值、text input的placeholder，简单帮助信息等。下面以smtpHost属性为例说明：
```json
{
  "smtpHost": {
    "lable": "SmtpHost",
    "placehold": "smtp.163.com",
    "help": "SMTP协议服务端host地址"
  }
}
```
在例如，EmailConfig的ssl属性为boolean属性，映射到EmailalertChannel也是Boolean属性，那在EmailalertChannel.json文件中就会为ssl配置：
```json
{
  "smtpHost": {
  },
  "ssl": {
    "lable": "支持ssl",
    "dftVal": false, // 输入项的默认值
    "enum": [
      {
        "label": "是",
        "val": true
      },
      {
        "label": "否",
        "val": false
      }
    ],
    "help": "邮件发送是否支持ssl"
  }
}
```
如果某属性在json文件通过简单文字还不足以说明，需要使用markdown的结构化说明，需要在插件同package的resources的目录下添加markdown文件
，例如：EmailalertChannel.md，假设ssl属性需要设置markdown格式的help说明内容，可按照如下设置：
```markdown
## ssl
邮件发送是否支持`ssl`加密机制
```
属性名前必须需要添加`##`二级header标识。一个属性如果既有json内的help属性设置，也有对应markdown文件中的帮助信息，则markdown中定义的`help说明信息`优先级高，前端页面展示时会显示markdown中定义的`help说明信息`

## 报警消息订阅

这部分先不作设计，下一阶段实施
TIS的登录用户，默认是不会接收到flink集群中的job报警信息的，需要完成`报警消息订阅`流程才有机会收到报警信息。



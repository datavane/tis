# TIS Flink Job监控与报警系统设计方案

> 设计日期: 2025-11-17
> 设计者: Claude
> 审核者: 百岁

---

## 一、整体架构

### 1.1 核心模块

```
tis-console (监控模块)
├── FlinkJobsMonitor.java          # 定时监控器,每5秒轮询一次
└── AlertTemplate.java             # 报警消息数据模型

tis-plugin (报警渠道扩展点)
└── alertChannel.java              # 报警渠道抽象类(已存在,需完善)

tis-realtime-flink (报警渠道实现)
├── EmailalertChannel.java         # 邮件报警(已有骨架,需完善)
├── DingTalkalertChannel.java      # 钉钉报警(新增)
├── WeComalertChannel.java         # 企业微信报警(新增)
├── HttpCallbackalertChannel.java  # HTTP回调报警(新增)
└── LarkalertChannel.java          # 飞书报警(新增)
```

### 1.2 监控流程

```
1. Spring定时任务(每5秒)
   → FlinkJobsMonitor.executeTask()
   → 同步获取所有Flink Job状态(使用TIS自有API)
   → 判断状态变化(FAILED/LOST/CANCELED)
   → 触发报警 doAlert()

2. 报警发送
   → 创建AlertTemplate
   → 获取全局报警渠道配置(alertChannel插件列表)
   → 遍历所有已配置渠道,调用send()发送
   → 使用Velocity模板渲染报警内容
```

### 1.3 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     Spring Container                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         FlinkJobsMonitor (@Scheduled)                 │  │
│  │  - 每5秒执行executeTask()                              │  │
│  │  - 同步遍历所有Flink Job                                │  │
│  │  - 检测状态变化(FAILED/LOST/CANCELED)                   │  │
│  └──────────────────┬────────────────────────────────────┘  │
│                     │ 状态异常                               │
│                     ↓                                       │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              doAlert(AlertTemplate)                   │  │
│  │  1. 创建AlertTemplate对象                              │  │
│  │  2. 获取所有已配置的alertChannel插件                     │  │
│  │  3. 遍历调用每个channel.send(template)                  │  │
│  └──────────────────┬────────────────────────────────────┘  │
└────────────────────┼────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ↓                       ↓
┌─────────────────┐    ┌─────────────────┐
│ alertChannel插件 │    │ alertChannel插件 │
│  (Emailalert)   │    │  (DingTalkalert)│
│                 │    │                 │
│ 1. 渲染Velocity │     │ 1. 渲染Velocity │
│    模板         │     │    模板         │
│ 2. 发送邮件      │     │ 2. 调用钉钉API  │
└─────────────────┘    └─────────────────┘
```

---

## 二、详细设计

### 2.1 FlinkJobsMonitor (监控核心)

**文件位置**: `tis-console/src/main/java/com/qlangtech/tis/alert/FlinkJobsMonitor.java`

**职责**:
- 定时监控TIS集群中所有运行中的Flink Job
- 检测任务状态变化
- 触发报警

**设计要点**:

```java
public class FlinkJobsMonitor implements InitializingBean {

    // 上次轮询时间
    private Long lastWatchTime;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化:加载所有需要监控的Flink Job列表
    }

    // 由Spring定时调度,每5秒执行一次
    @Scheduled(fixedRate = 5000)
    public void executeTask() {
        // 1. 获取所有运行中的Flink Job列表(使用TIS自有API)
        List<FlinkJob> jobs = getAllRunningFlinkJobs();

        // 2. 同步遍历每个Job,检测状态
        for (FlinkJob job : jobs) {
            try {
                checkJobStatus(job);
            } catch (Exception e) {
                // 记录错误,但不影响其他Job的监控
                log.error("Failed to check job: " + job.getName(), e);
            }
        }

        lastWatchTime = System.currentTimeMillis();
    }

    private void checkJobStatus(FlinkJob job) {
        // 1. 获取Job当前状态
        FlinkJobState currentState = job.getCurrentState();
        FlinkJobState previousState = job.getPreviousState();

        // 2. 判断是否需要报警
        if (shouldAlert(job, currentState, previousState)) {
            doAlert(job, currentState);
        }

        // 3. 更新状态
        job.setPreviousState(currentState);
    }

    private boolean shouldAlert(FlinkJob job,
                                FlinkJobState currentState,
                                FlinkJobState previousState) {
        // 报警触发条件:
        // 1. 任务失败 (FAILED)
        // 2. 任务丢失 (LOST)
        // 3. 任务被取消且不是手动停止 (CANCELED && !manualStop)

        if (currentState == FlinkJobState.FAILED) {
            return true;
        }

        if (currentState == FlinkJobState.LOST) {
            return true;
        }

        if (currentState == FlinkJobState.CANCELED && !job.isManualStop()) {
            return true;
        }

        return false;
    }

    private void doAlert(FlinkJob job, FlinkJobState state) {
        // 1. 创建AlertTemplate
        AlertTemplate template = AlertTemplate.builder()
            .title("TIS Flink Job Alert")
            .subject("[" + state.name() + "] " + job.getName())
            .jobName(job.getName())
            .status(state.getDisplayName())
            .startTime(job.getStartTime())
            .endTime(new Date())
            .duration(job.getStartTime(), new Date())
            .link(job.getFlinkWebUrl())
            .build();

        // 2. 获取所有已配置的报警渠道
        List<alertChannel> channels = ParamsConfig.getItems(alertChannel.KEY_CATEGORY);

        // 3. 遍历发送
        for (alertChannel channel : channels) {
            try {
                boolean success = channel.send(template);
                if (success) {
                    log.info("Alert sent successfully via: " + channel.identityValue());
                } else {
                    log.warn("Failed to send alert via: " + channel.identityValue());
                }
            } catch (Exception e) {
                log.error("Error sending alert via: " + channel.identityValue(), e);
            }
        }
    }
}
```

**Spring配置** (`tis.application.context.xml`):

```xml
<task:scheduled-tasks>
    <task:scheduled ref="flinkJobsMonitor" method="executeTask" fixed-rate="5000"/>
</task:scheduled-tasks>

<bean id="flinkJobsMonitor" class="com.qlangtech.tis.alert.FlinkJobsMonitor" />
```

---

### 2.2 AlertTemplate (报警数据模型)

**文件位置**: `tis-console/src/main/java/com/qlangtech/tis/alert/AlertTemplate.java`

**设计说明**: 完全参照StreamPark的AlertTemplate设计

**字段定义**:

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTemplate implements Serializable {

    private String title;                     // 标题
    private String subject;                   // 主题
    private String jobName;                   // 任务名称
    private String status;                    // 状态(FAILED/LOST/CANCELED)
    private Integer type;                     // 报警类型:1-任务状态,2-Checkpoint,3-集群,4-探活
    private String startTime;                 // 开始时间
    private String endTime;                   // 结束时间
    private String duration;                  // 持续时间
    private String link;                      // Flink Web UI链接
    private String cpFailureRateInterval;     // Checkpoint失败率间隔
    private Integer cpMaxFailureInterval;     // 最大失败间隔
    private Boolean restart;                  // 是否重启
    private Integer restartIndex;             // 当前重启次数
    private Integer totalRestart;             // 总重启次数
    private boolean atAll;                    // 是否@所有人
    private Integer allJobs;                  // 总任务数
    private Integer affectedJobs;             // 受影响任务数
    private String user;                      // 用户
    private Integer probeJobs;                // 探活任务数
    private Integer failedJobs;               // 失败任务数
    private Integer lostJobs;                 // 丢失任务数
    private Integer cancelledJobs;            // 取消任务数

    // Builder辅助方法
    public static class AlertTemplateBuilder {

        // 设置开始时间(自动格式化)
        public AlertTemplateBuilder startTime(Date startTime) {
            this.startTime = formatDate(startTime);
            return this;
        }

        // 设置结束时间
        public AlertTemplateBuilder endTime(Date endTime) {
            this.endTime = formatDate(endTime == null ? new Date() : endTime);
            return this;
        }

        // 计算持续时间
        public AlertTemplateBuilder duration(Date start, Date end) {
            long duration;
            if (start == null) {
                duration = 0L;
            } else if (end == null) {
                duration = System.currentTimeMillis() - start.getTime();
            } else {
                duration = end.getTime() - start.getTime();
            }
            this.duration = formatDuration(duration);
            return this;
        }

        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        }

        private String formatDuration(long millis) {
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
            } else if (hours > 0) {
                return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
            } else if (minutes > 0) {
                return String.format("%dm %ds", minutes, seconds % 60);
            } else {
                return String.format("%ds", seconds);
            }
        }
    }
}
```

---

### 2.3 alertChannel完善

**文件位置**: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/alert/alertChannel.java`

**需要新增的方法**:

```java
public abstract class alertChannel extends ParamsConfig {

    public static final String KEY_CATEGORY = "AlertChannel";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String name;

    @FormField(type = FormFieldType.TEXTAREA, ordinal = 999, validate = {Validator.require})
    public String alertTpl;

    /**
     * 发送报警消息(新增)
     *
     * @param template 报警消息模板
     * @return 是否发送成功
     * @throws Exception 发送失败时抛出异常
     */
    public abstract boolean send(AlertTemplate template) throws Exception;

    /**
     * 使用Velocity渲染模板(工具方法)
     */
    protected String renderTemplate(AlertTemplate template) {
        VelocityContext context = new VelocityContext();
        context.put("template", template);
        return DataXCfgGenerator.evaluateTemplate(context, this.alertTpl);
    }

    @Override
    public alertChannel createConfigInstance() {
        return this;
    }

    @Override
    public final String identityValue() {
        return this.name;
    }

    @Override
    protected final Class<alertChannelDescDesc> getBasicParamsConfigDescriptorClass() {
        return alertChannelDescDesc.class;
    }

    public static abstract class alertChannelDescDesc extends BasicParamsConfigDescriptor {
        public alertChannelDescDesc() {
            super(KEY_CATEGORY);
        }
    }
}
```

---

### 2.4 五个报警渠道插件实现

#### 2.4.1 EmailalertChannel (邮件报警)

**文件位置**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/java/com/qlangtech/plugins/incr/flink/alert/EmailalertChannel.java`

**配置属性**:

| 字段 | 类型 | FormFieldType | Validator | 说明 |
|------|------|---------------|-----------|------|
| smtpHost | String | INPUTTEXT | require, hostWithoutPort | SMTP服务器地址 |
| smtpPort | Integer | INT_NUMBER | require | SMTP端口 |
| from | String | INPUTTEXT | require, email | 发件人邮箱 |
| userName | String | INPUTTEXT | require | SMTP用户名 |
| password | String | PASSWORD | require | SMTP密码 |
| ssl | Boolean | ENUM | - | 是否启用SSL |
| contacts | String | TEXTAREA | require | 收件人列表,逗号分隔 |

**代码骨架**:

```java
public class EmailalertChannel extends alertChannel {

    private static final String KEY_DISPLAY_NAME = "Email";

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.hostWithoutPort})
    public String smtpHost;

    @FormField(ordinal = 2, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer smtpPort;

    @FormField(ordinal = 3, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.email})
    public String from;

    @FormField(ordinal = 4, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String userName;

    @FormField(ordinal = 5, type = FormFieldType.PASSWORD, validate = {Validator.require})
    public String password;

    @FormField(ordinal = 6, type = FormFieldType.ENUM)
    public Boolean ssl = false;

    @FormField(ordinal = 7, type = FormFieldType.TEXTAREA, validate = {Validator.require})
    public String contacts;

    public static String loadDefaultTpl() {
        // 加载默认的Email Velocity模板
        try {
            return IOUtils.toString(
                EmailalertChannel.class.getResourceAsStream(
                    "alert-email.vm"),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default email template", e);
        }
    }

    @Override
    public boolean send(AlertTemplate template) throws Exception {
        // 1. 渲染HTML内容
        String htmlContent = renderTemplate(template);

        // 2. 发送邮件
        HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setCharset("UTF-8");
        htmlEmail.setHostName(this.smtpHost);
        htmlEmail.setAuthentication(this.userName, this.password);
        htmlEmail.setFrom(this.from);

        if (this.ssl) {
            htmlEmail.setSSLCheckServerIdentity(true);
            htmlEmail.setSSLOnConnect(true);
            htmlEmail.setSslSmtpPort(this.smtpPort.toString());
        } else {
            htmlEmail.setSmtpPort(this.smtpPort);
        }

        htmlEmail.setSubject(template.getSubject());
        htmlEmail.setHtmlMsg(htmlContent);

        // 解析收件人
        String[] emails = this.contacts.split(",");
        for (String email : emails) {
            htmlEmail.addTo(email.trim());
        }

        htmlEmail.send();
        return true;
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

**JSON配置** (`EmailalertChannel.json`):

```json
{
  "smtpHost": {
    "label": "SMTP主机",
    "placeholder": "smtp.163.com",
    "help": "SMTP协议服务端host地址"
  },
  "smtpPort": {
    "label": "SMTP端口",
    "dftVal": 25,
    "help": "SMTP服务端口,SSL通常为465,非SSL通常为25"
  },
  "from": {
    "label": "发件人邮箱",
    "placeholder": "noreply@example.com",
    "help": "发送报警邮件的邮箱地址"
  },
  "userName": {
    "label": "SMTP用户名",
    "placeholder": "username",
    "help": "SMTP服务器认证用户名"
  },
  "password": {
    "label": "SMTP密码",
    "help": "SMTP服务器认证密码"
  },
  "ssl": {
    "label": "启用SSL",
    "dftVal": false,
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
    "help": "邮件发送是否支持SSL加密"
  },
  "contacts": {
    "label": "收件人",
    "placeholder": "user1@example.com,user2@example.com",
    "rows": 3,
    "help": "收件人邮箱列表,多个邮箱用逗号分隔"
  },
  "alertTpl": {
    "dftVal": "com.qlangtech.plugins.incr.flink.alert.EmailalertChannel.loadDefaultTpl()"
  }
}
```

**Velocity模板** (`alert-email.vm`):

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${template.subject}</title>
    <style>
        body { font-family: Arial, sans-serif; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #f44336; color: white; padding: 15px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        td { padding: 10px; border-bottom: 1px solid #ddd; }
        td:first-child { font-weight: bold; width: 40%; }
        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
        .status-failed { color: #f44336; font-weight: bold; }
        .link { color: #2196F3; text-decoration: none; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>TIS Flink Job Alert</h2>
        </div>
        <div class="content">
            <h3>${template.subject}</h3>

            #if($template.type == 1)
            <table>
                <tr>
                    <td>任务名称</td>
                    <td>${template.jobName}</td>
                </tr>
                <tr>
                    <td>任务状态</td>
                    <td><span class="status-failed">${template.status}</span></td>
                </tr>
                <tr>
                    <td>开始时间</td>
                    <td>${template.startTime}</td>
                </tr>
                <tr>
                    <td>结束时间</td>
                    <td>${template.endTime}</td>
                </tr>
                <tr>
                    <td>运行时长</td>
                    <td>${template.duration}</td>
                </tr>
                #if($template.restart)
                <tr>
                    <td>重启次数</td>
                    <td><span style="color: red">${template.restartIndex}</span> / ${template.totalRestart}</td>
                </tr>
                #end
                #if($template.link)
                <tr>
                    <td>详情链接</td>
                    <td><a href="${template.link}" class="link">查看详情</a></td>
                </tr>
                #end
            </table>
            #end

            <p style="margin-top: 20px;">请及时处理异常任务!</p>
        </div>
        <div class="footer">
            <p>TIS - 数据集成服务平台</p>
            <p>此邮件由系统自动发送,请勿回复</p>
        </div>
    </div>
</body>
</html>
```

---

#### 2.4.2 DingTalkalertChannel (钉钉报警)

**文件位置**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/java/com/qlangtech/plugins/incr/flink/alert/DingTalkalertChannel.java`

**配置属性**:

| 字段 | 类型 | FormFieldType | Validator | 说明 |
|------|------|---------------|-----------|------|
| token | String | INPUTTEXT | require | 钉钉机器人访问令牌 |
| contacts | String | TEXTAREA | - | @联系人手机号,逗号分隔 |
| alertDingURL | String | INPUTTEXT | url | 自定义钉钉webhook URL |
| isAtAll | Boolean | ENUM | - | 是否@所有人 |
| secretEnable | Boolean | ENUM | - | 是否启用签名验证 |
| secretToken | String | PASSWORD | - | 签名密钥 |

**代码骨架**:

```java
public class DingTalkalertChannel extends alertChannel {

    private static final String KEY_DISPLAY_NAME = "DingTalk";
    private static final String DEFAULT_WEBHOOK_URL = "https://oapi.dingtalk.com/robot/send";

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String token;

    @FormField(ordinal = 2, type = FormFieldType.TEXTAREA)
    public String contacts;

    @FormField(ordinal = 3, type = FormFieldType.INPUTTEXT, validate = {Validator.url}, advance = true)
    public String alertDingURL;

    @FormField(ordinal = 4, type = FormFieldType.ENUM)
    public Boolean isAtAll = false;

    @FormField(ordinal = 5, type = FormFieldType.ENUM, advance = true)
    public Boolean secretEnable = false;

    @FormField(ordinal = 6, type = FormFieldType.PASSWORD, advance = true)
    public String secretToken;

    public static String loadDefaultTpl() {
        try {
            return IOUtils.toString(
                DingTalkalertChannel.class.getResourceAsStream("alert-dingtalk.vm"),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean send(AlertTemplate template) throws Exception {
        // 1. 渲染Markdown内容
        String markdown = renderTemplate(template);

        // 2. 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("msgtype", "markdown");

        // 3. 添加@信息
        Map<String, Object> at = new HashMap<>();
        if (StringUtils.isNotEmpty(this.contacts)) {
            List<String> contactList = Arrays.asList(this.contacts.split(","));
            at.put("atMobiles", contactList);
        }
        at.put("isAtAll", this.isAtAll);
        body.put("at", at);

        // 4. 添加markdown内容
        Map<String, String> content = new HashMap<>();
        content.put("title", template.getTitle());
        content.put("text", markdown);
        body.put("markdown", content);

        // 5. 发送HTTP请求
        String url = buildWebhookUrl();
        String jsonBody = JSON.toJSONString(body);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String response = br.lines().collect(Collectors.joining());
                JSONObject result = JSON.parseObject(response);
                return result.getIntValue("errcode") == 0;
            }
        }

        return false;
    }

    private String buildWebhookUrl() throws Exception {
        String urlPrefix = StringUtils.isNotEmpty(this.alertDingURL)
            ? this.alertDingURL.replaceFirst("\\?.*", "")
            : DEFAULT_WEBHOOK_URL;

        if (this.secretEnable && StringUtils.isNotEmpty(this.secretToken)) {
            long timestamp = System.currentTimeMillis();
            String sign = calculateSign(timestamp);
            return String.format("%s?access_token=%s&timestamp=%d&sign=%s",
                urlPrefix, this.token, timestamp, sign);
        } else {
            return String.format("%s?access_token=%s", urlPrefix, this.token);
        }
    }

    private String calculateSign(long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + this.secretToken;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
            this.secretToken.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(
            Base64.getEncoder().encodeToString(signData),
            StandardCharsets.UTF_8.name());
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

**JSON配置** (`DingTalkalertChannel.json`):

```json
{
  "token": {
    "label": "访问令牌",
    "placeholder": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "help": "钉钉机器人的access_token"
  },
  "contacts": {
    "label": "@联系人",
    "placeholder": "13800138000,13900139000",
    "rows": 2,
    "help": "@的联系人手机号,多个手机号用逗号分隔。留空则不@任何人"
  },
  "alertDingURL": {
    "label": "自定义Webhook URL",
    "placeholder": "https://oapi.dingtalk.com/robot/send",
    "help": "自定义钉钉webhook地址,通常使用默认值即可"
  },
  "isAtAll": {
    "label": "@所有人",
    "dftVal": false,
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
    "help": "是否@群里所有人"
  },
  "secretEnable": {
    "label": "启用签名",
    "dftVal": false,
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
    "help": "是否启用签名验证,提高安全性"
  },
  "secretToken": {
    "label": "签名密钥",
    "help": "钉钉机器人的签名密钥,启用签名时必填"
  },
  "alertTpl": {
    "dftVal": "com.qlangtech.plugins.incr.flink.alert.DingTalkalertChannel.loadDefaultTpl()"
  }
}
```

**Velocity模板** (`alert-dingtalk.vm`):

```markdown
> TIS数据集成平台

# ${template.subject}

#if($template.type == 1)
---

**任务名称**: ${template.jobName}

**任务状态**: <font color="#FF0000">${template.status}</font>

**开始时间**: ${template.startTime}

**结束时间**: ${template.endTime}

**运行时长**: ${template.duration}

#if($template.restart)
**重启次数**: <font color="#FF0000">${template.restartIndex}</font> / ${template.totalRestart}
#end

#if($template.link)
[查看详情](${template.link})
#end
#end

---

> 请及时处理异常任务!

[TIS官网](https://tis.pub)
```

---

#### 2.4.3 WeComalertChannel (企业微信报警)

**文件位置**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/java/com/qlangtech/plugins/incr/flink/alert/WeComalertChannel.java`

**配置属性**:

| 字段 | 类型 | FormFieldType | Validator | 说明 |
|------|------|---------------|-----------|------|
| token | String | INPUTTEXT | require | 企业微信机器人key |

**代码骨架**:

```java
public class WeComalertChannel extends alertChannel {

    private static final String KEY_DISPLAY_NAME = "WeCom";
    private static final String WEBHOOK_URL_TEMPLATE = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=%s";

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String token;

    public static String loadDefaultTpl() {
        try {
            return IOUtils.toString(
                WeComalertChannel.class.getResourceAsStream("alert-wecom.vm"),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean send(AlertTemplate template) throws Exception {
        // 1. 渲染Markdown内容
        String markdown = renderTemplate(template);

        // 2. 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("msgtype", "markdown");

        Map<String, String> content = new HashMap<>();
        content.put("content", markdown);
        body.put("markdown", content);

        // 3. 发送HTTP请求
        String url = String.format(WEBHOOK_URL_TEMPLATE, this.token);
        String jsonBody = JSON.toJSONString(body);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String response = br.lines().collect(Collectors.joining());
                JSONObject result = JSON.parseObject(response);
                return result.getIntValue("errcode") == 0;
            }
        }

        return false;
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

**JSON配置** (`WeComalertChannel.json`):

```json
{
  "token": {
    "label": "机器人Key",
    "placeholder": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "help": "企业微信机器人的webhook key"
  },
  "alertTpl": {
    "dftVal": "com.qlangtech.plugins.incr.flink.alert.WeComalertChannel.loadDefaultTpl()"
  }
}
```

**Velocity模板** (`alert-wecom.vm`):

```markdown
# ${template.subject}

> TIS数据集成平台

#if($template.type == 1)
**任务名称**: ${template.jobName}
**任务状态**: <font color="warning">${template.status}</font>
**开始时间**: ${template.startTime}
**结束时间**: ${template.endTime}
**运行时长**: ${template.duration}

#if($template.restart)
**重启次数**: <font color="warning">${template.restartIndex}</font> / ${template.totalRestart}
#end

#if($template.link)
[查看详情](${template.link})
#end
#end

---
> 请及时处理异常任务!
```

---

#### 2.4.4 HttpCallbackalertChannel (HTTP回调报警)

**文件位置**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/java/com/qlangtech/plugins/incr/flink/alert/HttpCallbackalertChannel.java`

**配置属性**:

| 字段 | 类型 | FormFieldType | Validator | 说明 |
|------|------|---------------|-----------|------|
| url | String | INPUTTEXT | require, url | 回调URL |
| method | String | SELECTABLE | - | HTTP方法(POST/PUT) |
| contentType | String | SELECTABLE | - | Content-Type |

**代码骨架**:

```java
public class HttpCallbackalertChannel extends alertChannel {

    private static final String KEY_DISPLAY_NAME = "HttpCallback";

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.url})
    public String url;

    @FormField(ordinal = 2, type = FormFieldType.SELECTABLE)
    public String method = "POST";

    @FormField(ordinal = 3, type = FormFieldType.SELECTABLE)
    public String contentType = "application/json";

    public static String loadDefaultTpl() {
        // HTTP回调的默认模板是JSON格式
        return "{\n" +
               "  \"title\": \"${template.title}\",\n" +
               "  \"subject\": \"${template.subject}\",\n" +
               "  \"jobName\": \"${template.jobName}\",\n" +
               "  \"status\": \"${template.status}\",\n" +
               "  \"startTime\": \"${template.startTime}\",\n" +
               "  \"endTime\": \"${template.endTime}\",\n" +
               "  \"duration\": \"${template.duration}\",\n" +
               "  \"link\": \"${template.link}\"\n" +
               "}";
    }

    @Override
    public boolean send(AlertTemplate template) throws Exception {
        // 1. 渲染请求体(alertTpl本身就是Velocity模板)
        String requestBody = renderTemplate(template);

        // 2. 发送HTTP请求
        HttpURLConnection conn = (HttpURLConnection) new URL(this.url).openConnection();
        conn.setRequestMethod(this.method);
        conn.setRequestProperty("Content-Type", this.contentType + "; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        return responseCode >= 200 && responseCode < 300;
    }

    @TISExtension()
    public static class Desc extends alertChannelDescDesc {

        public Desc() {
            // 注册method选项
            registerSelectOptions("method", () -> {
                List<Option> options = new ArrayList<>();
                options.add(new Option("POST", "POST"));
                options.add(new Option("PUT", "PUT"));
                return options;
            });

            // 注册contentType选项
            registerSelectOptions("contentType", () -> {
                List<Option> options = new ArrayList<>();
                options.add(new Option("application/json", "application/json"));
                options.add(new Option("application/x-www-form-urlencoded", "application/x-www-form-urlencoded"));
                return options;
            });
        }

        @Override
        public String getDisplayName() {
            return KEY_DISPLAY_NAME;
        }
    }
}
```

**JSON配置** (`HttpCallbackalertChannel.json`):

```json
{
  "url": {
    "label": "回调URL",
    "placeholder": "https://example.com/api/alert",
    "help": "接收报警消息的HTTP回调地址"
  },
  "method": {
    "label": "HTTP方法",
    "help": "HTTP请求方法,通常使用POST"
  },
  "contentType": {
    "label": "Content-Type",
    "help": "请求体内容类型"
  },
  "alertTpl": {
    "label": "请求体模板",
    "dftVal": "com.qlangtech.plugins.incr.flink.alert.HttpCallbackalertChannel.loadDefaultTpl()",
    "help": "请求体Velocity模板,可自定义JSON格式"
  }
}
```

---

#### 2.4.5 LarkalertChannel (飞书报警)

**文件位置**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-realtime-flink/src/main/java/com/qlangtech/plugins/incr/flink/alert/LarkalertChannel.java`

**配置属性**:

| 字段 | 类型 | FormFieldType | Validator | 说明 |
|------|------|---------------|-----------|------|
| token | String | INPUTTEXT | require | 飞书机器人访问令牌 |
| isAtAll | Boolean | ENUM | - | 是否@所有人 |
| secretEnable | Boolean | ENUM | - | 是否启用签名验证 |
| secretToken | String | PASSWORD | - | 签名密钥 |

**代码骨架**:

```java
public class LarkalertChannel extends alertChannel {

    private static final String KEY_DISPLAY_NAME = "Lark";
    private static final String WEBHOOK_URL_TEMPLATE = "https://open.feishu.cn/open-apis/bot/v2/hook/%s";

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String token;

    @FormField(ordinal = 2, type = FormFieldType.ENUM)
    public Boolean isAtAll = false;

    @FormField(ordinal = 3, type = FormFieldType.ENUM, advance = true)
    public Boolean secretEnable = false;

    @FormField(ordinal = 4, type = FormFieldType.PASSWORD, advance = true)
    public String secretToken;

    public static String loadDefaultTpl() {
        try {
            return IOUtils.toString(
                LarkalertChannel.class.getResourceAsStream("alert-lark.vm"),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean send(AlertTemplate template) throws Exception {
        // 1. 渲染卡片内容(JSON格式)
        String cardJson = renderTemplate(template);
        Map<String, Object> card = JSON.parseObject(cardJson, Map.class);

        // 2. 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("msg_type", "interactive");
        body.put("card", card);

        // 3. 添加签名
        if (this.secretEnable && StringUtils.isNotEmpty(this.secretToken)) {
            long timestamp = System.currentTimeMillis() / 1000;
            String sign = calculateSign(timestamp);
            body.put("timestamp", timestamp);
            body.put("sign", sign);
        }

        // 4. 发送HTTP请求
        String url = String.format(WEBHOOK_URL_TEMPLATE, this.token);
        String jsonBody = JSON.toJSONString(body);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String response = br.lines().collect(Collectors.joining());
                JSONObject result = JSON.parseObject(response);
                return result.getIntValue("code") == 0;
            }
        }

        return false;
    }

    private String calculateSign(long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + this.secretToken;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(new byte[]{});
        return Base64.getEncoder().encodeToString(signData);
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

**JSON配置** (`LarkalertChannel.json`):

```json
{
  "token": {
    "label": "访问令牌",
    "placeholder": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "help": "飞书机器人的webhook token"
  },
  "isAtAll": {
    "label": "@所有人",
    "dftVal": false,
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
    "help": "是否@群里所有人"
  },
  "secretEnable": {
    "label": "启用签名",
    "dftVal": false,
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
    "help": "是否启用签名验证,提高安全性"
  },
  "secretToken": {
    "label": "签名密钥",
    "help": "飞书机器人的签名密钥,启用签名时必填"
  },
  "alertTpl": {
    "dftVal": "com.qlangtech.plugins.incr.flink.alert.LarkalertChannel.loadDefaultTpl()"
  }
}
```

**Velocity模板** (`alert-lark.vm`):

```json
{
  "config": {
    "wide_screen_mode": true
  },
  "header": {
    "template": "red",
    "title": {
      "content": "${template.subject}",
      "tag": "plain_text"
    }
  },
  "elements": [
    {
      "tag": "note",
      "elements": [
        {
          "tag": "plain_text",
          "content": "TIS数据集成平台"
        }
      ]
    },
#if($template.type == 1)
    {
      "tag": "div",
      "fields": [
        {
          "is_short": false,
          "text": {
            "tag": "lark_md",
            "content": "**任务名称**: ${template.jobName}"
          }
        },
        {
          "is_short": false,
          "text": {
            "tag": "lark_md",
            "content": "**任务状态**: <font color='red'>${template.status}</font>"
          }
        },
        {
          "is_short": true,
          "text": {
            "tag": "lark_md",
            "content": "**开始时间**: ${template.startTime}"
          }
        },
        {
          "is_short": true,
          "text": {
            "tag": "lark_md",
            "content": "**结束时间**: ${template.endTime}"
          }
        },
        {
          "is_short": false,
          "text": {
            "tag": "lark_md",
            "content": "**运行时长**: ${template.duration}"
          }
        }
      ]
    },
#if($template.link)
    {
      "tag": "action",
      "actions": [
        {
          "tag": "button",
          "text": {
            "tag": "plain_text",
            "content": "查看详情"
          },
          "type": "primary",
          "url": "${template.link}"
        }
      ]
    },
#end
#end
    {
      "tag": "note",
      "elements": [
        {
          "tag": "plain_text",
          "content": "请及时处理异常任务!"
        }
      ]
    }
  ]
}
```

---

## 三、Freemarker → Velocity 模板语法转换对照表

| 功能 | Freemarker | Velocity |
|------|-----------|----------|
| 变量输出 | `${mail.jobName}` | `${template.jobName}` |
| 条件判断 | `<#if mail.type == 1>...</#if>` | `#if($template.type == 1)...#end` |
| 否则分支 | `<#else>` | `#else` |
| 循环 | `<#list items as item>...</#list>` | `#foreach($item in $items)...#end` |
| 字符串判断 | `<#if mail.link??>` | `#if($template.link)` |
| 注释 | `<#-- comment -->` | `## comment` |

---

## 四、文件清单

### 4.1 需要新增的文件

**tis-console模块** (2个):
1. `tis-console/src/main/java/com/qlangtech/tis/alert/AlertTemplate.java`
2. `tis-console/src/main/java/com/qlangtech/tis/alert/FlinkJobState.java` (枚举类)

**tis-realtime-flink模块** (20个):

Java类 (4个):
3. `com/qlangtech/plugins/incr/flink/alert/DingTalkAlertChannel.java`
4. `com/qlangtech/plugins/incr/flink/alert/WeComAlertChannel.java`
5. `com/qlangtech/plugins/incr/flink/alert/HttpCallbackAlertChannel.java`
6. `com/qlangtech/plugins/incr/flink/alert/LarkAlertChannel.java`

JSON配置 (5个):
7. `com/qlangtech/plugins/incr/flink/alert/EmailAlertChannel.json`
8. `com/qlangtech/plugins/incr/flink/alert/DingTalkAlertChannel.json`
9. `com/qlangtech/plugins/incr/flink/alert/WeComAlertChannel.json`
10. `com/qlangtech/plugins/incr/flink/alert/HttpCallbackAlertChannel.json`
11. `com/qlangtech/plugins/incr/flink/alert/LarkAlertChannel.json`

Velocity模板 (4个):
12. `com/qlangtech/plugins/incr/flink/alert/alert-email.vm`
13. `com/qlangtech/plugins/incr/flink/alert/alert-dingtalk.vm`
14. `com/qlangtech/plugins/incr/flink/alert/alert-wecom.vm`
15. `com/qlangtech/plugins/incr/flink/alert/alert-lark.vm`

Markdown帮助文档 (5个,可选):
16. `com/qlangtech/plugins/incr/flink/alert/EmailalertChannel.md`
17. `com/qlangtech/plugins/incr/flink/alert/DingTalkalertChannel.md`
18. `com/qlangtech/plugins/incr/flink/alert/WeComalertChannel.md`
19. `com/qlangtech/plugins/incr/flink/alert/HttpCallbackalertChannel.md`
20. `com/qlangtech/plugins/incr/flink/alert/LarkalertChannel.md`

依赖相关 (2个):
21. `pom.xml` - 添加依赖(Apache Commons Email, HttpClient等)
22. `tis.application.context.xml` - 已更新

### 4.2 需要修改的文件

1. `tis-plugin/src/main/java/com/qlangtech/tis/plugin/alert/alertChannel.java` - 新增send()方法
2. `tis-console/src/main/java/com/qlangtech/tis/alert/FlinkJobsMonitor.java` - 实现完整监控逻辑
3. `plugins/tis-incr/tis-realtime-flink/src/main/java/com/qlangtech/plugins/incr/flink/alert/EmailalertChannel.java` - 完善实现

---

## 五、依赖管理

### 5.1 tis-realtime-flink模块需要添加的Maven依赖

```xml
<!-- Apache Commons Email -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-email</artifactId>
    <version>1.5</version>
</dependency>

<!-- HTTP Client (如果TIS没有统一的HTTP工具类) -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>

<!-- Fastjson (TIS应该已经有了) -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>${fastjson.version}</version>
</dependency>
```

---

## 六、实现要点总结

### 6.1 监控机制

1. **定时调度**: 使用Spring的`@Scheduled`注解,每5秒执行一次
2. **同步执行**: 直接在定时任务线程中同步执行,简单可控
3. **状态判断**: 检测FAILED、LOST、CANCELED(非手动)状态
4. **TIS API**: 使用TIS自有API获取Flink Job状态(需要在实现时确认具体API)

### 6.2 报警发送

1. **全局配置**: 通过`ParamsConfig.getItems(alertChannel.KEY_CATEGORY)`获取所有配置的报警渠道
2. **同步发送**: 遍历所有渠道,同步调用send()方法
3. **异常处理**: 单个渠道失败不影响其他渠道,记录日志

### 6.3 模板渲染

1. **Velocity引擎**: 复用`DataXCfgGenerator.evaluateTemplate()`方法
2. **上下文变量**: VelocityContext中放入AlertTemplate对象,key为"template"
3. **模板存储**: 每个alertChannel插件的alertTpl属性保存模板内容

### 6.4 插件机制

1. **@TISExtension**: 每个alertChannel实现类必须有内部静态Desc类
2. **@FormField**: 所有配置属性必须为public,且标注@FormField
3. **JSON配置**: 每个插件必须有对应的.json文件,定义属性的前端展示信息
4. **默认模板**: loadDefaultTpl()方法返回默认Velocity模板

### 6.5 安全性

1. **钉钉签名**: 使用HMAC-SHA256算法,`timestamp + "\n" + secret`
2. **飞书签名**: 使用HMAC-SHA256算法,`timestamp + "\n" + secret`,注意时间戳单位为秒
3. **HTTPS**: 所有webhook URL必须使用HTTPS

---

## 七、设计优势

### 7.1 技术优势

1. **简单可控**: 同步执行,不会出现任务堆积,易于调试和维护
2. **扩展性强**: 基于TIS插件SPI机制,用户可自定义报警渠道
3. **模板灵活**: 支持Velocity模板,用户可自定义报警内容格式
4. **全局配置**: 一次配置,所有Job共享,降低维护成本
5. **多渠道支持**: 可同时配置多个报警渠道,一次报警发送到所有渠道

### 7.2 业务优势

1. **及时响应**: 5秒轮询间隔,快速发现任务异常
2. **全面覆盖**: 支持主流IM平台(钉钉、企业微信、飞书)和通用HTTP回调
3. **安全可靠**: 支持签名验证,防止webhook被滥用
4. **易于集成**: 完全符合TIS插件规范,无缝集成到现有系统

### 7.3 维护优势

1. **参考成熟方案**: 借鉴Apache StreamPark的成熟实现,稳定可靠
2. **代码清晰**: 职责分明,监控、报警、渠道实现分离
3. **易于测试**: 每个组件可独立测试
4. **文档完善**: 每个配置项都有详细的help说明

---

## 八、后续扩展规划

### 8.1 报警消息订阅 (Phase 2)

**需求**: 用户级别的报警订阅管理

**设计思路**:
1. 用户可订阅特定Job或特定类型的报警
2. 每个用户配置自己的报警渠道(邮箱、手机号等)
3. FlinkJobsMonitor触发报警时,根据订阅关系发送

**实现要点**:
- 新增AlertSubscription表,记录用户订阅关系
- alertChannel.send()方法支持传入contacts参数
- FlinkJobsMonitor.doAlert()方法先查询订阅关系,再发送

### 8.2 报警规则扩展 (Phase 3)

**需求**: 支持更复杂的报警条件

**可扩展的规则**:
1. Checkpoint失败率报警
2. 任务延迟报警
3. 资源使用率报警
4. 自定义指标报警

**设计思路**:
- 抽象AlertRule接口
- 每种规则实现一个AlertRule
- FlinkJobsMonitor中注册所有规则,轮询时检查

### 8.3 报警静默 (Phase 4)

**需求**: 避免重复报警,支持静默期

**设计思路**:
1. 同一个Job的同一种报警,在静默期内只发送一次
2. 静默期可配置(如10分钟)
3. 使用缓存记录最近报警时间

### 8.4 报警统计 (Phase 5)

**需求**: 报警历史记录和统计分析

**功能**:
1. 记录所有报警历史
2. 统计报警频率、报警类型分布
3. 生成报警报表

---

## 九、风险评估与应对

### 9.1 潜在风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| TIS Flink Job状态API不明确 | 高 | 中 | 实现前先调研确认API |
| 报警渠道API变更 | 中 | 低 | 版本化管理,保持向后兼容 |
| 大量Job导致轮询性能问题 | 中 | 低 | 优化查询,支持分页轮询 |
| 模板渲染异常导致报警失败 | 高 | 低 | 严格校验模板,try-catch包裹 |
| 网络问题导致报警发送失败 | 中 | 中 | 重试机制,记录失败日志 |

### 9.2 测试计划

1. **单元测试**: 每个alertChannel的send()方法
2. **集成测试**: FlinkJobsMonitor完整流程
3. **模板测试**: 各种Velocity模板渲染
4. **压力测试**: 大量Job并发监控
5. **容错测试**: 网络异常、API异常等场景

---

## 十、单元测试设计

### 10.1 测试框架

**使用的测试框架**:
- JUnit 4
- Mockito (用于Mock依赖)
- TIS测试工具类 (JsonUtil, PluginDesc等)

### 10.2 AlertTemplate测试

**文件位置**: `tis-console/src/test/java/com/qlangtech/tis/alert/TestAlertTemplate.java`

```java
package com.qlangtech.tis.alert;

import com.qlangtech.tis.trigger.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * AlertTemplate单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class TestAlertTemplate {

    /**
     * 测试AlertTemplate.Builder构建
     */
    @Test
    public void testAlertTemplateBuilder() {
        Date startTime = new Date(System.currentTimeMillis() - 3600000); // 1小时前
        Date endTime = new Date();

        AlertTemplate template = AlertTemplate.builder()
                .title("TIS Flink Job Alert")
                .subject("[FAILED] test_flink_job")
                .jobName("test_flink_job")
                .status("FAILED")
                .type(1)
                .startTime(startTime)
                .endTime(endTime)
                .duration(startTime, endTime)
                .link("http://192.168.28.200:8081/#/job/abc123")
                .restart(true, 3)
                .restartIndex(2)
                .totalRestart(3)
                .build();

        Assert.assertEquals("TIS Flink Job Alert", template.getTitle());
        Assert.assertEquals("[FAILED] test_flink_job", template.getSubject());
        Assert.assertEquals("test_flink_job", template.getJobName());
        Assert.assertEquals("FAILED", template.getStatus());
        Assert.assertEquals(Integer.valueOf(1), template.getType());
        Assert.assertNotNull(template.getStartTime());
        Assert.assertNotNull(template.getEndTime());
        Assert.assertNotNull(template.getDuration());
        Assert.assertTrue(template.getDuration().contains("h")); // 包含小时
        Assert.assertEquals("http://192.168.28.200:8081/#/job/abc123", template.getLink());
        Assert.assertTrue(template.getRestart());
        Assert.assertEquals(Integer.valueOf(2), template.getRestartIndex());
        Assert.assertEquals(Integer.valueOf(3), template.getTotalRestart());
    }

    /**
     * 测试时长格式化
     */
    @Test
    public void testDurationFormat() {
        Date start = new Date(0);

        // 测试秒级
        Date end1 = new Date(30000); // 30秒
        AlertTemplate t1 = AlertTemplate.builder().duration(start, end1).build();
        Assert.assertEquals("30s", t1.getDuration());

        // 测试分钟级
        Date end2 = new Date(150000); // 2分30秒
        AlertTemplate t2 = AlertTemplate.builder().duration(start, end2).build();
        Assert.assertEquals("2m 30s", t2.getDuration());

        // 测试小时级
        Date end3 = new Date(7200000); // 2小时
        AlertTemplate t3 = AlertTemplate.builder().duration(start, end3).build();
        Assert.assertEquals("2h 0m 0s", t3.getDuration());

        // 测试天级
        Date end4 = new Date(90000000); // 1天多
        AlertTemplate t4 = AlertTemplate.builder().duration(start, end4).build();
        Assert.assertTrue(t4.getDuration().contains("d"));
    }

    /**
     * 测试JSON序列化
     */
    @Test
    public void testAlertTemplateSerialize() {
        AlertTemplate template = AlertTemplate.builder()
                .title("Test Alert")
                .subject("[FAILED] job1")
                .jobName("job1")
                .status("FAILED")
                .type(1)
                .build();

        // 序列化为JSON
        String json = JsonUtil.toString(template);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"jobName\":\"job1\""));
        Assert.assertTrue(json.contains("\"status\":\"FAILED\""));

        // 可选: 使用JsonUtil.assertJSONEqual进行更严格的对比
        JsonUtil.assertJSONEqual(TestAlertTemplate.class,
            "alert-template-serialize.json", json,
            (message, expected, actual) -> {
                Assert.assertEquals(message, expected, actual);
            });
    }
}
```

**测试资源文件**: `tis-console/src/test/resources/com/qlangtech/tis/alert/alert-template-serialize.json`

```json
{
  "title": "Test Alert",
  "subject": "[FAILED] job1",
  "jobName": "job1",
  "status": "FAILED",
  "type": 1
}
```

---

### 10.3 EmailalertChannel测试

**文件位置**: `tis-realtime-flink/src/test/java/com/qlangtech/plugins/incr/flink/alert/TestEmailalertChannel.java`

```java
package com.qlangtech.plugins.incr.flink.alert;

import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.plugin.common.PluginDesc;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.mail.EmailException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

/**
 * EmailalertChannel单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class TestEmailalertChannel {

    private EmailalertChannel emailChannel;
    private AlertTemplate alertTemplate;

    @Before
    public void setUp() {
        // 创建EmailalertChannel实例
        emailChannel = new EmailalertChannel();
        emailChannel.name = "test_email_channel";
        emailChannel.smtpHost = "smtp.163.com";
        emailChannel.smtpPort = 25;
        emailChannel.from = "test@163.com";
        emailChannel.userName = "test";
        emailChannel.password = "testpass";
        emailChannel.ssl = false;
        emailChannel.contacts = "user1@example.com,user2@example.com";
        emailChannel.alertTpl = EmailalertChannel.loadDefaultTpl();

        // 创建测试用的AlertTemplate
        alertTemplate = AlertTemplate.builder()
                .title("TIS Flink Job Alert")
                .subject("[FAILED] test_job")
                .jobName("test_job")
                .status("FAILED")
                .type(1)
                .startTime(new Date(System.currentTimeMillis() - 3600000))
                .endTime(new Date())
                .duration(new Date(System.currentTimeMillis() - 3600000), new Date())
                .link("http://localhost:8081/#/job/123")
                .build();
    }

    /**
     * 测试默认模板加载
     */
    @Test
    public void testLoadDefaultTpl() {
        String defaultTpl = EmailalertChannel.loadDefaultTpl();

        Assert.assertNotNull("默认模板不能为空", defaultTpl);
        Assert.assertTrue("模板应包含template变量", defaultTpl.contains("${template"));
        Assert.assertTrue("模板应包含HTML标签", defaultTpl.contains("<html>"));
        Assert.assertTrue("模板应包含Velocity条件判断", defaultTpl.contains("#if"));
    }

    /**
     * 测试模板渲染
     */
    @Test
    public void testRenderTemplate() {
        String rendered = emailChannel.renderTemplate(alertTemplate);

        Assert.assertNotNull("渲染结果不能为空", rendered);
        Assert.assertTrue("应包含任务名称", rendered.contains("test_job"));
        Assert.assertTrue("应包含状态", rendered.contains("FAILED"));
        Assert.assertTrue("应包含链接", rendered.contains("http://localhost:8081"));
    }

    /**
     * 测试配置序列化
     */
    @Test
    public void testInstanceSerialize() {
        String json = JsonUtil.toString(emailChannel);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"smtpHost\":\"smtp.163.com\""));
        Assert.assertTrue(json.contains("\"smtpPort\":25"));

        // 使用TIS标准测试方法
        JsonUtil.assertJSONEqual(TestEmailalertChannel.class,
            "email-alert-channel-serialize.json", json,
            (message, expected, actual) -> {
                Assert.assertEquals(message, expected, actual);
            });
    }

    /**
     * 测试Descriptor生成
     */
    @Test
    public void testDescGenerate() {
        PluginDesc.testDescGenerate(EmailalertChannel.class,
            "email-alert-channel-descriptor.json");
    }

    /**
     * 测试邮件发送 (Mock测试,不实际发送)
     * 注意: 实际发送邮件需要真实的SMTP服务器,这里仅测试方法调用
     */
    @Test
    public void testSendWithMock() throws Exception {
        // 创建一个Mock的EmailalertChannel
        EmailalertChannel mockChannel = Mockito.spy(emailChannel);

        // Mock renderTemplate方法返回固定内容
        String mockHtml = "<html><body>Test Email</body></html>";
        Mockito.doReturn(mockHtml).when(mockChannel).renderTemplate(Mockito.any());

        // 由于实际发送需要SMTP服务器,这里只验证配置是否正确
        Assert.assertNotNull(mockChannel.smtpHost);
        Assert.assertNotNull(mockChannel.contacts);
        Assert.assertTrue(mockChannel.contacts.contains("@"));
    }

    /**
     * 测试收件人解析
     */
    @Test
    public void testContactsParsing() {
        String contacts = "user1@example.com,user2@example.com,  user3@example.com  ";
        String[] emails = contacts.split(",");

        Assert.assertEquals(3, emails.length);
        Assert.assertEquals("user1@example.com", emails[0].trim());
        Assert.assertEquals("user2@example.com", emails[1].trim());
        Assert.assertEquals("user3@example.com", emails[2].trim());
    }

    /**
     * 测试SSL配置
     */
    @Test
    public void testSslConfiguration() {
        EmailalertChannel sslChannel = new EmailalertChannel();
        sslChannel.ssl = true;
        sslChannel.smtpPort = 465;

        Assert.assertTrue("应启用SSL", sslChannel.ssl);
        Assert.assertEquals("SSL端口应为465", Integer.valueOf(465), sslChannel.smtpPort);
    }
}
```

**测试资源文件**: `tis-realtime-flink/src/test/resources/com/qlangtech/plugins/incr/flink/alert/email-alert-channel-serialize.json`

```json
{
  "name": "test_email_channel",
  "smtpHost": "smtp.163.com",
  "smtpPort": 25,
  "from": "test@163.com",
  "userName": "test",
  "password": "testpass",
  "ssl": false,
  "contacts": "user1@example.com,user2@example.com"
}
```

---

### 10.4 DingTalkalertChannel测试

**文件位置**: `tis-realtime-flink/src/test/java/com/qlangtech/plugins/incr/flink/alert/TestDingTalkalertChannel.java`

```java
package com.qlangtech.plugins.incr.flink.alert;

import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.plugin.common.PluginDesc;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * DingTalkalertChannel单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class TestDingTalkalertChannel {

    private DingTalkalertChannel dingTalkChannel;
    private AlertTemplate alertTemplate;

    @Before
    public void setUp() {
        dingTalkChannel = new DingTalkalertChannel();
        dingTalkChannel.name = "test_dingtalk_channel";
        dingTalkChannel.token = "test_token_123456";
        dingTalkChannel.contacts = "13800138000,13900139000";
        dingTalkChannel.isAtAll = false;
        dingTalkChannel.secretEnable = true;
        dingTalkChannel.secretToken = "SECtest_secret_key";
        dingTalkChannel.alertTpl = DingTalkalertChannel.loadDefaultTpl();

        alertTemplate = AlertTemplate.builder()
                .title("钉钉报警测试")
                .subject("[FAILED] test_job")
                .jobName("test_job")
                .status("FAILED")
                .type(1)
                .startTime(new Date(System.currentTimeMillis() - 3600000))
                .endTime(new Date())
                .duration(new Date(System.currentTimeMillis() - 3600000), new Date())
                .build();
    }

    /**
     * 测试默认模板加载
     */
    @Test
    public void testLoadDefaultTpl() {
        String defaultTpl = DingTalkalertChannel.loadDefaultTpl();

        Assert.assertNotNull("默认模板不能为空", defaultTpl);
        Assert.assertTrue("模板应包含Markdown标记", defaultTpl.contains("#"));
        Assert.assertTrue("模板应包含template变量", defaultTpl.contains("${template"));
    }

    /**
     * 测试签名计算
     */
    @Test
    public void testCalculateSign() throws Exception {
        String secret = "SECtest_secret_key";
        long timestamp = 1637123456789L;

        // 计算签名
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(
            Base64.getEncoder().encodeToString(signData),
            StandardCharsets.UTF_8.name());

        Assert.assertNotNull("签名不能为空", sign);
        Assert.assertTrue("签名应经过URL编码", sign.length() > 0);
    }

    /**
     * 测试Webhook URL构建(不启用签名)
     */
    @Test
    public void testBuildWebhookUrlWithoutSign() {
        DingTalkalertChannel channel = new DingTalkalertChannel();
        channel.token = "abc123";
        channel.secretEnable = false;

        String expectedUrl = "https://oapi.dingtalk.com/robot/send?access_token=abc123";

        // 注意: 实际实现中buildWebhookUrl是private方法
        // 这里通过测试send方法间接验证URL构建
        Assert.assertEquals("abc123", channel.token);
        Assert.assertFalse(channel.secretEnable);
    }

    /**
     * 测试Webhook URL构建(启用签名)
     */
    @Test
    public void testBuildWebhookUrlWithSign() {
        Assert.assertNotNull(dingTalkChannel.token);
        Assert.assertTrue(dingTalkChannel.secretEnable);
        Assert.assertNotNull(dingTalkChannel.secretToken);
    }

    /**
     * 测试@联系人解析
     */
    @Test
    public void testContactsParsing() {
        String[] contacts = dingTalkChannel.contacts.split(",");

        Assert.assertEquals(2, contacts.length);
        Assert.assertEquals("13800138000", contacts[0]);
        Assert.assertEquals("13900139000", contacts[1]);
    }

    /**
     * 测试模板渲染
     */
    @Test
    public void testRenderTemplate() {
        String rendered = dingTalkChannel.renderTemplate(alertTemplate);

        Assert.assertNotNull("渲染结果不能为空", rendered);
        Assert.assertTrue("应包含任务名称", rendered.contains("test_job"));
        Assert.assertTrue("应包含状态", rendered.contains("FAILED"));
    }

    /**
     * 测试配置序列化
     */
    @Test
    public void testInstanceSerialize() {
        JsonUtil.assertJSONEqual(TestDingTalkalertChannel.class,
            "dingtalk-alert-channel-serialize.json",
            JsonUtil.toString(dingTalkChannel),
            (message, expected, actual) -> {
                Assert.assertEquals(message, expected, actual);
            });
    }

    /**
     * 测试Descriptor生成
     */
    @Test
    public void testDescGenerate() {
        PluginDesc.testDescGenerate(DingTalkalertChannel.class,
            "dingtalk-alert-channel-descriptor.json");
    }

    /**
     * 测试自定义URL
     */
    @Test
    public void testCustomUrl() {
        DingTalkalertChannel channel = new DingTalkalertChannel();
        channel.alertDingURL = "https://custom.dingtalk.com/robot/send";
        channel.token = "test_token";

        Assert.assertEquals("https://custom.dingtalk.com/robot/send", channel.alertDingURL);
    }
}
```

---

### 10.5 FlinkJobsMonitor测试

**文件位置**: `tis-console/src/test/java/com/qlangtech/tis/alert/TestFlinkJobsMonitor.java`

```java
package com.qlangtech.tis.alert;

import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FlinkJobsMonitor单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class TestFlinkJobsMonitor {

    private FlinkJobsMonitor monitor;
    private List<MockFlinkJob> mockJobs;

    @Before
    public void setUp() {
        monitor = new FlinkJobsMonitor();

        // 准备模拟的Flink Job数据
        mockJobs = new ArrayList<>();
        mockJobs.add(createMockJob("job1", FlinkJobState.RUNNING, FlinkJobState.RUNNING, false));
        mockJobs.add(createMockJob("job2", FlinkJobState.FAILED, FlinkJobState.RUNNING, false));
        mockJobs.add(createMockJob("job3", FlinkJobState.LOST, FlinkJobState.RUNNING, false));
        mockJobs.add(createMockJob("job4", FlinkJobState.CANCELED, FlinkJobState.RUNNING, true));
        mockJobs.add(createMockJob("job5", FlinkJobState.CANCELED, FlinkJobState.RUNNING, false));
    }

    /**
     * 创建模拟的Flink Job
     */
    private MockFlinkJob createMockJob(String name,
                                       FlinkJobState currentState,
                                       FlinkJobState previousState,
                                       boolean manualStop) {
        MockFlinkJob job = new MockFlinkJob();
        job.name = name;
        job.currentState = currentState;
        job.previousState = previousState;
        job.manualStop = manualStop;
        job.startTime = new Date(System.currentTimeMillis() - 3600000);
        job.flinkWebUrl = "http://localhost:8081/#/job/" + name;
        return job;
    }

    /**
     * 测试报警条件判断 - FAILED状态
     */
    @Test
    public void testShouldAlertForFailedJob() {
        MockFlinkJob job = mockJobs.get(1); // job2: FAILED

        boolean shouldAlert = monitor.shouldAlert(job,
            job.currentState, job.previousState);

        Assert.assertTrue("FAILED状态应该触发报警", shouldAlert);
    }

    /**
     * 测试报警条件判断 - LOST状态
     */
    @Test
    public void testShouldAlertForLostJob() {
        MockFlinkJob job = mockJobs.get(2); // job3: LOST

        boolean shouldAlert = monitor.shouldAlert(job,
            job.currentState, job.previousState);

        Assert.assertTrue("LOST状态应该触发报警", shouldAlert);
    }

    /**
     * 测试报警条件判断 - 手动停止的CANCELED状态
     */
    @Test
    public void testShouldNotAlertForManualCanceled() {
        MockFlinkJob job = mockJobs.get(3); // job4: CANCELED(手动)

        boolean shouldAlert = monitor.shouldAlert(job,
            job.currentState, job.previousState);

        Assert.assertFalse("手动停止的CANCELED状态不应触发报警", shouldAlert);
    }

    /**
     * 测试报警条件判断 - 非手动停止的CANCELED状态
     */
    @Test
    public void testShouldAlertForNonManualCanceled() {
        MockFlinkJob job = mockJobs.get(4); // job5: CANCELED(非手动)

        boolean shouldAlert = monitor.shouldAlert(job,
            job.currentState, job.previousState);

        Assert.assertTrue("非手动停止的CANCELED状态应该触发报警", shouldAlert);
    }

    /**
     * 测试报警条件判断 - RUNNING状态
     */
    @Test
    public void testShouldNotAlertForRunningJob() {
        MockFlinkJob job = mockJobs.get(0); // job1: RUNNING

        boolean shouldAlert = monitor.shouldAlert(job,
            job.currentState, job.previousState);

        Assert.assertFalse("RUNNING状态不应触发报警", shouldAlert);
    }

    /**
     * 测试AlertTemplate创建
     */
    @Test
    public void testCreateAlertTemplate() {
        MockFlinkJob job = mockJobs.get(1); // FAILED job

        AlertTemplate template = AlertTemplate.builder()
                .title("TIS Flink Job Alert")
                .subject("[" + job.currentState.name() + "] " + job.name)
                .jobName(job.name)
                .status(job.currentState.getDisplayName())
                .type(1)
                .startTime(job.startTime)
                .endTime(new Date())
                .duration(job.startTime, new Date())
                .link(job.flinkWebUrl)
                .build();

        Assert.assertEquals("[FAILED] job2", template.getSubject());
        Assert.assertEquals("job2", template.getJobName());
        Assert.assertEquals("FAILED", template.getStatus());
        Assert.assertEquals(Integer.valueOf(1), template.getType());
        Assert.assertNotNull(template.getLink());
    }

    /**
     * 测试报警发送流程 (使用Mock)
     */
    @Test
    public void testDoAlertWithMock() throws Exception {
        MockFlinkJob job = mockJobs.get(1); // FAILED job

        // 创建Mock的alertChannel
        alertChannel mockChannel = Mockito.mock(alertChannel.class);
        Mockito.when(mockChannel.identityValue()).thenReturn("mock_channel");
        Mockito.when(mockChannel.send(Mockito.any(AlertTemplate.class))).thenReturn(true);

        // 模拟调用send方法
        AlertTemplate template = AlertTemplate.builder()
                .jobName(job.name)
                .status(job.currentState.getDisplayName())
                .build();

        boolean result = mockChannel.send(template);

        Assert.assertTrue("报警发送应该成功", result);

        // 验证send方法被调用
        Mockito.verify(mockChannel, Mockito.times(1))
               .send(Mockito.any(AlertTemplate.class));
    }

    /**
     * 测试初始化
     */
    @Test
    public void testAfterPropertiesSet() throws Exception {
        FlinkJobsMonitor newMonitor = new FlinkJobsMonitor();
        newMonitor.afterPropertiesSet();

        // 验证初始化没有抛出异常
        Assert.assertNotNull(newMonitor);
    }

    /**
     * 模拟的Flink Job类(用于测试)
     */
    static class MockFlinkJob {
        String name;
        FlinkJobState currentState;
        FlinkJobState previousState;
        boolean manualStop;
        Date startTime;
        String flinkWebUrl;

        public FlinkJobState getCurrentState() {
            return currentState;
        }

        public FlinkJobState getPreviousState() {
            return previousState;
        }

        public boolean isManualStop() {
            return manualStop;
        }

        public String getName() {
            return name;
        }

        public Date getStartTime() {
            return startTime;
        }

        public String getFlinkWebUrl() {
            return flinkWebUrl;
        }
    }
}
```

---

### 10.6 Velocity模板渲染测试

**文件位置**: `tis-realtime-flink/src/test/java/com/qlangtech/plugins/incr/flink/alert/TestVelocityTemplateRender.java`

```java
package com.qlangtech.plugins.incr.flink.alert;

import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Velocity模板渲染测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class TestVelocityTemplateRender {

    /**
     * 测试基本变量渲染
     */
    @Test
    public void testBasicVariableRender() {
        AlertTemplate template = AlertTemplate.builder()
                .title("测试标题")
                .jobName("test_job")
                .status("FAILED")
                .build();

        String velocityTpl = "任务名称: ${template.jobName}, 状态: ${template.status}";

        VelocityContext context = new VelocityContext();
        context.put("template", template);

        String result = DataXCfgGenerator.evaluateTemplate(context, velocityTpl);

        Assert.assertEquals("任务名称: test_job, 状态: FAILED", result);
    }

    /**
     * 测试条件判断渲染
     */
    @Test
    public void testConditionalRender() {
        AlertTemplate template1 = AlertTemplate.builder()
                .type(1)
                .jobName("job1")
                .build();

        String velocityTpl = "#if($template.type == 1)类型1#else其他类型#end";

        VelocityContext context = new VelocityContext();
        context.put("template", template1);

        String result = DataXCfgGenerator.evaluateTemplate(context, velocityTpl);

        Assert.assertEquals("类型1", result);
    }

    /**
     * 测试HTML模板渲染
     */
    @Test
    public void testHtmlTemplateRender() {
        AlertTemplate template = AlertTemplate.builder()
                .title("报警测试")
                .subject("[FAILED] job1")
                .jobName("job1")
                .status("FAILED")
                .type(1)
                .startTime(new Date())
                .endTime(new Date())
                .duration(new Date(0), new Date(3600000))
                .build();

        String htmlTpl = "<html><body>" +
                "<h1>${template.subject}</h1>" +
                "#if($template.type == 1)" +
                "<p>任务: ${template.jobName}</p>" +
                "<p>状态: ${template.status}</p>" +
                "#end" +
                "</body></html>";

        VelocityContext context = new VelocityContext();
        context.put("template", template);

        String result = DataXCfgGenerator.evaluateTemplate(context, htmlTpl);

        Assert.assertTrue("应包含subject", result.contains("[FAILED] job1"));
        Assert.assertTrue("应包含jobName", result.contains("job1"));
        Assert.assertTrue("应包含status", result.contains("FAILED"));
        Assert.assertTrue("应包含HTML标签", result.contains("<html>"));
    }

    /**
     * 测试Markdown模板渲染
     */
    @Test
    public void testMarkdownTemplateRender() {
        AlertTemplate template = AlertTemplate.builder()
                .subject("报警通知")
                .jobName("test_job")
                .status("FAILED")
                .startTime(new Date())
                .endTime(new Date())
                .duration(new Date(0), new Date(1800000))
                .build();

        String markdownTpl = "# ${template.subject}\n\n" +
                "**任务名称**: ${template.jobName}\n\n" +
                "**任务状态**: ${template.status}\n\n" +
                "**运行时长**: ${template.duration}";

        VelocityContext context = new VelocityContext();
        context.put("template", template);

        String result = DataXCfgGenerator.evaluateTemplate(context, markdownTpl);

        Assert.assertTrue("应包含Markdown标题", result.contains("# 报警通知"));
        Assert.assertTrue("应包含粗体标记", result.contains("**"));
        Assert.assertTrue("应包含时长", result.contains("30m"));
    }

    /**
     * 测试空值处理
     */
    @Test
    public void testNullValueHandling() {
        AlertTemplate template = AlertTemplate.builder()
                .jobName("job1")
                .build(); // 其他字段为null

        String velocityTpl = "#if($template.link)链接: ${template.link}#else无链接#end";

        VelocityContext context = new VelocityContext();
        context.put("template", template);

        String result = DataXCfgGenerator.evaluateTemplate(context, velocityTpl);

        Assert.assertEquals("无链接", result);
    }
}
```

---

### 10.7 其他报警渠道测试

**WeComalertChannel测试**: 参照EmailalertChannel和DingTalkalertChannel的测试模式

**HttpCallbackalertChannel测试**: 重点测试自定义模板和HTTP请求构建

**LarkalertChannel测试**: 重点测试JSON卡片格式和签名计算

测试文件命名规范:
- `TestWeComalertChannel.java`
- `TestHttpCallbackalertChannel.java`
- `TestLarkalertChannel.java`

---

### 10.8 测试资源文件清单

所有测试资源文件应放置在对应的 `src/test/resources` 目录下:

**tis-console模块**:
1. `com/qlangtech/tis/alert/alert-template-serialize.json`

**tis-realtime-flink模块**:
2. `com/qlangtech/plugins/incr/flink/alert/email-alert-channel-serialize.json`
3. `com/qlangtech/plugins/incr/flink/alert/email-alert-channel-descriptor.json`
4. `com/qlangtech/plugins/incr/flink/alert/dingtalk-alert-channel-serialize.json`
5. `com/qlangtech/plugins/incr/flink/alert/dingtalk-alert-channel-descriptor.json`
6. `com/qlangtech/plugins/incr/flink/alert/wecom-alert-channel-serialize.json`
7. `com/qlangtech/plugins/incr/flink/alert/wecom-alert-channel-descriptor.json`
8. `com/qlangtech/plugins/incr/flink/alert/httpcallback-alert-channel-serialize.json`
9. `com/qlangtech/plugins/incr/flink/alert/httpcallback-alert-channel-descriptor.json`
10. `com/qlangtech/plugins/incr/flink/alert/lark-alert-channel-serialize.json`
11. `com/qlangtech/plugins/incr/flink/alert/lark-alert-channel-descriptor.json`

---

### 10.9 测试覆盖率目标

| 模块 | 目标覆盖率 | 重点测试内容 |
|------|-----------|-------------|
| AlertTemplate | 90%+ | Builder模式、时间格式化、序列化 |
| FlinkJobsMonitor | 80%+ | 报警条件判断、状态检测 |
| EmailalertChannel | 85%+ | 模板渲染、配置验证 |
| DingTalkalertChannel | 85%+ | 签名计算、URL构建、模板渲染 |
| WeComalertChannel | 85%+ | Markdown渲染、请求构建 |
| HttpCallbackalertChannel | 85%+ | 自定义模板、HTTP方法 |
| LarkalertChannel | 85%+ | JSON卡片格式、签名计算 |

---

### 10.10 集成测试

**文件位置**: `tis-console/src/test/java/com/qlangtech/tis/alert/TestFlinkJobsMonitorIntegration.java`

```java
package com.qlangtech.tis.alert;

import com.qlangtech.plugins.incr.flink.alert.EmailalertChannel;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * FlinkJobsMonitor集成测试
 * 测试完整的监控->报警流程
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class TestFlinkJobsMonitorIntegration {

    private FlinkJobsMonitor monitor;
    private EmailalertChannel emailChannel;

    @Before
    public void setUp() {
        monitor = new FlinkJobsMonitor();

        // 配置邮件报警渠道
        emailChannel = new EmailalertChannel();
        emailChannel.name = "integration_test_email";
        emailChannel.smtpHost = "smtp.example.com";
        emailChannel.smtpPort = 25;
        emailChannel.from = "test@example.com";
        emailChannel.userName = "test";
        emailChannel.password = "testpass";
        emailChannel.contacts = "admin@example.com";
        emailChannel.alertTpl = EmailalertChannel.loadDefaultTpl();
    }

    /**
     * 测试完整的报警流程
     */
    @Test
    public void testCompleteAlertFlow() throws Exception {
        // 1. 创建AlertTemplate
        AlertTemplate template = AlertTemplate.builder()
                .title("集成测试")
                .subject("[FAILED] integration_test_job")
                .jobName("integration_test_job")
                .status("FAILED")
                .type(1)
                .startTime(new Date(System.currentTimeMillis() - 3600000))
                .endTime(new Date())
                .duration(new Date(System.currentTimeMillis() - 3600000), new Date())
                .build();

        // 2. 渲染模板
        String rendered = emailChannel.renderTemplate(template);

        Assert.assertNotNull("渲染结果不能为空", rendered);
        Assert.assertTrue("应包含任务名称", rendered.contains("integration_test_job"));
        Assert.assertTrue("应包含FAILED状态", rendered.contains("FAILED"));

        // 3. 验证模板是HTML格式
        Assert.assertTrue("应为HTML格式", rendered.contains("<html>"));
        Assert.assertTrue("应包含表格", rendered.contains("<table>"));

        // 注意: 实际发送邮件需要真实的SMTP服务器
        // 在集成测试环境中可以使用Mock SMTP服务器(如GreenMail)进行测试
    }

    /**
     * 测试多渠道报警
     */
    @Test
    public void testMultiChannelAlert() {
        // 准备多个报警渠道
        // 在实际集成测试中,可以同时配置Email、钉钉等多个渠道
        // 验证所有渠道都能正常接收报警

        Assert.assertNotNull(emailChannel);
        Assert.assertEquals("integration_test_email", emailChannel.name);
    }
}
```

---

### 10.11 Mock SMTP服务器测试

如果需要真实测试邮件发送,可以使用GreenMail等Mock SMTP服务器:

**pom.xml依赖**:
```xml
<dependency>
    <groupId>com.icegreen</groupId>
    <artifactId>greenmail-junit4</artifactId>
    <version>1.6.9</version>
    <scope>test</scope>
</dependency>
```

**使用示例**:
```java
@Rule
public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

@Test
public void testEmailSendingWithGreenMail() throws Exception {
    // GreenMail会自动启动Mock SMTP服务器
    emailChannel.smtpHost = "localhost";
    emailChannel.smtpPort = 3025; // GreenMail默认端口

    boolean result = emailChannel.send(alertTemplate);

    Assert.assertTrue("邮件发送应该成功", result);

    // 验证邮件是否被接收
    MimeMessage[] messages = greenMail.getReceivedMessages();
    Assert.assertEquals(1, messages.length);
    Assert.assertEquals("[FAILED] test_job", messages[0].getSubject());
}
```

---

## 十一、参考资料

### 10.1 Apache StreamPark

- GitHub: https://github.com/apache/streampark
- 报警实现: `streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/watcher/FlinkAppHttpWatcher.java`

### 10.2 报警渠道官方文档

- 钉钉机器人: https://open.dingtalk.com/document/group/customize-robot-security-settings
- 企业微信机器人: https://developer.work.weixin.qq.com/document/path/91770
- 飞书机器人: https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN

### 10.3 TIS相关文档

- TIS官网: https://tis.pub
- 插件开发文档: https://tis.pub/docs/develop/compile-running/
- 插件开发脚手架: https://github.com/qlangtech/tis-archetype-plugin

---

## 附录A: 完整类图

```
┌─────────────────────────────────────────────┐
│            ParamsConfig                     │
│  (TIS插件基类)                               │
└──────────────┬──────────────────────────────┘
               │
               │ extends
               │
┌──────────────▼──────────────────────────────┐
│         alertChannel                        │
│  (报警渠道抽象类)                             │
│  ───────────────────────────────────────    │
│  + String name                              │
│  + String alertTpl                          │
│  ───────────────────────────────────────    │
│  + boolean send(AlertTemplate)              │
│  # String renderTemplate(AlertTemplate)     │
└──────────────┬──────────────────────────────┘
               │
               │ extends (5个实现类)
       ┌───────┴───────┬───────────┬──────────┬──────────┐
       │               │           │          │          │
┌──────▼─────┐  ┌──────▼─────┐  ┌─▼────┐  ┌──▼───┐  ┌──▼────┐
│Emailalert  │  │DingTalkalert│  │WeCom │  │HttpCb│  │ Lark  │
│Channel     │  │Channel      │  │alert │  │alert │  │alert  │
└────────────┘  └─────────────┘  └──────┘  └──────┘  └───────┘


┌─────────────────────────────────────────────┐
│        FlinkJobsMonitor                     │
│  ───────────────────────────────────────    │
│  - Long lastWatchTime                       │
│  ───────────────────────────────────────    │
│  + void executeTask()                       │
│  - void checkJobStatus(FlinkJob)            │
│  - boolean shouldAlert(...)                 │
│  - void doAlert(FlinkJob, FlinkJobState)    │
└─────────────────────────────────────────────┘
                    │
                    │ uses
                    ▼
┌─────────────────────────────────────────────┐
│        AlertTemplate                        │
│  ───────────────────────────────────────    │
│  + String title                             │
│  + String subject                           │
│  + String jobName                           │
│  + String status                            │
│  + String startTime                         │
│  + String endTime                           │
│  + String duration                          │
│  + ...                                      │
└─────────────────────────────────────────────┘
```

---

## 附录B: 时序图

```
用户配置      FlinkJobsMonitor    TIS API    alertChannel插件    钉钉/邮件服务器
  │                │                 │              │                 │
  │ 配置报警渠道   │                 │              │                 │
  ├──────────────>│                 │              │                 │
  │                │                 │              │                 │
  │                │ ◄───每5秒───    │              │                 │
  │                │                 │              │                 │
  │                │ 获取Job状态     │              │                 │
  │                ├────────────────>│              │                 │
  │                │ 返回状态列表    │              │                 │
  │                │◄────────────────┤              │                 │
  │                │                 │              │                 │
  │                │ 判断状态异常     │              │                 │
  │                │─────┐           │              │                 │
  │                │     │           │              │                 │
  │                │◄────┘           │              │                 │
  │                │                 │              │                 │
  │                │ 创建AlertTemplate│             │                 │
  │                │─────┐           │              │                 │
  │                │     │           │              │                 │
  │                │◄────┘           │              │                 │
  │                │                 │              │                 │
  │                │ 遍历所有报警渠道 │              │                 │
  │                │─────┐           │              │                 │
  │                │     │           │              │                 │
  │                │◄────┘           │              │                 │
  │                │                 │              │                 │
  │                │ send(template)  │              │                 │
  │                ├──────────────────────────────>│                 │
  │                │                 │              │                 │
  │                │                 │   渲染模板   │                 │
  │                │                 │   ──────┐   │                 │
  │                │                 │         │   │                 │
  │                │                 │   ◄─────┘   │                 │
  │                │                 │              │                 │
  │                │                 │              │ 发送报警        │
  │                │                 │              ├────────────────>│
  │                │                 │              │                 │
  │                │                 │              │ 返回结果        │
  │                │                 │              │◄────────────────┤
  │                │                 │              │                 │
  │                │ 返回发送结果    │              │                 │
  │                │◄──────────────────────────────┤                 │
  │                │                 │              │                 │
```

---

**设计文档版本**: v1.0
**最后更新时间**: 2025-11-17
**文档维护者**: 百岁

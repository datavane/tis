/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.alert;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiConsumer;

/**
 * 报警消息数据模型
 * 参照StreamPark的AlertTemplate设计
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class AlertTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建一个用于测试报警的默认实例
     * 模拟Flink任务失败场景
     */
    public static AlertTemplate createDefault() {
        Date now = new Date();
        Date startTime = new Date(now.getTime() - 3600000); // 1小时前启动

        return AlertTemplate.builder()
                .title("【TIS报警测试】Flink任务状态异常")
                .subject("数据同步任务执行失败告警[测试]")
                .jobName("mysql-to-doris-sync-job")
                .status("FAILED")
                .type(1) // 任务状态报警
                .startTime(startTime)
                .endTime(now)
                .duration(startTime, now)
                .link("http://localhost:8081/#/job/running")
                .cpFailureRateInterval("5min")
                .cpMaxFailureInterval(3)
                .restart(true, 3)
                .restartIndex(2)
                .totalRestart(3)
                .atAll(false)
                .allJobs(10)
                .affectedJobs(1)
                .failedJobs(1)
                .lostJobs(0)
                .cancelledJobs(0)
                .probeJobs(10)
                .user("admin")
                .build();
    }

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

    // 无参构造函数
    public AlertTemplate() {
    }

    // Builder模式
    public static Builder builder() {
        return new Builder();
    }

    public void visitAllProp(BiConsumer<String, Object> propConsumer) {
        // 将AlertTemplate的所有字段添加到上下文中
        // 使用反射获取所有字段值
        java.lang.reflect.Field[] fields = AlertTemplate.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null) {
                    propConsumer.accept(field.getName(), value);
//                    context.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }
    }

    public static class Builder {
        private AlertTemplate template = new AlertTemplate();

        public Builder title(String title) {
            template.title = title;
            return this;
        }

        public Builder subject(String subject) {
            template.subject = subject;
            return this;
        }

        public Builder jobName(String jobName) {
            template.jobName = jobName;
            return this;
        }

        public Builder status(String status) {
            template.status = status;
            return this;
        }

        public Builder type(Integer type) {
            template.type = type;
            return this;
        }

        public Builder startTime(Date startTime) {
            template.startTime = formatDate(startTime);
            return this;
        }

        public Builder endTime(Date endTime) {
            template.endTime = formatDate(endTime == null ? new Date() : endTime);
            return this;
        }

        public Builder duration(Date start, Date end) {
            long durationMillis;
            if (start == null) {
                durationMillis = 0L;
            } else if (end == null) {
                durationMillis = System.currentTimeMillis() - start.getTime();
            } else {
                durationMillis = end.getTime() - start.getTime();
            }
            template.duration = formatDuration(durationMillis);
            return this;
        }

        public Builder link(String link) {
            template.link = link;
            return this;
        }

        public Builder cpFailureRateInterval(String cpFailureRateInterval) {
            template.cpFailureRateInterval = cpFailureRateInterval;
            return this;
        }

        public Builder cpMaxFailureInterval(Integer cpMaxFailureInterval) {
            template.cpMaxFailureInterval = cpMaxFailureInterval;
            return this;
        }

        public Builder restart(Boolean restart, Integer totalRestart) {
            template.restart = restart && totalRestart != null && totalRestart > 0;
            template.totalRestart = totalRestart;
            return this;
        }

        public Builder restartIndex(Integer restartIndex) {
            template.restartIndex = restartIndex;
            return this;
        }

        public Builder totalRestart(Integer totalRestart) {
            template.totalRestart = totalRestart;
            return this;
        }

        public Builder atAll(boolean atAll) {
            template.atAll = atAll;
            return this;
        }

        public Builder allJobs(Integer allJobs) {
            template.allJobs = allJobs;
            return this;
        }

        public Builder affectedJobs(Integer affectedJobs) {
            template.affectedJobs = affectedJobs;
            return this;
        }

        public Builder user(String user) {
            template.user = user;
            return this;
        }

        public Builder probeJobs(Integer probeJobs) {
            template.probeJobs = probeJobs;
            return this;
        }

        public Builder failedJobs(Integer failedJobs) {
            template.failedJobs = failedJobs;
            return this;
        }

        public Builder lostJobs(Integer lostJobs) {
            template.lostJobs = lostJobs;
            return this;
        }

        public Builder cancelledJobs(Integer cancelledJobs) {
            template.cancelledJobs = cancelledJobs;
            return this;
        }

        public AlertTemplate build() {
            return template;
        }

        private String formatDate(Date date) {
            if (date == null) {
                return null;
            }
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

    // Getter and Setter methods
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCpFailureRateInterval() {
        return cpFailureRateInterval;
    }

    public void setCpFailureRateInterval(String cpFailureRateInterval) {
        this.cpFailureRateInterval = cpFailureRateInterval;
    }

    public Integer getCpMaxFailureInterval() {
        return cpMaxFailureInterval;
    }

    public void setCpMaxFailureInterval(Integer cpMaxFailureInterval) {
        this.cpMaxFailureInterval = cpMaxFailureInterval;
    }

    public Boolean getRestart() {
        return restart;
    }

    public void setRestart(Boolean restart) {
        this.restart = restart;
    }

    public Integer getRestartIndex() {
        return restartIndex;
    }

    public void setRestartIndex(Integer restartIndex) {
        this.restartIndex = restartIndex;
    }

    public Integer getTotalRestart() {
        return totalRestart;
    }

    public void setTotalRestart(Integer totalRestart) {
        this.totalRestart = totalRestart;
    }

    public boolean isAtAll() {
        return atAll;
    }

    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    public Integer getAllJobs() {
        return allJobs;
    }

    public void setAllJobs(Integer allJobs) {
        this.allJobs = allJobs;
    }

    public Integer getAffectedJobs() {
        return affectedJobs;
    }

    public void setAffectedJobs(Integer affectedJobs) {
        this.affectedJobs = affectedJobs;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getProbeJobs() {
        return probeJobs;
    }

    public void setProbeJobs(Integer probeJobs) {
        this.probeJobs = probeJobs;
    }

    public Integer getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(Integer failedJobs) {
        this.failedJobs = failedJobs;
    }

    public Integer getLostJobs() {
        return lostJobs;
    }

    public void setLostJobs(Integer lostJobs) {
        this.lostJobs = lostJobs;
    }

    public Integer getCancelledJobs() {
        return cancelledJobs;
    }

    public void setCancelledJobs(Integer cancelledJobs) {
        this.cancelledJobs = cancelledJobs;
    }
}
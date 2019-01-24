/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.common.trigger;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.manage.common.trigger.sources.ODPSTaskConfig;
import com.qlangtech.tis.manage.common.trigger.sources.RDSTaskConfig;
import com.qlangtech.tis.manage.common.trigger.sources.TddlTaskConfig;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerTaskConfig extends SourceType {

    public static final String DS_TYPE_TDDL = "tddl";

    public static final String DS_TYPE_RDS = "rds";

    public static final String DS_TYPE_ODPS = "odps";

    // 任务执行类型
    private ExecType execType;

    /**
     * 用户创建索引的时候定义规格（qps）
     */
    private Integer maxQPS;

    public static void main(String[] arg) {
        String value = "{\"appName\":\"JST_TERMHOME_APP\",\"cols\":[\"dpt_id\",\"is_auto_deploy\",\"update_time\",\"create_time\",\"price\"],\"dbs\":{\"JST_TERMINATORHOME_GROUP\":[\"application\"]},\"logicTableName\":\"application\",\"maxDumpCount\":50,\"odpsConfig\":{\"accessId\":\"07mbAZ3it1QaLTE8\",\"accessKey\":\"BEGxJuH9JY7E6AYR2qWoHSjvufN0cV\",\"dailyPartition\":{\"key\":\"dp\",\"value\":\"20141101\"},\"datatunelEndPoint\":\"http://dt.odps.aliyun.com\",\"groupPartition\":\"gp\",\"project\":\"jst_tsearcher\",\"serviceEndPoint\":\"http://service.odps.aliyun.com/api\",\"shallIgnorPartition\":false},\"shareId\":\"dpt_id\",\"taskId\":1111,\"type\":\"tddl\"}";
        TriggerTaskConfig.parse(value);
    }

    /**
     * 序列化
     *
     * @param config
     * @return
     */
    public static String serialize(TriggerTaskConfig config) {
        JSON json = (JSON) JSON.toJSON(config);
        return json.toJSONString();
    }

    /**
     * 解析一个对象
     *
     * @param value
     * @return
     */
    public static TriggerTaskConfig parse(String value) {
        SourceType type = JSON.parseObject(value, SourceType.class);
        if (DS_TYPE_TDDL.equals(type.getType())) {
            return JSON.parseObject(value, TddlTaskConfig.class);
        } else if (DS_TYPE_RDS.equals(type.getType())) {
            return JSON.parseObject(value, RDSTaskConfig.class);
        } else if (DS_TYPE_ODPS.equals(type.getType()) || "bcrds".equals(type.getType())) {
            return JSON.parseObject(value, ODPSTaskConfig.class);
        } else {
            throw new IllegalArgumentException("illegal source type:" + type + ",value:" + value);
        }
    }

    public Integer getMaxQPS() {
        return maxQPS;
    }

    public void setMaxQPS(Integer maxQPS) {
        this.maxQPS = maxQPS;
    }

    public ExecType getExecType() {
        return execType;
    }

    public void setExecType(ExecType execType) {
        this.execType = execType;
    }

    public final String getAppName() {
        return appName;
    }

    public final void setAppName(String appName) {
        this.appName = appName;
    }

    private int taskid;

    protected String appName;

    private Long maxDumpCount;

    /**
     */
    public TriggerTaskConfig() {
        super();
    }

    public int getTaskId() {
        return taskid;
    }

    public void setTaskId(int taskId) {
        this.taskid = taskId;
    }

    public Long getMaxDumpCount() {
        return maxDumpCount;
    }

    public void setMaxDumpCount(Long maxDumpCount) {
        this.maxDumpCount = maxDumpCount;
    }
}

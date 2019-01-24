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
package com.qlangtech.tis.manage.common.trigger.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.manage.common.trigger.ODPSConfig;
import com.qlangtech.tis.manage.common.trigger.ODPSConfig.DailyPartition;
import com.qlangtech.tis.manage.common.trigger.TriggerTaskConfig;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TddlTaskConfig extends TriggerTaskConfig {

    private String logicTableName;

    // tddl解析出来的拓扑结构
    private Map<String, List<String>> /* tabs */
    dbs = new HashMap<String, /* groupname */
    List<String>>();

    // 需要 导入的列，与table中的列求交之后的结果
    private List<String> cols = new ArrayList<String>();

    private ODPSConfig odpsConfig;

    private String tddlAppName;

    /**
     * 数据预估
     */
    private Integer dataSizeEstimate;

    public Integer getDataSizeEstimate() {
        return dataSizeEstimate;
    }

    public void setDataSizeEstimate(Integer dataSizeEstimate) {
        this.dataSizeEstimate = dataSizeEstimate;
    }

    public void addTable(String groupname, List<String> tableName) {
        List<String> tabs = dbs.get(groupname);
        if (tabs == null) {
            tabs = new ArrayList<String>();
            dbs.put(groupname, tabs);
        }
        tabs.addAll(tableName);
    }

    public void addTable(String groupname, String tableName) {
        addTable(groupname, Arrays.asList(tableName));
    }

    // 分区键
    private String shareId;

    public String getLogicTableName() {
        return logicTableName;
    }

    public void setLogicTableName(String logicTableName) {
        this.logicTableName = logicTableName;
    }

    public ODPSConfig getOdpsConfig() {
        return odpsConfig;
    }

    public void setOdpsConfig(ODPSConfig odpsConfig) {
        this.odpsConfig = odpsConfig;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public Map<String, List<String>> getDbs() {
        return dbs;
    }

    public List<String> getCols() {
        return this.cols;
    }

    public void setDbs(Map<String, List<String>> dbs) {
        this.dbs = dbs;
    }

    public void setCols(List<String> cols) {
        this.cols = cols;
    }

    /**
     * 添加需要导出的列
     *
     * @param colName
     */
    public void addSchemaColumn(String colName) {
        this.cols.add(colName);
    }

    public String getTddlAppName() {
        return tddlAppName;
    }

    public void setTddlAppName(String tddlAppName) {
        this.tddlAppName = tddlAppName;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TddlTaskConfig config = new TddlTaskConfig();
        config.setTaskId(123);
        config.setAppName("search41234");
        config.setLogicTableName("user");
        config.setMaxDumpCount(123l);
        ODPSConfig odpsCfg = new ODPSConfig();
        odpsCfg.setAccessId("dafasdfasd");
        odpsCfg.setAccessKey("dafdafasdfasd");
        odpsCfg.setDailyPartition(new DailyPartition("ps", "20141101"));
        odpsCfg.setServiceEndPoint("http://adfadfasdf");
        odpsCfg.setGroupPartition("group_mod=dddd");
        odpsCfg.setProject("project");
        odpsCfg.setShallIgnorPartition(false);
        config.setOdpsConfig(odpsCfg);
        config.addTable("adfasd", "user_0000");
        config.addTable("adfasd", "user_0001");
        JSON json = (JSON) JSON.toJSON(config);
        System.out.println(json.toJSONString());
        config = JSON.parseObject(json.toJSONString(), TddlTaskConfig.class);
        System.out.println(config.getAppName());
    }
}

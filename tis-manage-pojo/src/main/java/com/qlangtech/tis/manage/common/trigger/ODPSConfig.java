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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ODPSConfig {

    private String accessKey;

    private String accessId;

    private String project;

    private String serviceEndPoint;

    private String datatunelEndPoint;

    private Integer groupSize;

    public Integer getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(Integer groupSize) {
        this.groupSize = groupSize;
    }

    // 需要忽略 分区吗？
    private boolean shallIgnorPartition;

    // 日期分区键key名称
    private DailyPartition dailyPartition;

    // 组分区键key名称
    private String groupPartition;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isShallIgnorPartition() {
        return shallIgnorPartition;
    }

    public void setShallIgnorPartition(boolean shallIgnorPartition) {
        this.shallIgnorPartition = shallIgnorPartition;
    }

    public DailyPartition getDailyPartition() {
        return dailyPartition;
    }

    public static class DailyPartition {

        private String key;

        private String value;

        /**
         */
        public DailyPartition() {
            super();
        }

        public DailyPartition(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public void setDailyPartition(DailyPartition dailyPartition) {
        this.dailyPartition = dailyPartition;
    }

    public String getGroupPartition() {
        return groupPartition;
    }

    public void setGroupPartition(String groupPartition) {
        this.groupPartition = groupPartition;
    }

    public String getServiceEndPoint() {
        return serviceEndPoint;
    }

    public void setServiceEndPoint(String serviceEndPoint) {
        this.serviceEndPoint = serviceEndPoint;
    }

    public String getDatatunelEndPoint() {
        return datatunelEndPoint;
    }

    public void setDatatunelEndPoint(String datatunelEndPoint) {
        this.datatunelEndPoint = datatunelEndPoint;
    }
}

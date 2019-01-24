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
package com.qlangtech.tis.manage.common;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerCrontab {

    private String appName;

    private Integer appId;

    private Integer fjobId;

    private Integer fjobType;

    private String fcrontab;

    // 该定时任务是否已经停止？
    private boolean fstop;

    private Integer ijobId;

    private Integer ijobType;

    private String icrontab;

    // 该定时任务是否已经停止？
    private boolean istop;

    public boolean isFstop() {
        return fstop;
    }

    public void setFstop(boolean fstop) {
        this.fstop = fstop;
    }

    public boolean isIstop() {
        return istop;
    }

    public void setIstop(boolean istop) {
        this.istop = istop;
    }

    public Integer getFjobId() {
        return fjobId;
    }

    public void setFjobId(Integer fjobId) {
        this.fjobId = fjobId;
    }

    public Integer getFjobType() {
        return fjobType;
    }

    public void setFjobType(Integer fjobType) {
        this.fjobType = fjobType;
    }

    public String getFcrontab() {
        return fcrontab;
    }

    public void setFcrontab(String fcrontab) {
        this.fcrontab = fcrontab;
    }

    public Integer getIjobId() {
        return ijobId;
    }

    public void setIjobId(Integer ijobId) {
        this.ijobId = ijobId;
    }

    public Integer getIjobType() {
        return ijobType;
    }

    public void setIjobType(Integer ijobType) {
        this.ijobType = ijobType;
    }

    public String getIcrontab() {
        return icrontab;
    }

    public void setIcrontab(String icrontab) {
        this.icrontab = icrontab;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }
}

/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.indexbuilder.map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SuccessFlag {

    private final String name;

    // 持有这个任务的task名称
    public SuccessFlag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public enum Flag {

        RUNNING, SUCCESS, KILL, FAILURE
    }

    private Flag flag = Flag.RUNNING;

    public Flag getFlag() {
        return flag;
    }

    private void setFlag(Flag flag) {
        if (this.flag == Flag.FAILURE || this.flag == Flag.KILL) {
            // 如果进程已经被终止了就不能再被设置状态了，以免被设置成失败状态之后再被设置成成功
            return;
        }
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(Flag flag, String msg) {
        this.setFlag(flag);
        this.msg = msg;
    }

    private String msg;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("name:").append(this.name).append(",flag:").append(flag).append(",msg:").append(this.getMsg());
        return buffer.toString();
    }
}

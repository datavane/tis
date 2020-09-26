/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

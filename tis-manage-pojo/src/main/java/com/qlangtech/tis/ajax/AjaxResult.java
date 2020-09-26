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
package com.qlangtech.tis.ajax;

import java.util.List;

/**
 * 当服务端发送一个新的Ajax请求，之后反馈的消息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月20日
 */
public class AjaxResult<T> {

    private boolean success;

    private List<String> errormsg;

    private List<String> msg;

    private T bizresult;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(List<String> errormsg) {
        this.errormsg = errormsg;
    }

    public List<String> getMsg() {
        return msg;
    }

    public void setMsg(List<String> msg) {
        this.msg = msg;
    }

    public T getBizresult() {
        return bizresult;
    }

    public void setBizresult(T bizresult) {
        this.bizresult = bizresult;
    }
}

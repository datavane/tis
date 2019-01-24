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
package com.qlangtech.tis.runtime.module.screen;

import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Changedomain extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    public Changedomain() {
        super("changedomain");
    }

    private static final String GO_BACK_URL_KEY = "gobackurl";

    // public void execute(//@Param("bizid") Integer bizid,
    // //@Param("appid") Integer appId,
    // Context context) throws Exception {
    // 
    // Integer bizid = this.getInt("bizid");
    // Integer appid = this.getInt("appid");
    // 
    // 
    // 
    // if (StringUtils.isEmpty(getFieldValue(GO_BACK_URL_KEY))) {
    // setFieldValue(GO_BACK_URL_KEY, this.getRequest().getHeader(
    // "Referer"));
    // }
    // 
    // context.put("envirlist", RunEnvironment.getRunEnvironmentList());
    // }
    // @Override
    // public void setFieldValue(String field, Object value) {
    // 
    // // try {
    // // return this.getDefaultGroup().getField(field);
    // // } catch (Exception e) {
    // // throw new RuntimeException("field:" + field + " is not exist", e);
    // // }
    // 
    // Group group = getGroup(("true"
    // .equals(this.getString("justenvironment")) ? "runtime"
    // : StringUtils.EMPTY)
    // + ChangeDomainAction.GROUPNAME);
    // 
    // group.getField(field).setValue(value);
    // }
    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}

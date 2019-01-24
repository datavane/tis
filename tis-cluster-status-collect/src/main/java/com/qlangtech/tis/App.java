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
package com.qlangtech.tis;

import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class App {

    private static final String DPT_SEPARATE = "/";

    private String dpt;

    public String getBu() {
        String[] dpts = StringUtils.split(dpt, DPT_SEPARATE);
        for (int i = 0; i < dpts.length; i++) {
            if (StringUtils.isNotBlank(dpts[i])) {
                return dpts[i];
            }
        }
        throw new IllegalStateException("is not a illeal dpt name:" + dpt);
    }

    private String serviceName;

    private Integer appid;

    public String getDpt() {
        String[] dpts = StringUtils.split(dpt, DPT_SEPARATE);
        // if (dpts.length > 2) {
        // return dpts[dpts.length - 2] + DPT_SEPARATE + dpts[dpts.length - 1];
        // }
        // return StringUtils.substringAfterLast(dpt, DPT_SEPARATE);
        StringBuffer dptName = new StringBuffer();
        boolean hasFindBu = false;
        for (int i = 0; i < dpts.length; i++) {
            if (!hasFindBu && StringUtils.isNotBlank(dpts[i])) {
                hasFindBu = true;
                continue;
            }
            if (StringUtils.isNotBlank(dpts[i])) {
                dptName.append(dpts[i]);
                if ((i + 1) < dpts.length) {
                    dptName.append(DPT_SEPARATE);
                }
            }
        }
        return dptName.toString();
    }

    public Integer getAppid() {
        return appid;
    }

    public void setAppid(Integer appid) {
        this.appid = appid;
    }

    public void setDpt(String dpt) {
        this.dpt = dpt;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}

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
package com.qlangtech.tis.runtime.module.screen.jarcontent;

import java.util.Arrays;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.screen.BasicManageScreen;
import com.qlangtech.tis.runtime.pojo.ResSynManager;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FileCompareResult extends BasicManageScreen {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        String[] snid = this.getRequest().getParameterValues("comparesnapshotid");
        if (snid.length != 2) {
            throw new IllegalArgumentException("param comparesnapshotid lenght is not equal to 2," + snid.length);
        }
        int[] snids = new int[2];
        snids[0] = Integer.parseInt(snid[0]);
        snids[1] = Integer.parseInt(snid[1]);
        Arrays.sort(snids);
        ResSynManager synManager = ResSynManager.create(this.getAppDomain().getAppName(), // /////// old
        this.getSnapshotViewDAO().getView(snids[0]), // new
        this.getSnapshotViewDAO().getView(snids[1]), this);
        context.put("synManager", synManager);
        context.put("differ", synManager.diff());
    }
}

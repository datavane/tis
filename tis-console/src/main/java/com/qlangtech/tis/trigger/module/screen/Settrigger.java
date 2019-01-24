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
package com.qlangtech.tis.trigger.module.screen;

import static com.qlangtech.tis.trigger.biz.dal.dao.JobConstant.JOB_INCREASE_DUMP;
import static com.qlangtech.tis.trigger.biz.dal.dao.JobConstant.JOB_TYPE_FULL_DUMP;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.spring.aop.Func;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Settrigger extends TriggerBasicScreen {

    /**
     */
    private static final long serialVersionUID = 1L;

    private static final Set<String> executeMethod = new HashSet<String>();

    private static final String EXECUTE_UPDATE = "update";

    static {
        executeMethod.add(EXECUTE_UPDATE);
        executeMethod.add("set");
    }

    @Override
    @Func(PermissionConstant.TRIGGER_JOB_SET)
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        Assert.assertNotNull("appid can not be null", this.getInt("appid"));
        int dumptype = this.getInt("dumptype");
        if (JOB_TYPE_FULL_DUMP == dumptype) {
            context.put("executeLiteria", "full");
        } else if (JOB_INCREASE_DUMP == dumptype) {
            context.put("executeLiteria", "increase");
        } else {
            throw new IllegalStateException("dumptype:" + dumptype + " is illegal");
        }
        final String execute = this.getString("execute");
        Assert.assertTrue("param execute can not be null", executeMethod.contains(execute));
        if (EXECUTE_UPDATE.equals(execute)) {
            // 更新模式
            Integer jobid = this.getInt("jobid");
            Assert.assertNotNull(jobid);
            context.put("updateTrigger", this.getTriggerJobDAO().loadFromWriteDB(new Long(jobid)));
        }
        context.put("execute", execute);
    }
}

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
package com.qlangtech.tis.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qlangtech.tis.config.module.action.CrontabListAction;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.common.trigger.TriggerTaskConfig;
import com.qlangtech.tis.trigger.httpserver.ITriggerContext;

/*
 * 通过本地加载数据库的方式加载所有定时任务
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisTriggerJobManage extends TriggerJobManage {

	private RunContext runContext;

	@Override
	public void triggerFullDump(TriggerTaskConfig config, ITriggerContext triggerContext) throws Exception {
		super.triggerFullDump(config, triggerContext);
	}

	@Override
	protected Map<String, Crontab> getAllAvailableCrontabs() throws Exception {
		List<TriggerCrontab> contabs = CrontabListAction.getAllTriggerCrontab(getRunContext());
		Map<String, Crontab> result = new HashMap<String, Crontab>();
		Crontab t = null;
		for (TriggerCrontab contab : contabs) {
			t = new Crontab(contab.getAppName(), contab.getFcrontab(), contab.getFjobId());
			result.put(getGroupName(contab.getAppName()), t);
		}
		return result;
	}

	protected void shnchronizeCrontabConfig() {
		super.shnchronizeCrontabConfig();
	}

	public RunContext getRunContext() {
		return runContext;
	}

	public void setRunContext(RunContext runContext) {
		this.runContext = runContext;
	}
}

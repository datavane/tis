package com.qlangtech.tis.runtime.module.screen;

import com.alibaba.citrus.turbine.Context;

/**
 * @author 百岁（baisui@2dfire.com）
 *
 * @date 2019年2月20日
 */
public class Errors extends BasicScreen {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Context context) throws Exception {
		this.disableNavigationBar(context);
		// getRundataInstance().setLayout("blank");
	}

}

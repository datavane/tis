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

//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import junit.framework.Assert;
//import com.alibaba.citrus.turbine.Context;
//import com.qlangtech.tis.manage.PermissionConstant;
//import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria.Criteria;
//import com.qlangtech.tis.runtime.pojo.ServerGroupAdapter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class App extends BasicManageScreen {

//	/**
//	 */
//	private static final long serialVersionUID = 1L;
//
//	@Override
//	public void execute(final Context context) throws Exception {
//		// this.disableDomainView(context);
//		final Integer appid = getInt("appid");
//		Assert.assertNotNull("appid can not be null in request parameter", appid);
//		context.put("app", this.getApplicationDAO().loadFromWriteDB(appid));
//		List<ServerGroupAdapter> grouplist = this.createServerGroupAdapterList(new ServerGroupCriteriaSetter() {
//
//			@Override
//			public void process(Criteria criteria) {
//				criteria.andAppIdEqualTo(appid);
//			}
//		}, false);
//		Collections.sort(grouplist, new Comparator<ServerGroupAdapter>() {
//
//			@Override
//			public int compare(ServerGroupAdapter o1, ServerGroupAdapter o2) {
//				int environmentCompare = (o1.getRuntEnvironment() - o2.getRuntEnvironment());
//				if (environmentCompare != 0) {
//					return environmentCompare;
//				} else {
//					return o1.getGroup().getGroupIndex() - o2.getGroup().getGroupIndex();
//				}
//			}
//		});
//		// 是否有应用数据的维护权限
//		// context.put("hasdatamanageperminssion", this.getAuthService()
//		// .hasPermissionUseByNaviagionBar(this.getUserId(),
//		// PermissionConstant.PERMISSION_BASE_DATA_MANAGE));
//		context.put("hasdatamanageperminssion",
//				this.getAppsFetcher().hasGrantAuthority(PermissionConstant.APP_SERVER_GROUP_SET)
//						&& this.getAppsFetcher().hasGrantAuthority(PermissionConstant.APP_SERVER_SET));
//		context.put("grouplist", grouplist);
//	}
}

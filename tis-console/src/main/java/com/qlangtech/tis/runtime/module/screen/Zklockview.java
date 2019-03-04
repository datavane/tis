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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.spring.ZooKeeperGetter;
import com.qlangtech.tis.trigger.TriggerJobManage;
import com.qlangtech.tis.trigger.TriggerJobManage.PathValueProcess;
import com.qlangtech.tis.trigger.utils.TriggerParam;
import com.qlangtech.tis.trigger.utils.LockResult;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Zklockview extends BasicScreen {

	/**
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Context context) throws Exception {
		this.enableChangeDomain(context);
		final AppDomainInfo domain = this.getAppDomain();
		List<LockResult> result = new ArrayList<LockResult>();

		LockResult fulldumpLock = TriggerJobManage.getFullDumpNode(this.getSolrZkClient(), domain.getAppName());
		result.add((fulldumpLock != null) ? fulldumpLock : TriggerJobManage.NULL_LOCK);
		String path = "/tis/incr-transfer-group/" + TriggerParam.GROUP_NAME + "/" + domain.getAppName();
		result.add(TriggerJobManage.getNodeInfo(this.getSolrZkClient(), true, /* hasChild */
				path, false, "增量执行节点"));
		context.put("locklist", result);
	}

	public static LockResult getNodeInfo(TisZkClient zk, boolean hasChild, String path, boolean editable)
			throws KeeperException, InterruptedException, UnsupportedEncodingException {
		return TriggerJobManage.getNodeInfo(zk, hasChild, /* haChild */
				path, editable, new PathValueProcess() {

					@Override
					public String process(String path, TisZkClient zk, LockResult lock)
							throws KeeperException, InterruptedException {
						lock.stat = new Stat();
						String child = new String(zk.getData(path, null, lock.stat, true));
						lock.addChildValue(child);
						return child;
					}
				}, null);
	}

	private ZooKeeperGetter zooKeeperGetter;

	protected TisZkClient getZooKeeper() {
		return zooKeeperGetter.getInstance();
	}

	@Autowired
	public void setZooKeeperGetter(ZooKeeperGetter zooKeeperGetter) {
		this.zooKeeperGetter = zooKeeperGetter;
	}
}

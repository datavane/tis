/**
 * 
 */
package com.qlangtech.tis.openapi.impl;

import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.openapi.SnapshotNotFindException;

/**
 * @author 百岁（baisui@taobao.com）
 * @date 2013-12-20
 */
public class SnapshotDomainGetter {

	// private static final long serialVersionUID = 1L;

	private final RunContext runContext;

	public SnapshotDomainGetter(RunContext runContext) {
		super();
		this.runContext = runContext;
	}

	// @Override
	public SnapshotDomain getSnapshot(AppKey appKey)// HttpServletRequest
			// request)
			throws SnapshotNotFindException {

		SnapshotInfoFromRequest result = new SnapshotInfoFromRequest();

		// final String appName = matcher.group(1);
		// final Short groupIndex = Short.parseShort(matcher.group(2));
		// final RunEnvironment runtime =
		// RunEnvironment.getEnum(matcher.group(3));

		// final String resources = getResources(request);
		if (appKey.getTargetSnapshotId() != null
				&& appKey.getTargetSnapshotId() > 0) {
			result.snapshotId = appKey.getTargetSnapshotId().intValue();
		} else {
			final ServerGroup group = runContext.getServerGroupDAO().load(
					appKey.appName, appKey.groupIndex, appKey.runtime.getId());
			if (group == null) {
				throw new SnapshotNotFindException("appName:" + appKey.appName
						+ " groupIndex:" + appKey.groupIndex + " runtime:"
						+ appKey.runtime
						+ " has not a corresponding server group in db");
			}
			if (group.getPublishSnapshotId() == null) {
				throw new SnapshotNotFindException("groupid:" + group.getGid()
						+ " has not set publish snapshot id");
			}
			result.snapshotId = group.getPublishSnapshotId();
		}

		// 如果在request中设置了unmergeglobalparams 这个参数
		if (!appKey.unmergeglobalparams) {
			result.runtime = appKey.runtime;
		}
		// return result;

		return runContext.getSnapshotViewDAO().getView(result.snapshotId,
				result.runtime);
	}

}

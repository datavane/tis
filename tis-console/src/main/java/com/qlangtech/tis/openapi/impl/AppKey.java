/**
 * 
 */
package com.qlangtech.tis.openapi.impl;

import org.apache.commons.lang.StringUtils;

import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁
 * @date 2013-12-20
 */
public class AppKey {
	public final String appName;
	public final Short groupIndex;
	public final RunEnvironment runtime;
	public final boolean unmergeglobalparams;
	// 目标配置文件版本
	private Long targetSnapshotId;

	// 取的内容是否要用缓存中索取
	private boolean fromCache = true;

	public AppKey(String appName, Short groupIndex, RunEnvironment runtime,
			boolean unmergeglobalparams) {
		this.appName = appName;
		this.groupIndex = groupIndex;
		this.runtime = runtime;
		this.unmergeglobalparams = unmergeglobalparams;

	}

	public String getAppName() {
		return appName;
	}

	public boolean isFromCache() {
		return fromCache;
	}

	public void setFromCache(boolean fromCache) {
		this.fromCache = fromCache;
	}

	public Long getTargetSnapshotId() {
		return targetSnapshotId;
	}

	public void setTargetSnapshotId(Long targetSnapshotId) {
		this.targetSnapshotId = targetSnapshotId;
	}

	@Override
	public int hashCode() {
		// 确保 这个key在5秒之内是相同的

		final String stamp = (appName + String.valueOf(groupIndex)
				+ runtime.getKeyName() + String.valueOf(unmergeglobalparams)
				+ (this.getTargetSnapshotId() == null ? StringUtils.EMPTY
						: this.getTargetSnapshotId())
				+ (System.currentTimeMillis() / (1000 * 5)));

		// String stamp = (appName + String.valueOf(groupIndex)
		// + runtime.getKeyName() + String
		// .valueOf(unmergeglobalparams));

		// System.out.println(stamp);

		return stamp.hashCode();
	}

}

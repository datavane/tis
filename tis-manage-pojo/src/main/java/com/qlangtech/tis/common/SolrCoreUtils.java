package com.qlangtech.tis.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@2dfire.com）
 *
 * @date 2019年2月13日
 */
public class SolrCoreUtils {
	private static String NAME_PREFIX = "search4";
	private static final Pattern coreNamePattern = Pattern.compile(NAME_PREFIX + "(.+?)_shard(\\d+?)_replica_n(\\d+?)");

	public static class TisCoreName {
		private String name;
		private int sharedNo;
		private int replicaNo;

		public String getName() {
			return NAME_PREFIX + name;
		}

		public int getSharedNo() {
			return sharedNo;
		}

		public int getReplicaNo() {
			return replicaNo;
		}

	}

	public static TisCoreName parse(String corename) {
		TisCoreName coreName = new TisCoreName();

		Matcher coreNameMatcher = coreNamePattern.matcher(corename);
		if (!coreNameMatcher.matches()) {
			throw new IllegalArgumentException("core name:" + corename + " does not match pattern:" + coreNamePattern);
		}

		coreName.name = coreNameMatcher.group(1);
		coreName.sharedNo = Integer.parseInt(coreNameMatcher.group(2));
		coreName.replicaNo = Integer.parseInt(coreNameMatcher.group(3));

		return coreName;
	}
}

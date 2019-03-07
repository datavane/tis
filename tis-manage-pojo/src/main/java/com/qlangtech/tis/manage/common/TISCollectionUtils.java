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
package com.qlangtech.tis.manage.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISCollectionUtils {

	public static String INDEX_BACKFLOW_STATUS = "indexflowback_status";
	public static String INDEX_BACKFLOW_READED = "readed";
	public static String INDEX_BACKFLOW_ALL = "all";

	private static String NAME_PREFIX = "search4";
	private static final Pattern coreNamePattern = Pattern.compile(NAME_PREFIX + "(.+?)_shard(\\d+?)_replica_n(\\d+?)");

	public static class TisCoreName {
		private String name;
		private int sharedNo;
		private int replicaNo;

		public String getName() {
			return NAME_PREFIX + this.name;
		}

		public int getSharedNo() {
			return this.sharedNo;
		}

		public int getReplicaNo() {
			return this.replicaNo;
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

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
package com.qlangtech.tis.yarn.common;

import java.io.File;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class YarnConstant {

	public static final String YARN_CONFIG_FILE_NAME = "yarn-site.xml";

	public static final String CLASSPATH_YARN_CONFIG_PATH = "/tis-web-config/" + YarnConstant.YARN_CONFIG_FILE_NAME;

	public static final String PATH_YARN_SITE = "/opt/app/hadoop-2.6.1/etc/hadoop/" + YARN_CONFIG_FILE_NAME;

	public static final String HDFS_GROUP_LIB_DIR = "/user/admin/";

	public static final String INDEX_BUILD_JAR_DIR = "indexbuild";

	// ===============================================
	public static final String PARAM_COLLECTION_NAME = "collection";

	public static final String PARAM_OPTION_LOCAL_JAR_DIR = "localJarDir";

	public static final String LOCAL_JAR_DIR_PATH = INDEX_BUILD_JAR_DIR + File.separator + PARAM_OPTION_LOCAL_JAR_DIR;

	public static final String PARAM_OPTION_LOCAL_QUEUE = "queue";
}

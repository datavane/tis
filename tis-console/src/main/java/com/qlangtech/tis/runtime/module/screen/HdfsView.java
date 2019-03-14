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

import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
 * HDFS 目录结构
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsView extends BasicScreen {

	private static final long serialVersionUID = 1L;

	// private static final String hdfs_host = "hdfs://10.232.36.131:9000";
	static {
	}

	private String appName;

	// private String parent;
	// @Override
	// public String execute() throws Exception {
	//
	// return INPUT;
	// }
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public String getPageName() {
		return "/runtime/hdfs_view.htm";
	}

	@Override
	public void execute(Context context) throws Exception {
		this.enableChangeDomain(context);
		String parent = this.getString("parent");
		Path path = null;
		String appName = this.getAppDomain().getAppName();
		if (StringUtils.isBlank(parent)) {
			// || StringUtils.indexOf(parent, appName) < 1) {
			path = getDefaultPath(appName);
		} else {
			path = new Path(parent);
		}

		context.put("currentpath", TSearcherConfigFetcher.get().getHdfsAddress() + parsePath(path));
		final List<HdfsNode> childNodes = new ArrayList<HdfsNode>();
		if (!getFilesystem().exists(path)) {
			context.put("childnodes", childNodes);
			this.forward("hdfsViewTemplate.vm");
			return;
		}
		FileStatus[] fileStatus = getFilesystem().listStatus(path);
		HdfsNode hdfsNode = null;
		FileStatus status = null;
		for (int i = 0; fileStatus != null && i < fileStatus.length; i++) {
			status = fileStatus[i];
			hdfsNode = new HdfsNode(path);
			// hdfsNode.pId = path.toString();
			hdfsNode.name = status.getPath().getName();
			hdfsNode.id = URLEncoder.encode(path.toString() + "/" + hdfsNode.name, getEncode());
			hdfsNode.modifyTime = status.getModificationTime();
			hdfsNode.size = status.getLen();
			hdfsNode.parent = status.isDir();
			if (hdfsNode.isSuccessFile()) {
				InputStream reader = getFilesystem().open(status.getPath());
				hdfsNode.memo = "一共导入了" + IOUtils.toString(reader) + "条数据";
				IOUtils.closeQuietly(reader);
			}
			childNodes.add(hdfsNode);
		}
		// Collections.sort(childNodes, new Comparator<HdfsNode>() {
		// @Override
		// public int compare(HdfsNode o1, HdfsNode o2) {
		// return (int) (o2.modifyTime - o1.modifyTime);
		// }
		// });
		// this.setChildNodes(childNodes);
		// writeJson2Response(childNodes);
		// return "childnodes";
		context.put("childnodes", childNodes);
		this.forward("hdfsViewTemplate.vm");
	}

	/**
	 * @param appName
	 * @return
	 */
	protected Path getDefaultPath(String appName) {
		return new Path(TSearcherConfigFetcher.get().getHDFSRootDir()+"/" + appName);
	}

	private String parsePath(Path path) throws Exception {
		String[] p = StringUtils.split(path.toString(), "/");
		// if (p.length < 2) {
		// return path.toString();
		// }
		StringBuffer result = new StringBuffer();
		StringBuffer pathTemp = new StringBuffer();
		for (int i = 0; i < p.length; i++) {
			result.append("/");
			pathTemp.append("/").append(p[i]);
			// if (i < 1) {
			// result.append(p[i]);
			// } else {
			result.append("<a target='_self' class='path' href='" + this.getPageName() + "?parent=");
			result.append(URLEncoder.encode(pathTemp.toString(), getEncode()));
			result.append("'>");
			result.append(p[i]);
			result.append("</a>");
			// }
		}
		return result.toString();
	}

	// private List<HdfsNode> childNodes;
	// public void setChildNodes(List<HdfsNode> childNodes) {
	// this.childNodes = childNodes;
	// }
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	// public static void main(String[] arg) throws Exception {
	// System.out.println(getFilesystem());
	//
	// FSDataInputStream reader = getFilesystem()
	// .open(
	// new Path(
	// 
	//
	// // 读取success文件
	// System.out.println("dump count :" + IOUtils.toString(reader));
	//
	// IOUtils.closeQuietly(reader);
	// }
	private static final Map<RunEnvironment, FileSystem> fileSysMap = new HashMap<RunEnvironment, FileSystem>();

	protected FileSystem getFilesystem() {
		RunEnvironment envi = this.getAppDomain().getRunEnvironment();
		return getFilesystem(envi);
	}

	/**
	 * @return the filesystem
	 */
	protected FileSystem getFilesystem(RunEnvironment envi) {
		FileSystem fileSys = fileSysMap.get(envi);
		if (fileSys == null) {
			fileSys = TISHdfsUtils.getFileSystem();
			fileSysMap.put(envi, fileSys);
		}
		return fileSys;
	}

	// private FileSystem createFileSystem(String hdfsHost) {
	// Configuration configuration = new Configuration();
	// FileSystem fileSys = null;
	// if (StringUtils.isEmpty(hdfsHost)) {
	// throw new IllegalStateException("hdfsHost can not be null");
	// }
	// try {
	// configuration.set("fs.default.name", hdfsHost);
	// // configuration.set("mapred.job.tracker",
	// // "10.232.36.131:9001");
	// // configuration.set("mapred.local.dir",
	// // "/home/yusen/hadoop/mapred/local");
	// // configuration.set("mapred.system.dir",
	// // "/home/yusen/hadoop/tmp/mapred/system");
	// // configuration.setInt("dump.split.size", 2);
	// //
	// configuration.addResource("core-site.xml");
	// configuration.addResource("mapred-site.xml");
	//
	// fileSys = FileSystem.get(configuration);//
	// FileSystem.newInstance(configuration);
	//
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// return fileSys;
	// }
	public static void main(String[] arg) throws Exception {
		// ClassLoader loader = HdfsView.class.getClassLoader();
		// System.out.println(loader);
		// System.out.println(loader
		// .getResource("org/apache/hadoop/ipc/Client.class"));
		// HdfsView view = new HdfsView();
		//
		// FileSystem fs = view.createFileSystem("hdfs://10.1.6.211:9000");
		//
		// FileStatus[] fileStatus = fs.listStatus(new Path("/user/hive"));
		// FileStatus status = null;
		// for (int i = 0; fileStatus != null && i < fileStatus.length; i++) {
		// status = fileStatus[i];
		//
		// System.out.println(status.getPath());
		//
		// // System.out.println("path:" + .getPath());
		// // System.out.println("path:" + fileStatus[i].getPath().getName());
		// // System.out.println("last modify:"
		// // + new Date(fileStatus[i].getModificationTime()));
		// //
		// // System.out.println(fileStatus[i].isDir());
		// //
		// // System.out.println();
		//
		// }
	}

	public static class HdfsNode {

		private final Path path;

		// },
		public HdfsNode(Path path) {
			super();
			this.path = path;
		}

		public String getPath() {
			return (new Path(this.path, this.getName())).toString();
		}

		private String id;

		private String pId;

		// 节点名称
		private String name;

		// 是否是父亲节点
		private boolean parent;

		private long size;

		// private String modifyTime;
		private long modifyTime;

		// 备注
		private String memo;

		public String getMemo() {
			return memo;
		}

		public String getModifyTime() {
			return dateFormat.format(new Date(modifyTime));
		}

		public String getId() {
			return id;
		}

		public String getpId() {
			return pId;
		}

		public boolean isParent() {
			return parent;
		}

		public String getName() {
			return name;
		}

		public long getSize() {
			return size;
		}

		public String getFormatSize() {
			// ManageUtils.formatVolume(size);
			return FileUtils.byteCountToDisplaySize(size);
		}

		/**
		 * 是否是成功文件
		 *
		 * @return
		 */
		public boolean isSuccessFile() {
			return !this.isParent() && StringUtils.endsWith(this.getName(), ".suc");
		}

		public boolean isDownloadable() {
			return !this.isParent() & this.getSize() > 0;
		}
	}
}

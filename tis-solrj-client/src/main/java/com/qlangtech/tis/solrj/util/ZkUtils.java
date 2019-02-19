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
package com.qlangtech.tis.solrj.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.TisZkClient;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ZkUtils {

	private static final Logger logger = LoggerFactory.getLogger(ZkUtils.class);

	private static final String PATH_SPLIT = "/";

	public static String getFirstChildValue(final TisZkClient zookeeper, final String zkPath) {
		return getFirstChildValue(zookeeper, zkPath, null, false);
	}

	public static String getFirstChildValue(final TisZkClient zookeeper, final String zkPath, final Watcher watcher) {
		return getFirstChildValue(zookeeper, zkPath, watcher, watcher != null);
	}

	public static String getFirstChildValue(final TisZkClient zookeeper, final String zkPath, final Watcher watcher,
			boolean onReconnect) {
		try {
			List<String> children = zookeeper.getChildren(zkPath, watcher, true);
			if (onReconnect && watcher != null) {
				zookeeper.addOnReconnect(new OnReconnect() {

					@Override
					public void command() {
						getFirstChildValue(zookeeper, zkPath, watcher, false);
					}
				});
			}
			for (String c : children) {
				return new String(zookeeper.getData(zkPath + PATH_SPLIT + c, null, new Stat(), true), "utf8");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		throw new IllegalStateException("zkpath:" + zkPath + " have not find child node");
	}

	/**
	 * 将本地ip地址(端口)以临时节点的方式注册的ZK上
	 *
	 * @param zookeeper
	 * @param zkPath
	 * @param port
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static String registerAddress2ZK(final TisZkClient zookeeper, final String zkPath, final int port)
			throws KeeperException, InterruptedException {
		try {
			String ip = Inet4Address.getLocalHost().getHostAddress();
			registerMyIp(zkPath, ip, port, zookeeper.getZK());
			zookeeper.addOnReconnect(new OnReconnect() {

				@Override
				public void command() {
					// try {
					registerMyIp(zkPath, ip, port, zookeeper.getZK());
					// } catch (KeeperException e) {
					// logger.error(e.getMessage() + "\n zkpath:" + zkPath, e);
					// throw new
					// ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
					// "",
					// e);
					// } catch (InterruptedException e) {
					// logger.error(e.getMessage() + "\n zkpath:" + zkPath, e);
					// // Restore the interrupted status
					// Thread.currentThread().interrupt();
					// throw new
					// ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
					// "",
					// e);
					// }
				}
			});
			return ip;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 注册内容到临时节点上
	 *
	 * @param zookeeper
	 * @param zkPath
	 *            临时节点路径例如：/tis/incr-transfer-group/search4totalpay/consume-0000001
	 *            临时节点的内容为json内容如下：
	 * @param content
	 *            在Yarn集群启动的一个分区节点会自动将本节点的信息注册到Zookeeper节点上
	 *            临时节点的内容为json内容如下：id分区消费节点的id值，该值应该是这个节点启动时自动生成的一个32位的GUID值（这个id值伴随该节点的整个生命周期）
	 *            group：消费节点所在消费组，通常一个组内可以放置多个索引消费节点 host:消费节点所在的节点地址
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static void registerTemporaryContent(final TisZkClient zookeeper, final String zkPath, final String content)
			throws KeeperException, InterruptedException {
		registerContent(zkPath, content, zookeeper.getZK());
		zookeeper.addOnReconnect(new OnReconnect() {

			@Override
			public void command() {
				// try {
				registerContent(zkPath, content, zookeeper.getZK());
				// } catch (KeeperException e) {
				// logger.error(e.getMessage() + "\n zkpath:" + zkPath, e);
				// throw new
				// ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "",
				// e);
				// } catch (InterruptedException e) {
				// logger.error(e.getMessage() + "\n zkpath:" + zkPath, e);
				// // Restore the interrupted status
				// Thread.currentThread().interrupt();
				// throw new
				// ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "",
				// e);
				// }
			}
		});
	}

	private static void registerContent(final String zkpath, String content, SolrZkClient zookeeper) {
		try {
			String[] pathname = StringUtils.split(zkpath, PATH_SPLIT);
			if (pathname.length > 1) {
				StringBuffer path = new StringBuffer();
				guaranteeExist(zookeeper.getSolrZooKeeper() //
						, path, Arrays.copyOfRange(pathname, 0, pathname.length - 1), 0);
			}
			zookeeper.create(zkpath, content.getBytes(Charset.forName("utf8")), CreateMode.EPHEMERAL_SEQUENTIAL, true);
		} catch (Exception e) {
			logger.error(e.getMessage() + "\n zkpath:" + zkpath, e);
			throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, e.getMessage() + "\n zkpath:" + zkpath,
					e);
			// throw new RuntimeException(e);
		}
	}

	/**
	 * @param zookeeper
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	private static String registerMyIp(final String parentNodepath, String ip, int port, SolrZkClient zookeeper) {
		try {
			if ("127.0.0.1".equals(ip)) {
				throw new IllegalStateException("ip can not be 127.0.0.1");
			}
			if (port > 0) {
				ip = ip + ":" + port;
			}
			registerContent(parentNodepath + "/nodes", ip, zookeeper);
			return ip;
			// String[] pathname = StringUtils.split(parentNodepath,
			// PATH_SPLIT);
			// StringBuffer path = new StringBuffer();
			// guaranteeExist(zookeeper, path, pathname, 0);
			//
			// zookeeper.create(parentNodepath + "/nodes", ip.getBytes(),
			// CreateMode.EPHEMERAL_SEQUENTIAL, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 确保节点存在
	 *
	 * @param zookeeper
	 * @param parentNodepath
	 */
	public static void guaranteeExist(ZooKeeper zookeeper, String parentNodepath) throws Exception {
		String[] pathname = StringUtils.split(parentNodepath, PATH_SPLIT);
		StringBuffer path = new StringBuffer();
		guaranteeExist(zookeeper, path, pathname, 0);
	}

	private static void guaranteeExist(ZooKeeper zookeeper, StringBuffer path, String[] paths, int deepth)
			throws Exception {
		if (deepth >= paths.length) {
			return;
		}
		path.append(PATH_SPLIT).append(paths[deepth]);
		if (zookeeper.exists(path.toString(), false) == null) {
			zookeeper.create(path.toString(), StringUtils.EMPTY.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		}
		guaranteeExist(zookeeper, path, paths, ++deepth);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// String fromZkAddress = args[0];
		// String toZkAddress = args[1];
		//
		// System.out.println("fromZkAddress:" + fromZkAddress);
		// System.out.println("toZkAddress:" + toZkAddress);
		//
		// TisZkClient fromZk = new TisZkClient(fromZkAddress, 30000);
		// TisZkClient toZk = new TisZkClient(toZkAddress, 30000);
		// final String parent = PATH_SPLIT;
		// processNode(fromZk, toZk, parent);
		System.out.println("start");
		String[] pathname = new String[] { "a", "b" };
		pathname = Arrays.copyOfRange(pathname, 0, pathname.length - 1);
		for (String n : pathname) {
			System.out.println(n);
		}
	}

	protected static void processNode(TisZkClient fromZk, TisZkClient toZk, String zkpath)
			throws KeeperException, InterruptedException, Exception {
		List<String> child = fromZk.getChildren(zkpath, null, true);
		Stat state = new Stat();
		byte[] content = null;
		String childPath = null;
		// 将节点拷贝
		guaranteeExist(toZk.getZK().getSolrZooKeeper(), zkpath);
		for (String c : child) {
			if (StringUtils.endsWith(zkpath, PATH_SPLIT)) {
				childPath = zkpath + c;
			} else {
				childPath = zkpath + PATH_SPLIT + c;
			}
			content = fromZk.getData(childPath, null, state, true);
			// 持久节点
			if (state.getEphemeralOwner() < 1) {
				try {
					toZk.create(childPath, content, CreateMode.PERSISTENT, true);
					System.out.println("create node:" + childPath);
				} catch (Exception e) {
					throw new RuntimeException("childPath create error:" + childPath, e);
				}
				if (state.getNumChildren() > 0) {
					processNode(fromZk, toZk, childPath);
				}
			}
		}
	}
}

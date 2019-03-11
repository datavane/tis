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
package com.qlangtech.tis.indexbuilder.source.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.net.NetworkTopology;
import org.apache.hadoop.net.Node;
import org.apache.hadoop.net.NodeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.indexbuilder.map.IndexConf;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class FileSplitor {

	public static final Logger logger = LoggerFactory.getLogger(FileSplitor.class);

	FileSystem fileSystem;

	static final double SPLIT_SLOP = 1.1D;

	IndexConf indexConf;

	long totalSize;

	public FileSplitor() {
	}

	public FileSplitor(IndexConf indexConf, FileSystem fileSystem) {
		this.indexConf = indexConf;
		this.fileSystem = fileSystem;
	}

	protected abstract void getFiles(Path paramPath, List<FileStatus> paramList) throws Exception;

	public List<FileSplit> getSplits() throws Exception {
		List<FileStatus> files = new ArrayList<FileStatus>();
		getFiles(new Path(this.indexConf.getSourcePath()), files);
		for (FileStatus file : files) {
			if (file.isDir()) {
				continue;
			}
			this.totalSize += file.getLen();
		}
		if (this.totalSize == 0L) {
			throw new Exception("源文件大小为0！");
		}
		int numSplits = this.indexConf.getDocMakerThreadCount() * 5;
		logger.warn("[numSplits]==>" + numSplits);
		long goalSize = this.totalSize / (numSplits == 0 ? 1 : numSplits);
		logger.warn("[goalSize]==>" + goalSize);
		long minSize = this.indexConf.getMinSplitSize();
		logger.warn("[minSize]==>" + minSize);
		ArrayList<FileSplit> splits = new ArrayList<FileSplit>(numSplits);
		NetworkTopology clusterMap = new NetworkTopology();
		Path path;
		for (FileStatus file : files) {
			path = file.getPath();
			long length = file.getLen();
			BlockLocation[] blkLocations = this.fileSystem.getFileBlockLocations(file, 0L, length);
			if ((length != 0L) && (isSplitable(this.fileSystem, path))) {
				long blockSize = file.getBlockSize();
				long splitSize = computeSplitSize(goalSize, minSize, blockSize);
				logger.warn("[splitSize]==>" + splitSize);
				long bytesRemaining = length;
				while (bytesRemaining / splitSize > 1.1D) {
					String[] splitHosts = getSplitHosts(blkLocations, length - bytesRemaining, splitSize, clusterMap);
					splits.add(new FileSplit(path, length - bytesRemaining, splitSize, splitHosts));
					bytesRemaining -= splitSize;
				}
				if (bytesRemaining != 0L)
					splits.add(new FileSplit(path, length - bytesRemaining, bytesRemaining,
							blkLocations[(blkLocations.length - 1)].getHosts()));
			} else if (length != 0L) {
				String[] splitHosts = getSplitHosts(blkLocations, 0L, length, clusterMap);
				splits.add(new FileSplit(path, 0L, length, splitHosts));
			} else {
				splits.add(new FileSplit(path, 0L, length, new String[0]));
			}
		}
		int size = splits.size();
		logger.warn("[" + this.indexConf.getCollectionName() + "] 需要DUMP[" + this.totalSize / 1024L / 1024L
				+ "]MB的源数据-->索引数据，切分数据为: " + size + " 份");
		for (FileSplit split : splits) {
			logger.warn("split name=" + split.getPath() + ",offset=" + split.getStart());
		}
		return Collections.unmodifiableList(splits);
	}

	private String[] fakeRacks(BlockLocation[] blkLocations, int index) throws IOException {
		String[] allHosts = blkLocations[index].getHosts();
		String[] allTopos = new String[allHosts.length];
		for (int i = 0; i < allHosts.length; i++) {
			allTopos[i] = ("/default-rack/" + allHosts[i]);
		}
		return allTopos;
	}

	private String[] identifyHosts(int replicationFactor, Map<Node, NodeInfo> racksMap) {
		String[] retVal = new String[replicationFactor];
		List<NodeInfo> rackList = new LinkedList<NodeInfo>();
		rackList.addAll(racksMap.values());
		sortInDescendingOrder(rackList);
		boolean done = false;
		int index = 0;
		for (NodeInfo ni : rackList) {
			Set hostSet = ni.getLeaves();
			List<NodeInfo> hostList = new LinkedList<NodeInfo>();
			hostList.addAll(hostSet);
			sortInDescendingOrder(hostList);
			for (NodeInfo host : hostList) {
				retVal[(index++)] = host.node.getName().split(":")[0];
				if (index == replicationFactor) {
					done = true;
					break;
				}
			}
			if (done) {
				break;
			}
		}
		return retVal;
	}

	private void sortInDescendingOrder(List<NodeInfo> mylist) {
		Collections.sort(mylist, new Comparator<NodeInfo>() {

			public int compare(FileSplitor.NodeInfo obj1, FileSplitor.NodeInfo obj2) {
				if ((obj1 == null) || (obj2 == null)) {
					return -1;
				}
				if (obj1.getValue() == obj2.getValue()) {
					return 0;
				}
				return obj1.getValue() < obj2.getValue() ? 1 : -1;
			}
		});
	}

	protected int getBlockIndex(BlockLocation[] blkLocations, long offset) {
		for (int i = 0; i < blkLocations.length; i++) {
			if ((blkLocations[i].getOffset() <= offset)
					&& (offset < blkLocations[i].getOffset() + blkLocations[i].getLength())) {
				return i;
			}
		}
		BlockLocation last = blkLocations[(blkLocations.length - 1)];
		long fileLength = last.getOffset() + last.getLength() - 1L;
		throw new IllegalArgumentException("Offset " + offset + " is outside of file (0.." + fileLength + ")");
	}

	public FileSystem getFileSystem() {
		return this.fileSystem;
	}

	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public IndexConf getIndexConf() {
		return this.indexConf;
	}

	public void setIndexConf(IndexConf indexConf) {
		this.indexConf = indexConf;
	}

	protected String[] getSplitHosts(BlockLocation[] blkLocations, long offset, long splitSize,
			NetworkTopology clusterMap) throws IOException {
		int startIndex = getBlockIndex(blkLocations, offset);
		long bytesInThisBlock = blkLocations[startIndex].getOffset() + blkLocations[startIndex].getLength() - offset;
		if (bytesInThisBlock >= splitSize) {
			return blkLocations[startIndex].getHosts();
		}
		long bytesInFirstBlock = bytesInThisBlock;
		int index = startIndex + 1;
		splitSize -= bytesInThisBlock;
		while (splitSize > 0L) {
			bytesInThisBlock = Math.min(splitSize, blkLocations[(index++)].getLength());
			splitSize -= bytesInThisBlock;
		}
		long bytesInLastBlock = bytesInThisBlock;
		int endIndex = index - 1;
		Map hostsMap = new IdentityHashMap();
		Map racksMap = new IdentityHashMap();
		String[] allTopos = new String[0];
		for (index = startIndex; index <= endIndex; index++) {
			if (index == startIndex)
				bytesInThisBlock = bytesInFirstBlock;
			else if (index == endIndex)
				bytesInThisBlock = bytesInLastBlock;
			else {
				bytesInThisBlock = blkLocations[index].getLength();
			}
			allTopos = blkLocations[index].getTopologyPaths();
			if (allTopos.length == 0) {
				allTopos = fakeRacks(blkLocations, index);
			}
			for (String topo : allTopos) {
				Node node = clusterMap.getNode(topo);
				if (node == null) {
					node = new NodeBase(topo);
					clusterMap.add(node);
				}
				NodeInfo nodeInfo = (NodeInfo) hostsMap.get(node);
				NodeInfo parentNodeInfo = null;
				if (nodeInfo == null) {
					nodeInfo = new NodeInfo(node);
					hostsMap.put(node, nodeInfo);
					Node parentNode = node.getParent();
					parentNodeInfo = (NodeInfo) racksMap.get(parentNode);
					if (parentNodeInfo == null) {
						parentNodeInfo = new NodeInfo(parentNode);
						racksMap.put(parentNode, parentNodeInfo);
					}
					parentNodeInfo.addLeaf(nodeInfo);
				} else {
					nodeInfo = (NodeInfo) hostsMap.get(node);
					Node parentNode = node.getParent();
					parentNodeInfo = (NodeInfo) racksMap.get(parentNode);
				}
				nodeInfo.addValue(index, bytesInThisBlock);
				parentNodeInfo.addValue(index, bytesInThisBlock);
			}
		}
		return identifyHosts(allTopos.length, racksMap);
	}

	public long getTotalSize() {
		return this.totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	protected long computeSplitSize(long goalSize, long minSize, long blockSize) {
		if (goalSize / blockSize <= 4L) {
			return Math.max(minSize, Math.min(goalSize, blockSize));
		}
		return Math.max(minSize, Math.max(goalSize / 4L, blockSize));
	}

	protected boolean isSplitable(FileSystem fs, Path filename) {
		return true;
	}

	private static class NodeInfo {

		final Node node;

		final Set<Integer> blockIds;

		final Set<NodeInfo> leaves;

		private long value;

		NodeInfo(Node node) {
			this.node = node;
			this.blockIds = new HashSet();
			this.leaves = new HashSet();
		}

		long getValue() {
			return this.value;
		}

		void addValue(int blockIndex, long value) {
			if (this.blockIds.add(Integer.valueOf(blockIndex)))
				this.value += value;
		}

		Set<NodeInfo> getLeaves() {
			return this.leaves;
		}

		void addLeaf(NodeInfo nodeInfo) {
			this.leaves.add(nodeInfo);
		}
	}
}

package com.qlangtech.tis.indexbuilder.source.impl;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.indexbuilder.map.IndexConf;

/**
 * 执行所有历史文件的PT的全量构建,也就是所谓的小全量，索引构建 <br>
 * 业务方会传入这样的路径:/user/hive/warehouse/olap_member_marketing.db/cust_mgt_label_new_new/pt=20(\d{6})/pmod=0
 * <br>
 * 
 * @author 百岁（baisui）
 * @date 2019年9月16日
 */
public class AllHistoryPtFileSplitor extends DefaultFileSplitor {
	private static final Logger logger = LoggerFactory.getLogger(DefaultFileSplitor.class);

	public AllHistoryPtFileSplitor(IndexConf indexConf, FileSystem fileSystem) {
		super(indexConf, fileSystem);
	}

	@Override
	protected void getFiles(String paramPath, List<FileStatus> paramList) throws Exception {
		if (notContainPattern(paramPath)) {
			throw new IllegalStateException("param path:" + paramPath + " shall contain Pattern");
		}

		final String[] pathSplit = StringUtils.split(paramPath, Path.SEPARATOR);
		final StringBuffer pathParent = new StringBuffer();
		final StringBuffer pathChild = new StringBuffer();
		boolean parentCreated = false;
		for (int i = 0; i < pathSplit.length; i++) {
			if (!parentCreated) {
				if (notContainPattern(pathSplit[i])) {
					pathParent.append(Path.SEPARATOR).append(pathSplit[i]);
				} else {
					parentCreated = true;
				}
			} else {
				pathChild.append(Path.SEPARATOR).append(pathSplit[i]);
			}
		}

		final Pattern childPathPattern = Pattern.compile(pathChild.toString());
		logger.info("\nparamPath:{}\n,parent:{}\n,childpattern:{}", paramPath, pathParent.toString(), childPathPattern);
		Path path = new Path(pathParent.toString());
		this.getFiles(path, paramList, (r) -> {
			return childPathPattern.matcher(r.getParent().toString()).find();
		});

	}

	/**
	 * 路径中不存在正则式
	 * 
	 * @param paramPath
	 * @return
	 */
	public static boolean notContainPattern(String paramPath) {
		return StringUtils.indexOf(paramPath, "(") < 0 && StringUtils.indexOf(paramPath, ")") < 0;
	}

}

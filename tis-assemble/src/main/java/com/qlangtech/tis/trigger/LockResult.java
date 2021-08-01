/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */


package com.qlangtech.tis.trigger;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.data.Stat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 百岁（baisui@2dfire.com）
 *
 * @date 2016年4月26日
 */
public class LockResult {
	private String zkAddress;
	private String path;
	private String content;
	public Stat stat;
	// 节点描述信息
	private String desc;

	public final List<String> childValus = new ArrayList<String>();

	private final boolean editable;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public LockResult(boolean editable) {
		super();
		this.editable = editable;
	}

	public void addChildValue(String value) {
		this.childValus.add(value);
	}

	public boolean isEditable() {
		return this.editable;
	}

	private static final ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy/MM/dd HH:mm");
		}

	};

	public String getCreateTime() {
		if (stat == null) {
			return StringUtils.EMPTY;
		}
		return format.get().format(new Date(stat.getCtime()));
		// return ManageUtils.formatDateYYYYMMdd();
	}

	public String getUpdateTime() {
		if (stat == null) {
			return StringUtils.EMPTY;
		}
		return format.get().format(new Date(stat.getMtime()));
		// return ManageUtils.formatDateYYYYMMdd(new Date(stat.getMtime()));
	}

	public String getZkAddress() {
		return zkAddress;
	}

	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}

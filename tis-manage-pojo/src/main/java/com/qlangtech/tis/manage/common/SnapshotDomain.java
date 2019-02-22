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

import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SnapshotDomain {

	private UploadResource solrSchema = new UploadResource();

	private UploadResource solrConfig = new UploadResource();

	private final Snapshot snapshot;

	public SnapshotDomain() {
		super();
		snapshot = new Snapshot();
	}

	public SnapshotDomain(Snapshot snapshot) {
		super();
		if (snapshot == null) {
			throw new IllegalArgumentException("snapshot can not be null");
		}
		this.snapshot = snapshot;
	}

	public Integer getAppId() {
		return snapshot.getAppId();
	}

	public Snapshot getSnapshot() {
		if (this.snapshot == null) {
			throw new NullPointerException("this.snapshot can not be null");
		}
		return snapshot;
	}

	public void setSolrSchema(UploadResource solrSchema) {
		this.solrSchema = solrSchema;
	}

	public void setSolrConfig(UploadResource solrConfig) {
		this.solrConfig = solrConfig;
	}

	public UploadResource getSolrSchema() {
		return solrSchema;
	}

	public UploadResource getSolrConfig() {
		return solrConfig;
	}
}

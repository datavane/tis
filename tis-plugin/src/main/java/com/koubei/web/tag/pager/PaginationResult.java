/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.koubei.web.tag.pager;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * 分页查询的响应体：行数据 + 分页元信息一起回，前端一次请求就能把表格与分页条都画出来。
 *
 * <p>本类原先内嵌在 tis-console 的 {@code com.qlangtech.tis.runtime.module.action.BasicModule} 里，
 * 现下沉到 tis-plugin（挨着 {@link Pager}）：它的 4 个元信息 getter 全部委托给 {@link Pager}，
 * 两者是同一个「分页」概念的控件面与响应面，放在一起才能让插件侧
 * （{@code ObjectTableWidget} 的 {@code get_data}）复用同一份形状定义。
 *
 * <p>⚠️ {@link #pager} 上的 {@code @JSONField(serialize = false)} 是**安全绳**，不是装饰：
 * 响应经 {@code AjaxValve} → {@code JsonUtil.toString} → fastjson 的 {@code JSON.toJSONString}
 * 序列化，这个注解拦住了 {@link Pager} 进 JSON。若不生效（例如哪天整条链路换成 Jackson），
 * fastjson/Jackson 会去读 {@code getLink()}，而它在总页数 &gt; 1 时会走到
 * {@code linkBuilder.getPageUrl(...)} —— 插件侧传的 {@code LinkBuilder} 是 {@code null}，直接 NPE。
 *
 * @author 百岁（baisui@qlangtech.com）
 */
public class PaginationResult {

  @JSONField(serialize = false)
  private final Pager pager;

  private final List<?> rows;

  private Object[] payload = new Object[0];

  /**
   * @param pager   分页器（已经 setTotalCount / setCurPage / setRowsPerPage 过）
   * @param rows    本页行数据
   * @param payload 可以附带一些参数
   */
  public PaginationResult(Pager pager, List<?> rows, Object... payload) {
    super();
    this.pager = pager;
    this.rows = rows;
    this.payload = payload;
  }

  public Object[] getPayload() {
    return this.payload;
  }

  public int getTotalPage() {
    return this.pager.getTotalPage();
  }

  public int getCurPage() {
    return this.pager.getCurPage();
  }

  public int getTotalCount() {
    return this.pager.getTotalCount();
  }

  public int getPageSize() {
    return this.pager.getRowsPerPage();
  }

  public List<?> getRows() {
    return rows;
  }
}

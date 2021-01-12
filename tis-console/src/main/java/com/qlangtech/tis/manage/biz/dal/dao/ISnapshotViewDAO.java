/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.dao;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.SnapshotDomain;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-27
 */
public interface ISnapshotViewDAO {

  String KEY_MIN_GRAM_SIZE = "minGramSize";
  String KEY_MAX_GRAM_SIZE = "maxGramSize";

  default List<String> getOptionParamKeys() {
    return Lists.newArrayList(KEY_MIN_GRAM_SIZE, KEY_MAX_GRAM_SIZE);
  }

  SnapshotDomain getView(Integer snId, boolean mergeContextParams);

  default SnapshotDomain getView(Integer snId) {
    return getView(snId, true);
  }
}

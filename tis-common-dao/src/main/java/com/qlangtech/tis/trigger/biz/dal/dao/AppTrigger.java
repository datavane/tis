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

/**
 *
 */
package com.qlangtech.tis.trigger.biz.dal.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@taobao.com）
 * @date 2012-7-31
 */
public class AppTrigger {
  private final TriggerJob fullTrigger;
  private final TriggerJob incTrigger;

  public AppTrigger(
    TriggerJob fullTrigger,
    TriggerJob incTrigger) {
    super();
    this.fullTrigger = fullTrigger;
    this.incTrigger = incTrigger;
  }

  public TriggerJob getFullTrigger() {
    return fullTrigger;
  }

  public TriggerJob getIncTrigger() {
    return incTrigger;
  }

  /**
   * dump是否是停止的状态
   *
   * @return
   */
  public boolean isPause() {
    if (fullTrigger != null && !fullTrigger.isStop()) {
      return false;
    }

    if (incTrigger != null && !incTrigger.isStop()) {
      return false;
    }

    return true;
  }

  public List<Long> getJobsId() {
    final List<Long> jobs = new ArrayList<Long>();
    if (this.getFullTrigger() != null) {
      jobs.add(this.getFullTrigger().getJobId());
    }

    if (this.getIncTrigger() != null) {
      jobs.add(this.getIncTrigger().getJobId());
    }
    return jobs;
  }

}

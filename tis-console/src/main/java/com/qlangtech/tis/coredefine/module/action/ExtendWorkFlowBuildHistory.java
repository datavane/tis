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
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年7月29日
 */
public class ExtendWorkFlowBuildHistory {

  private final WorkFlowBuildHistory delegate;

  public ExtendWorkFlowBuildHistory(WorkFlowBuildHistory delegate) {
    super();
    this.delegate = delegate;
  }

  public Integer getId() {
    return delegate.getId();
  }

  public Date getStartTime() {
    return delegate.getStartTime();
  }

  @JSONField(serialize = false)
  public WorkFlowBuildHistory getDelegate() {
    return this.delegate;
  }

  public static void main(String[] args) {
    SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    long t1 = 1599460480838l;
    long t2 = 1599450043000l;
    System.out.println(f.format(new Date(t1)));
    System.out.println(f.format(new Date(t2)));
    long diff = t1 - t2;
    System.out.println(diff / (1000 * 60 * 60) + "小时");
    System.out.println((diff / 1000) % (60) + "秒");
    System.out.println((diff / (1000 * 60)) % (60) + "分");
    Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    System.out.println(gmt.getTimeInMillis());
    gmt = Calendar.getInstance();
    System.out.println(gmt.getTimeInMillis());
    // ExtendWorkFlowBuildHistory test = new ExtendWorkFlowBuildHistory(null);
    // System.out.println(test.getNow());
  }

  /**
   * 耗时
   *
   * @return
   */
  public String getConsuming() {
    ExecResult result = ExecResult.parse(this.delegate.getState());
    Date endTime = ((result == ExecResult.FAILD || result == ExecResult.SUCCESS) && this.getEndTime() != null) ? this.getEndTime() : new Date();
    int consuming = (int) ((endTime.getTime() - this.getStartTime().getTime()) / 1000);
    if (consuming < 60) {
      return consuming + "秒";
    } else {
      return (consuming / 60) + "分钟";
    }
  }

  public Date getEndTime() {
    return delegate.getEndTime();
  }

  public String getStateClass() {
    // SUCCESS(1, "成功"), FAILD(-1, "失败"), DOING(2, "执行中"), CANCEL(3, "终止");
    ExecResult result = ExecResult.parse(this.delegate.getState());
    switch (result) {
      case DOING:
      case ASYN_DOING:
        return "fa fa-cog fa-spin";
      case SUCCESS:
        return "fa fa-check";
      case FAILD:
        return "fa fa-times";
      case CANCEL:
        // 取消了
        return "fa fa-cancel";
      default:
        throw new IllegalStateException("result:" + result + " status is illegal");
    }
  }

  public String getStateColor() {
    // [ngStyle]="{'max-width.px': widthExp}"
    ExecResult result = ExecResult.parse(this.delegate.getState());
    switch (result) {
      case DOING:
      case ASYN_DOING:
        return "blue";
      case SUCCESS:
        return "green";
      case FAILD:
        return "red";
      case CANCEL:
        // 取消了
        return "red";
      default:
        throw new IllegalStateException("result:" + result + " status is illegal");
    }
  }

  public int getState() {
    return delegate.getState();
  }

  /**
   * 取得执状态
   *
   * @return
   */
  public String getLiteralState() {
    return ExecResult.parse(this.delegate.getState()).getLiteral();
  }

  public String getTriggerType() {
    return TriggerType.parse(delegate.getTriggerType()).getLiteral();
  }

  public Integer getOpUserId() {
    return delegate.getOpUserId();
  }

  public String getOpUserName() {
    return delegate.getOpUserName();
  }

  public Integer getAppId() {
    return delegate.getAppId();
  }

  public String getAppName() {
    return delegate.getAppName();
  }

  public String getStartPhase() {
    return FullbuildPhase.parse(delegate.getStartPhase()).getLiteral();
  }

  public String getEndPhase() {
    return FullbuildPhase.parse(delegate.getEndPhase()).getLiteral();
  }

  public Integer getHistoryId() {
    return delegate.getHistoryId();
  }

  public Integer getWorkFlowId() {
    return delegate.getWorkFlowId();
  }

  public long getCreateTime() {
    return delegate.getCreateTime().getTime();
  }

  public Date getOpTime() {
    return delegate.getOpTime();
  }
}

/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年5月31日
 */
public class ClusterSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date fromTime;

    private Date toTime;

    private Long requestCount;

    private Long updateCount;

    public ThreadLocal<SimpleDateFormat> dateformat;

    @JSONField(serialize = false)
    public Date getFromTime() {
        return this.fromTime;
    }

    public String getLabel() {
        if (this.dateformat == null) {
            throw new IllegalStateException("dateformat can not be null");
        }
        return this.dateformat.get().format(this.getToTime());
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    @JSONField(serialize = false)
    public Date getToTime() {
        return this.toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public Long getRequestCount() {
        return this.requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public Long getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Long updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * 统计一段时间内的指标总量
     */
    public static class Summary {

        private long updateCount;

        private long queryCount;

        public Summary() {
        }

        public void add(ClusterSnapshot snapshot) {
            this.updateCount += snapshot.updateCount;
            this.queryCount += snapshot.requestCount;
        }

        public long getUpdateCount() {
            return updateCount;
        }

        public long getQueryCount() {
            return queryCount;
        }
    }
}

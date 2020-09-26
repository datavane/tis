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
package com.qlangtech.tis.hdfs.client.time;

import java.util.Date;
import com.qlangtech.tis.exception.TimeManageException;

/**
 * @description
 * @since 2011-8-11 04:27:31
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface TimeProvider {

    public void reSetEndTime();

    public StartAndEndTime readTimeFormFile() throws TimeManageException;

    public void reWriteTimeToFile() throws TimeManageException;

    public StartAndEndTime justGetTimes() throws TimeManageException;

    public class StartAndEndTime {

        public StartAndEndTime(Date startTime, Date endTime) {
            this.endTime = endTime;
            this.startTime = startTime;
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("startTime:" + startTime.toString() + "\n");
            s.append("endTime:" + endTime.toString() + "\n");
            return s.toString();
        }

        public Date endTime;

        public Date startTime;
    }
}

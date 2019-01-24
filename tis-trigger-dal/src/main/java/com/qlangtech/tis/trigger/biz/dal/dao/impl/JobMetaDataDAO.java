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
package com.qlangtech.tis.trigger.biz.dal.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import junit.framework.Assert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import com.qlangtech.tis.trigger.biz.dal.dao.IJobMetaDataDAO;
import com.qlangtech.tis.trigger.biz.dal.dao.JobConstant;
import com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.AppTrigger;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JobMetaDataDAO extends JdbcDaoSupport implements IJobMetaDataDAO {

    // 
    // public static final int FULL_DUMP = 1;
    // public static final int INCR_DUMP = 2;
    private static final String SQL_SELECT_APP = "" + " select aa.job_id,aa.job_type,aa.is_stop " + " from app_trigger_job_relation aa inner join application bb on (aa.app_id = bb.app_id)" + " where bb.project_name = ? ";

    public static final String SQL_UPDATE_SET_STOP = "update app_trigger_job_relation set is_stop = ? where project_name= ?";

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.trigger.biz.dal.dao.IJobMetaDataDAO#queryJob(java
	 * .lang.String, java.lang.Integer)
	 */
    public TriggerJob queryJob(String appName, Integer jobtype) {
        // DataSource datasource = this.getDataSource();
        PreparedStatement statement = null;
        ResultSet result = null;
        TriggerJob job = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            statement = conn.prepareStatement(SQL_SELECT_APP + " and job_type= ?");
            statement.setString(1, appName);
            statement.setInt(2, jobtype);
            result = statement.executeQuery();
            if (result.next()) {
                job = new TriggerJob();
                job.setJobId(result.getLong(1));
                job.setJobType(result.getInt(2));
                job.setStop(JobConstant.STOPED.equals(result.getString(3)));
                return job;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                result.close();
            } catch (Throwable e) {
            }
            try {
                statement.close();
            } catch (Throwable e) {
            }
            try {
                conn.close();
            } catch (Throwable e) {
            }
        }
        return null;
    }

    /**
     * 暂停一下
     */
    public void setStop(String appName, boolean stop) {
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            statement = conn.prepareStatement(SQL_UPDATE_SET_STOP);
            statement.setString(1, stop ? JobConstant.STOPED : JobConstant.STOPED_NOT);
            statement.setString(2, appName);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                statement.close();
            } catch (Throwable e) {
            }
            try {
                conn.close();
            } catch (Throwable e) {
            }
        }
    }

    public AppTrigger queryJob(String appName) {
        PreparedStatement statement = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            statement = conn.prepareStatement(SQL_SELECT_APP);
            statement.setString(1, appName);
            result = statement.executeQuery();
            TriggerJob full = null;
            TriggerJob incr = null;
            while (result.next()) {
                if (JobConstant.JOB_TYPE_FULL_DUMP == result.getInt(2)) {
                    full = new TriggerJob();
                    full.setJobId(result.getLong(1));
                    full.setJobType(JobConstant.JOB_TYPE_FULL_DUMP);
                    full.setStop(JobConstant.STOPED.equals(result.getString(3)));
                } else if (JobConstant.JOB_INCREASE_DUMP == result.getInt(2)) {
                    incr = new TriggerJob();
                    incr.setJobId(result.getLong(1));
                    incr.setJobType(JobConstant.JOB_INCREASE_DUMP);
                    incr.setStop(JobConstant.STOPED.equals(result.getString(3)));
                }
            }
            Assert.assertNotNull(full);
            Assert.assertNotNull(incr);
            return new AppTrigger(full, incr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                result.close();
            } catch (Throwable e) {
            }
            try {
                statement.close();
            } catch (Throwable e) {
            }
            try {
                conn.close();
            } catch (Throwable e) {
            }
        }
    }
}

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
package com.qlangtech.tis.runtime.module.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.biz.dal.dao.IClusterSnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshotQuery;

/*
 * 用户查询访问提供不同时段的报表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ClusterStateCollectAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private IClusterSnapshotDAO clusterSnapshotDAO;

    private static final Map<Integer, IClusterSnapshotQueryGetter> queryTimeSpan = new HashMap<Integer, IClusterSnapshotQueryGetter>();

    public static final ThreadLocal<SimpleDateFormat> dateformat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd HH:mm");
        }
    };

    private static final int ONE_HOUR = 60;

    static {
        queryTimeSpan.put(ONE_HOUR, new IClusterSnapshotQueryGetter() {

            @Override
            public ClusterSnapshotQuery create() {
                return ClusterSnapshotQuery.hour();
            }
        });
        queryTimeSpan.put(300, new IClusterSnapshotQueryGetter() {

            @Override
            public ClusterSnapshotQuery create() {
                return ClusterSnapshotQuery.fiveHour();
            }
        });
        queryTimeSpan.put(1440, new IClusterSnapshotQueryGetter() {

            @Override
            public ClusterSnapshotQuery create() {
                return ClusterSnapshotQuery.hour24();
            }
        });
        queryTimeSpan.put(7200, new IClusterSnapshotQueryGetter() {

            @Override
            public ClusterSnapshotQuery create() {
                return ClusterSnapshotQuery.days15();
            }
        });
        queryTimeSpan.put(43200, new IClusterSnapshotQueryGetter() {

            @Override
            public ClusterSnapshotQuery create() {
                return ClusterSnapshotQuery.last1Month();
            }
        });
    }

    /**
     * 收集集群状态信息
     *
     * @param context
     * @throws Exception
     */
    public void doCollect(Context context) throws Exception {
        Integer minute = this.getInt("m");
        Assert.assertNotNull(minute);
        final StatusCollectStrategy collectStrategy = getCollectStrategy(minute);
        this.setBizResult(context, collectStrategy.getSnapshots());
    }

    private StatusCollectStrategy getCollectStrategy(final int minute) {
        return new StatusCollectStrategy() {

            @Override
            public String getFormat(Date date) {
                return dateformat.get().format(date);
            }

            @Override
            public List<ClusterSnapshot> getSnapshots() {
                IClusterSnapshotQueryGetter getter = queryTimeSpan.get(minute);
                Assert.assertNotNull("time getter:" + minute + " can not be null", getter);
                final ClusterSnapshotQuery timeSpanQuery = getter.create();
                Assert.assertNotNull("time span:" + minute + " can not be null", timeSpanQuery);
                timeSpanQuery.setAppId(getAppDomain().getAppid());
                return clusterSnapshotDAO.reportClusterStatus(timeSpanQuery);
            }
        };
    }

    private static interface StatusCollectStrategy {

        public List<ClusterSnapshot> getSnapshots();

        public String getFormat(Date date);
    }

    public static class PonitStatus {

        private String createTime;

        private String serviceName;

        private int qps;

        private long requestCount;

        private long docNumber;

        private float avgConsumeTimePerRequest;

        public float getAvgConsumeTimePerRequest() {
            return avgConsumeTimePerRequest;
        }

        public String getCreateTime() {
            return createTime;
        }

        public String getServiceName() {
            return serviceName;
        }

        public int getQps() {
            return qps;
        }

        public long getRequestCount() {
            return requestCount;
        }

        public long getDocNumber() {
            return docNumber;
        }
    }

    interface IClusterSnapshotQueryGetter {

        ClusterSnapshotQuery create();
    }

    public IClusterSnapshotDAO getClusterSnapshotDAO() {
        return clusterSnapshotDAO;
    }

    @Autowired
    public void setClusterSnapshotDAO(IClusterSnapshotDAO clusterSnapshotDAO) {
        this.clusterSnapshotDAO = clusterSnapshotDAO;
    }
}

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
package com.qlangtech.tis.config.module.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.common.apps.AdminAppsFetcher;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.trigger.zk.AbstractWatcher;

/*
 * 显示所有有效的全量触发器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CrontabListAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CrontabListAction.class);

    // 集群中已存活的索引名称
    private static List<String> colls;

    public void doGetList(Context context) throws Exception {
        List<TriggerCrontab> crontablist = getAllTriggerCrontab(this);
        JSONArray result = new JSONArray();
        JSONObject o = null;
        for (TriggerCrontab tab : crontablist) {
            o = new JSONObject();
            o.put("name", tab.getAppName());
            o.put("fulldump", tab.getFcrontab());
            o.put("fulljobid", tab.getFjobId());
            result.put(o);
        }
        // 
        context.put("query_result", result.toString(1));
    }

    /**
     * @return
     */
    public static List<TriggerCrontab> getAllTriggerCrontab(final RunContext context) {
        if (colls == null) {
            synchronized (CrontabListAction.class) {
                if (colls == null) {
                    colls = new ArrayList<String>();
                    setIndexCollectionName(context);
                    context.getSolrZkClient().addOnReconnect(new OnReconnect() {

                        @Override
                        public void command() {
                            setIndexCollectionName(context);
                        }
                    });
                }
            }
        }
        List<TriggerCrontab> crontablist = AdminAppsFetcher.getAllTriggerTabs(context.getUsrDptRelationDAO());
        TriggerCrontab next = null;
        Iterator<TriggerCrontab> cronIt = crontablist.iterator();
        while (cronIt.hasNext()) {
            next = cronIt.next();
            if (!colls.contains(next.getAppName()) || next.isFstop()) {
                cronIt.remove();
            }
        }
        return crontablist;
    }

    // public static IAppsFetcher getAppsFetcher(HttpServletRequest request,
    // boolean maxMatch, IUser user, RunContext context) {
    // 
    // if (maxMatch) {
    // return AppsFetcher.create(user, context, true);
    // }
    // 
    // return UserUtils.getAppsFetcher(request, context);
    // }
    /**
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void setIndexCollectionName(final RunContext context) {
        try {
            colls.addAll(context.getSolrZkClient().getChildren(ZkStateReader.COLLECTIONS_ZKNODE, new AbstractWatcher() {

                @Override
                protected void process(Watcher watcher) throws KeeperException, InterruptedException {
                    synchronized (CrontabListAction.class) {
                        log.info("receive a new rewatch colls event");
                        setIndexCollectionName(context);
                    }
                }
            }, true));
            log.info("colls:{}", colls);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
        }
    }
}

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
package com.qlangtech.tis.config.module.action;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.common.apps.TerminatorAdminAppsFetcher;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.trigger.zk.AbstractWatcher;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 显示所有有效的全量触发器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年10月28日下午1:33:02
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
          context.getSolrZkClient().addOnReconnect(() -> {
            setIndexCollectionName(context);
          });
        }
      }
    }
    List<TriggerCrontab> crontablist = TerminatorAdminAppsFetcher.getAllTriggerTabs(context.getUsrDptRelationDAO());
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
          // TisTriggerJobManage.setAppAndRuntime();
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

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
package com.qlangtech.tis.order.dump.task;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.db.parser.domain.DBConfig;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.client.bean.TISDumpClient;
import com.qlangtech.tis.hdfs.client.bean.TISDumpClient.TriggerParamProcess;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.manage.common.SpringDBRegister;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.trigger.util.TriggerParam;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月29日 上午11:46:56
 */
public abstract class AbstractTableDumpTask implements TaskMapper {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTableDumpTask.class);

    private static final ExecutorService threadPool = java.util.concurrent.Executors.newCachedThreadPool();

    // private static final Pattern DB_HOST_ENUM =
    // Pattern.compile("db\\.(.+?)\\.enum");
    public ClassPathXmlApplicationContext springContext;

    protected final TableDumpFactory tableDumpFactory;

    private TISDumpClient dumpbeans;

    public int getAllTableDumpRows() {
        Objects.requireNonNull(this.dumpbeans, "dumpBean can not be null");
        return getDumpContext().getAllTableDumpRows().get();
    }

    public TSearcherDumpContext getDumpContext() {
        Objects.requireNonNull(this.dumpbeans, "dumpBean can not be null");
        return this.dumpbeans.getDumpContext();
    }

    public AbstractTableDumpTask(TableDumpFactory tableDumpFactory) {
        super();
        this.tableDumpFactory = tableDumpFactory;
    }

    protected abstract DBConfig getDataSourceConfig();

    protected final DBConfig parseDbLinkMetaData(TaskContext context) {
        final DBConfig dbLinkMetaData = getDataSourceConfig();
        logger.info("dbLinkMetaData:" + dbLinkMetaData.toString());
        return dbLinkMetaData;
    }

    // protected abstract List<DBLinkMetaData> parseDbLinkMetaData(
    // TaskContext context);
    protected abstract TISDumpClient getDumpBeans(TaskContext context) throws Exception;

    /**
     * 在容器中注册额外的bean,比如大量的datasource
     *
     * @param factory
     */
    protected abstract void registerExtraBeanDefinition(DefaultListableBeanFactory factory);

    // @Override
    @SuppressWarnings("all")
    public void map(TaskContext context) {
        // Map<String, DumpResult> /* 索引名称 */          dumpResultMap = new HashMap<>();
        Objects.requireNonNull(tableDumpFactory, "fs2Table has not be initial");
        logger.info("static initialize start");
        initialSpringContext(context);
        logger.info("static initialize success");
        // StatusRpcClient rpcClient = new StatusRpcClient();
        try {
            final String startTime = context.get(ITableDumpConstant.DUMP_START_TIME);
            logger.info("dump startTime:" + startTime);
            final boolean force = getParamForce(context);
            // leader 选举代码
            // https://git-wip-us.apache.org/repos/asf?p=curator.git;a=blob;f=curator-examples/src/main/java/leader/LeaderSelectorExample.java;h=85f0598a62537952f072db6bdb5c16f049bab38f;hb=HEAD
            // ExecutorCompletionService<DumpResult> executeService = new ExecutorCompletionService(threadPool);
            // AtomicBoolean joinTableClear = new AtomicBoolean(false);
            // 取得所有的dump bean 以表为单位
            this.dumpbeans = getDumpBeans(context);
            if (dumpbeans == null) {
                throw new IllegalStateException("dumpbeans list size can not small than 1");
            }
            // AtomicInteger dumpJobCount = new AtomicInteger();
            tableDumpFactory.startTask((connContext) -> {
                // for (final TISDumpClient dumpBean : dumpbeans) {
                // executeService.submit(new Callable<DumpResult>() {
                // 
                // @Override
                // public DumpResult call() throws Exception {
                // DumpResult dumpResult = new DumpResult();
                // try {
                dumpbeans.executeDumpTask(false, force, new TriggerParamProcess() {

                    @Override
                    public void callback(TriggerParam param) {
                        return;
                    }
                }, startTime, connContext);
            // dumpResult.dumpTable = dumpbeans.getDumpContext().getDumpTable();
            // } catch (Exception e) {
            // dumpResult.error = e;
            // }
            // return dumpResult;
            // }
            // });
            // dumpJobCount.incrementAndGet();
            // }
            // DumpResult dumpResult = null;
            // for (int i = 0; i < dumpJobCount.get(); i++) {
            // dumpResult = executeService.take().get();
            // if (!dumpResult.isSuccess()) {
            // // 失败了
            // if (dumpResult.error != null) {
            // throw dumpResult.error;
            // } else {
            // throw new DumpResultException(dumpResult);
            // }
            // }
            // logger.info("dump job:" + dumpResult.dumpTable + " complete!!!");
            // dumpResultMap.put(dumpResult.dumpTable.toString(), dumpResult);
            // }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // return new TaskReturn(ReturnCode.FAILURE, "all table dump faild");
            throw new RuntimeException(e);
        } finally {
        // try {
        // if (hiveConnection != null) {
        // hiveConnection.close();
        // }
        // } catch (Exception e) {
        // 
        // }
        }
    // TaskReturn taskResult;
    // try {
    // JSONObject dumpResultDesc = createDumpResultDesc(dumpResultMap);
    // taskResult = new TaskReturn(ReturnCode.SUCCESS, dumpResultDesc.toString(1));
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // return taskResult;
    }

    /**
     * 当历史数据已经存在，是否还要再导入一份数据？
     *
     * @param context
     * @return
     */
    private boolean getParamForce(TaskContext context) {
        boolean force = false;
        try {
            force = Boolean.parseBoolean(context.get(ITableDumpConstant.DUMP_FORCE));
        } catch (Throwable e) {
        }
        return force;
    }

    public void initialSpringContext(TaskContext context) {
        final DBConfig dbLinkMetaData = parseDbLinkMetaData(context);
        springContext = new ClassPathXmlApplicationContext("dump-app-context.xml", this.getClass()) {

            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
                // DataSourceRegister.setApplicationContext(factory,
                // dbMetaList);
                SpringDBRegister dbRegister = new SpringDBRegister(dbLinkMetaData.getName(), dbLinkMetaData, factory);
                dbRegister.visitAll();
                registerExtraBeanDefinition(factory);
                super.prepareBeanFactory(beanFactory);
            }
        };
    }

    /**
     * @param dumpResultMap
     * @return
     */
    private JSONObject createDumpResultDesc(Map<String, DumpResult> dumpResultMap) {
        try {
            JSONObject dumpResultDesc = new JSONObject();
            JSONArray importTabs = new JSONArray();
            for (String indexName : dumpResultMap.keySet()) {
                importTabs.put(indexName);
            }
            dumpResultDesc.put("tabs", importTabs);
            return dumpResultDesc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class DumpResult {

        private EntityName dumpTable;

        private Exception error;

        public void setDumpTable(EntityName dumpTable) {
            this.dumpTable = dumpTable;
        }

        boolean isSuccess() {
            return // serviceConfig != null &&
            error == null;
        }
    }

    public static class DumpResultException extends Exception {

        private final DumpResult dumpResult;

        public DumpResultException(DumpResult dumpResult) {
            super(String.valueOf(dumpResult.dumpTable));
            this.dumpResult = dumpResult;
        }
    }
}

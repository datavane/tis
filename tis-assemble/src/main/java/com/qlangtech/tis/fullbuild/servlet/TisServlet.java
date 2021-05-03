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
package com.qlangtech.tis.fullbuild.servlet;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor.NewTaskParam;
import com.qlangtech.tis.exec.impl.WorkflowDumpAndJoinInterceptor;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.servlet.impl.HttpExecContext;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.util.HeteroEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 触发全量索引构建任务<br>
 * 例子： curl
 * 'http://localhost:8080/trigger?appname=search4totalpay&component.start=
 * tableJoin&ps=20160622110738'<br>
 * curl 'http://localhost:8080/trigger?component.start=indexBackflow&ps=
 * 20160623001000&appname=search4_fat_instance' <br>
 * curl 'http://localhost:8080/tis-assemble/trigger?component.start=indexBuild&ps=20200525134425&appname=search4totalpay&workflow_id=45&workflow_name=totalpay&index_shard_count=1&history.task.id=1'
 * <br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年11月6日 下午1:32:24
 */
public class TisServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(TisServlet.class);

    private static final long serialVersionUID = 1L;

    private IndexSwapTaskflowLauncher indexSwapTaskflowLauncher;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.indexSwapTaskflowLauncher = IndexSwapTaskflowLauncher.getIndexSwapTaskflowLauncher(config.getServletContext());
        ComponentMeta assembleComponent = TIS.getAssembleComponent();
        assembleComponent.synchronizePluginsFromRemoteRepository();
        log.info("synchronize Plugins FromRemoteRepository success");
    }

    private static final ExecutorService executeService = Executors.newCachedThreadPool(new ThreadFactory() {

        int index = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("triggerTask#" + (index++));
            t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });
            return t;
        }
    });

    // private static final AtomicBoolean idle = new AtomicBoolean(true);
    private static final Map<String, ExecuteLock> idles = new HashMap<String, ExecuteLock>();

    // public TisServlet() {
    // super();
    // }

    /**
     * 校验参数是否正确
     *
     * @param execContext
     * @param req
     * @param res
     * @return
     * @throws ServletException
     */
    protected boolean isValidParams(HttpExecContext execContext, HttpServletRequest req, HttpServletResponse res) throws ServletException {
        return true;
    }

    protected boolean shallValidateCollectionExist() {
        return true;
    }

    protected final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final HttpExecContext execContext = createHttpExecContext(req);
        final MDCParamContext mdcContext = this.getMDCParam(execContext, res);
        try {
            if (!isValidParams(execContext, req, res)) {
                return;
            }
            if (!mdcContext.validateParam()) {
                return;
            }
            final ExecuteLock lock = mdcContext.getExecLock();
            // getLog().info("start to execute index swap work flow");
            final CountDownLatch countDown = new CountDownLatch(1);
            // final Future<?> future =
            lock.futureQueue.add(executeService.submit(() -> {
                // MDC.put("app", indexName);
                getLog().info("index swap start to work");
                try {
                    while (true) {
                        try {
                            if (lock.lock()) {
                                DefaultChainContext chainContext = new DefaultChainContext(execContext);
                                chainContext.setMdcParamContext(mdcContext);
                                final Integer newTaskId = createNewTask(chainContext);
                                try {
                                    String msg = "trigger task" + mdcContext.getExecLockKey() + " successful";
                                    getLog().info(msg);
                                    mdcContext.resetParam(newTaskId);
                                    writeResult(true, msg, res, new KV(IExecChainContext.KEY_TASK_ID, String.valueOf(newTaskId)));
                                    IndexBuilderTriggerFactory builderFactory = HeteroEnum.INDEX_BUILD_CONTAINER.getPlugin();
                                    Objects.requireNonNull(builderFactory, "builderFactory can not be null");
                                    // chainContext.setIndexBuildFileSystem(builderFactory.getFsFactory());

                                    PluginStore<TableDumpFactory> tableDumpFactory = TIS.getPluginStore(TableDumpFactory.class);
                                    Objects.requireNonNull(tableDumpFactory.getPlugin(), "tableDumpFactory can not be null");
                                    chainContext.setTableDumpFactory(tableDumpFactory.getPlugin());
                                    chainContext.setIndexBuilderTriggerFactory(builderFactory);
                                    //   chainContext.setTopology(SqlTaskNodeMeta.getSqlDataFlowTopology(chainContext.getWorkflowName()));
                                    countDown.countDown();
                                    // 开始执行内部任务
                                    TrackableExecuteInterceptor.createTaskComplete(newTaskId, startWork(chainContext).isSuccess() ? ExecResult.SUCCESS : ExecResult.FAILD);
                                } catch (Throwable e) {
                                    TrackableExecuteInterceptor.createTaskComplete(newTaskId, ExecResult.FAILD);
                                    getLog().error(e.getMessage(), e);
                                    throw new RuntimeException(e);
                                } finally {
                                    lock.unlock();
                                    lock.futureQueue.clear();
                                }
                            } else {
                                if (lock.isExpire()) {
                                    getLog().warn("this lock has expire,this lock will cancel");
                                    // 执行已經超時
                                    lock.futureQueue.clear();
                                    lock.unlock();
                                    // while (lock.futureQueue.size() >= 1)
                                    // {
                                    // lock.futureQueue.poll().cancel(true);
                                    // }
                                    getLog().warn("this lock[" + lock.getTaskOwnerUniqueName() + "] has expire,has unlocked");
                                    continue;
                                } else {
                                    String msg = "pre task[" + lock.getTaskOwnerUniqueName() + "] is executing ,so this commit will be ignore";
                                    getLog().warn(msg);
                                    writeResult(false, msg, res);
                                }
                                countDown.countDown();
                            }
                            // }
                            break;
                        } catch (Throwable e) {
                            getLog().error(e.getMessage(), e);
                            try {
                                if (countDown.getCount() > 0) {
                                    writeResult(false, ExceptionUtils.getMessage(e), res);
                                }
                            } catch (Exception e1) {
                            } finally {
                                try {
                                    countDown.countDown();
                                } catch (Throwable ee) {
                                }
                            }
                            break;
                        }
                    }
                } finally {
                    mdcContext.removeParam();
                }
                // end run
            }));
            try {
                countDown.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        } finally {
            mdcContext.removeParam();
        }
    }

    // private MDCParamContext getMDCParam(final HttpExecContext execContext) {
    // MDCParamContext result = new MDCParamContext();
    // final String indexName =
    // execContext.getString(IFullBuildContext.KEY_APP_NAME);
    // if (StringUtils.isNotEmpty(indexName)) {
    // MDC.put("app", indexName);
    // return new FullPhraseMDCParamContext(indexName);
    // return result;
    // }
    // 
    // Long wfid = execContext.getLong(IFullBuildContext.KEY_WORKFLOW_ID);
    // MDC.put(IFullBuildContext.KEY_WORKFLOW_ID, String.valueOf(wfid));
    // result.setWorkflowId(wfid);
    // return result;
    // }
    private MDCParamContext getMDCParam(final HttpExecContext execContext, HttpServletResponse res) {
        final String indexName = execContext.getString(IFullBuildContext.KEY_APP_NAME);
        if (StringUtils.isNotEmpty(indexName)) {
            MDC.put("app", indexName);
            return StringUtils.startsWith(indexName, TISCollectionUtils.NAME_PREFIX) ?
                    new FullPhraseMDCParamContext(indexName, res)
                    : new DataXMDCParamContext(indexName, res);
        }
        Long wfid = execContext.getLong(IFullBuildContext.KEY_WORKFLOW_ID);
        MDC.put(IFullBuildContext.KEY_WORKFLOW_ID, String.valueOf(wfid));
        return new JustDataFlowMDCParamContext(wfid, res);
    }

    private abstract class MDCParamContext implements IRebindableMDC {

        protected static final String MDC_KEY_TASK_ID = IParamContext.KEY_TASK_ID;

        protected final HttpServletResponse res;

        private Integer taskid;

        public MDCParamContext(HttpServletResponse res) {
            super();
            this.res = res;
        }

        abstract boolean validateParam() throws ServletException;

        protected abstract String getExecLockKey();

        public final ExecuteLock getExecLock() {
            ExecuteLock lock = idles.get(getExecLockKey());
            if (lock == null) {
                synchronized (TisServlet.this) {
                    lock = idles.get(getExecLockKey());
                    if (lock == null) {
                        lock = new ExecuteLock(getExecLockKey());
                        idles.put(getExecLockKey(), lock);
                    }
                }
            }
            return lock;
        }

        void resetParam(Integer taskid) {
            if (taskid == null || taskid < 1) {
                throw new IllegalArgumentException("param taskid can not be empty");
            }
            this.taskid = taskid;
            MDC.put(IParamContext.KEY_TASK_ID, String.valueOf(taskid));
        }

        /**
         * 当子流程在新的线程中执行，需要重新绑定上下文参数
         */
        @Override
        public void rebind() {
            this.resetParam(this.taskid);
        }

        abstract void removeParam();
    }

    private class JustDataFlowMDCParamContext extends MDCParamContext {

        private final Long workflowId;

        public JustDataFlowMDCParamContext(Long workflowId, HttpServletResponse res) {
            super(res);
            this.workflowId = workflowId;
        }

        @Override
        protected String getExecLockKey() {
            return IFullBuildContext.KEY_WORKFLOW_ID + "-" + this.getWorkflowId();
        }

        @Override
        void removeParam() {
            MDC.remove("app");
            MDC.remove(IFullBuildContext.KEY_WORKFLOW_ID);
        }

        @Override
        boolean validateParam() {
            return true;
        }

        @Override
        void resetParam(Integer taskid) {
            super.resetParam(taskid);
            MDC.put(IFullBuildContext.KEY_WORKFLOW_ID, String.valueOf(this.getWorkflowId()));
        }

        private Long getWorkflowId() {
            return this.workflowId;
        }
    }

    /**
     * 全阶段构参数上下文
     */
    private class FullPhraseMDCParamContext extends MDCParamContext {

        private final String indexName;

        public FullPhraseMDCParamContext(String indexName, HttpServletResponse res) {
            super(res);
            this.indexName = indexName;
        }

        protected String getExecLockKey() {
            return this.indexName;
        }

        private String getIndexName() {
            return this.indexName;
        }

        @Override
        void removeParam() {
            MDC.remove("app");
            MDC.remove(IFullBuildContext.KEY_WORKFLOW_ID);
        }

        @Override
        void resetParam(Integer taskid) {
            super.resetParam(taskid);
            MDC.put("app", indexName);
        }

        @Override
        boolean validateParam() throws ServletException {
            if (shallValidateCollectionExist() && !indexSwapTaskflowLauncher.containIndex(indexName)) {
                String msg = "indexName:" + indexName + " is not acceptable";
                getLog().error(msg + ",exist collection:{}", indexSwapTaskflowLauncher.getIndexNames());
                writeResult(false, msg, res);
                return false;
            }
            return true;
        }
    }

    private class DataXMDCParamContext extends FullPhraseMDCParamContext {
        public DataXMDCParamContext(String dataxName, HttpServletResponse res) {
            super(dataxName, res);
        }

        @Override
        boolean validateParam() throws ServletException {
            return true;
        }
    }

    /**
     * 创建新的task
     *
     * @param chainContext
     * @return taskid
     */
    Integer createNewTask(IExecChainContext chainContext) {
        Integer workflowId = chainContext.getWorkflowId();
        NewTaskParam newTaskParam = new NewTaskParam();
        ExecutePhaseRange executeRanage = chainContext.getExecutePhaseRange();
        if (executeRanage.getEnd().bigThan(FullbuildPhase.JOIN)) {
            String indexname = chainContext.getIndexName();
            newTaskParam.setAppname(indexname);
        }
        String histroyTaskId = chainContext.getString(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID);
        if (StringUtils.isNotBlank(histroyTaskId)) {
            newTaskParam.setHistoryTaskId(Integer.parseInt(histroyTaskId));
        }
        newTaskParam.setWorkflowid(workflowId);
        newTaskParam.setExecuteRanage(executeRanage);
        // newTaskParam.setToPhase(FullbuildPhase.IndexBackFlow);
        newTaskParam.setTriggerType(TriggerType.MANUAL);
        Integer taskid = WorkflowDumpAndJoinInterceptor.createNewTask(newTaskParam);
        log.info("create new taskid:" + taskid);
        chainContext.setAttribute(IParamContext.KEY_TASK_ID, taskid);
        return taskid;
    }

    protected Logger getLog() {
        return log;
    }

    protected ExecuteResult startWork(final DefaultChainContext chainContext) throws Exception {
        return indexSwapTaskflowLauncher.startWork(chainContext);
    }

    protected HttpExecContext createHttpExecContext(ServletRequest req) {
        return new HttpExecContext(req);
    }

    protected class ExecuteLock {

        private final Queue<Future<?>> futureQueue = new ConcurrentLinkedQueue<Future<?>>();

        // private final ReentrantLock lock;
        private final AtomicBoolean lock = new AtomicBoolean(false);

        // 开始时间，需要用它判断是否超时
        private AtomicLong startTimestamp;

        // 超时时间为9个小时
        private static final long EXPIR_TIME = 1000 * 60 * 60 * 9;

        private final String taskOwnerUniqueName;

        public ExecuteLock(String indexName) {
            this.taskOwnerUniqueName = indexName;
            // 这个lock 的问题是必须要由拥有这个lock的owner thread 来释放锁，不然的话就会抛异常
            // this.lock = new ReentrantLock();
            this.startTimestamp = new AtomicLong(System.currentTimeMillis());
        }

        public String getTaskOwnerUniqueName() {
            return taskOwnerUniqueName;
        }

        boolean isExpire() {
            long start = startTimestamp.get();
            long now = System.currentTimeMillis();
            // 没有完成
            // 查看是否超时
            boolean expire = ((start + EXPIR_TIME) < now);
            if (expire) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                log.info("time:" + format.format(new Date(start)) + "is expire");
            }
            return expire;
        }

        /**
         * 尝试加锁
         *
         * @return
         */
        public boolean lock() {
            if (this.lock.compareAndSet(false, true)) {
                this.startTimestamp.getAndSet(System.currentTimeMillis());
                return true;
            } else {
                return false;
            }
        }

        /**
         * 释放锁
         */
        public void unlock() {
            // this.lock.unlock();
            this.lock.lazySet(false);
        }
    }

    protected void writeResult(boolean success, String msg, ServletResponse res, KV... kvs) throws ServletException {
        res.setContentType("text/json");
        try {
            JSONObject json = new JSONObject();
            json.put("success", success);
            if (StringUtils.isNotBlank(msg)) {
                json.put("msg", msg);
            }
            if (kvs != null) {
                JSONObject kvjson = new JSONObject();
                for (KV kv : kvs) {
                    kvjson.put(kv.key, kv.value);
                }
                json.put("biz", kvjson);
            }
            res.getWriter().write(json.toString(1));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public static class KV {

        private final String key;

        private final String value;

        /**
         * @param key
         * @param value
         */
        public KV(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}

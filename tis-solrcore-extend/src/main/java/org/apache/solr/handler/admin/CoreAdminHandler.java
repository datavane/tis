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
package org.apache.solr.handler.admin;

import com.google.common.collect.ImmutableMap;
import com.qlangtech.tis.cloud.ICoreAdminAction;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.RepositoryException;
import com.qlangtech.tis.solrextend.cloud.TisSolrResourceLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.api.Api;
import org.apache.solr.cloud.CloudDescriptor;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ExecutorUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.SolrNamedThreadFactory;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.logging.MDCLoggingContext;
import org.apache.solr.metrics.SolrMetricManager;
import org.apache.solr.metrics.SolrMetricsContext;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.security.AuthorizationContext;
import org.apache.solr.security.PermissionNameProvider;
import org.apache.solr.util.stats.MetricUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ExecutorService;
import static org.apache.solr.common.params.CoreAdminParams.ACTION;
import static org.apache.solr.common.params.CoreAdminParams.CoreAdminAction.STATUS;
import static org.apache.solr.security.PermissionNameProvider.Name.CORE_EDIT_PERM;
import static org.apache.solr.security.PermissionNameProvider.Name.CORE_READ_PERM;

/**
 * @since solr 1.3
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class CoreAdminHandler extends RequestHandlerBase implements PermissionNameProvider {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final CoreContainer coreContainer;

    protected final Map<String, Map<String, TaskObject>> requestStatusMap;

    private final CoreAdminHandlerApi coreAdminHandlerApi;

    protected ExecutorService parallelExecutor = ExecutorUtil.newMDCAwareFixedThreadPool(50, new SolrNamedThreadFactory("parallelCoreAdminExecutor"));

    protected static int MAX_TRACKED_REQUESTS = 100;

    public static String RUNNING = "running";

    public static String COMPLETED = "completed";

    public static String FAILED = "failed";

    public static String RESPONSE = "Response";

    public static String RESPONSE_STATUS = "STATUS";

    public static String RESPONSE_MESSAGE = "msg";

    public CoreAdminHandler() {
        super();
        // Unlike most request handlers, CoreContainer initialization
        // should happen in the constructor...
        this.coreContainer = null;
        HashMap<String, Map<String, TaskObject>> map = new HashMap<>(3, 1.0f);
        map.put(RUNNING, Collections.synchronizedMap(new LinkedHashMap<String, TaskObject>()));
        map.put(COMPLETED, Collections.synchronizedMap(new LinkedHashMap<String, TaskObject>()));
        map.put(FAILED, Collections.synchronizedMap(new LinkedHashMap<String, TaskObject>()));
        requestStatusMap = Collections.unmodifiableMap(map);
        coreAdminHandlerApi = new CoreAdminHandlerApi(this);
    }

    /**
     * Overloaded ctor to inject CoreContainer into the handler.
     *
     * @param coreContainer Core Container of the solr webapp installed.
     */
    public CoreAdminHandler(final CoreContainer coreContainer) {
        this.coreContainer = coreContainer;
        HashMap<String, Map<String, TaskObject>> map = new HashMap<>(3, 1.0f);
        map.put(RUNNING, Collections.synchronizedMap(new LinkedHashMap<String, TaskObject>()));
        map.put(COMPLETED, Collections.synchronizedMap(new LinkedHashMap<String, TaskObject>()));
        map.put(FAILED, Collections.synchronizedMap(new LinkedHashMap<String, TaskObject>()));
        requestStatusMap = Collections.unmodifiableMap(map);
        coreAdminHandlerApi = new CoreAdminHandlerApi(this);
    }

    @Override
    public final void init(@SuppressWarnings({ "rawtypes" }) NamedList args) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "CoreAdminHandler should not be configured in solrconf.xml\n" + "it is a special Handler configured directly by the RequestDispatcher");
    }

    @Override
    public void initializeMetrics(SolrMetricsContext parentContext, String scope) {
        super.initializeMetrics(parentContext, scope);
        parallelExecutor = MetricUtils.instrumentedExecutorService(parallelExecutor, this, solrMetricsContext.getMetricRegistry(), SolrMetricManager.mkName("parallelCoreAdminExecutor", getCategory().name(), scope, "threadPool"));
    }

    @Override
    public Boolean registerV2() {
        return Boolean.TRUE;
    }

    /**
     * The instance of CoreContainer this handler handles. This should be the CoreContainer instance that created this
     * handler.
     *
     * @return a CoreContainer instance
     */
    public CoreContainer getCoreContainer() {
        return this.coreContainer;
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        // Make sure the cores is enabled
        try {
            CoreContainer cores = getCoreContainer();
            if (cores == null) {
                throw new SolrException(ErrorCode.BAD_REQUEST, "Core container instance missing");
            }
            // boolean doPersist = false;
            final String taskId = req.getParams().get(CommonAdminParams.ASYNC);
            final TaskObject taskObject = new TaskObject(taskId);
            if (taskId != null) {
                // Put the tasks into the maps for tracking
                if (getRequestStatusMap(RUNNING).containsKey(taskId) || getRequestStatusMap(COMPLETED).containsKey(taskId) || getRequestStatusMap(FAILED).containsKey(taskId)) {
                    throw new SolrException(ErrorCode.BAD_REQUEST, "Duplicate request with the same requestid found.");
                }
                addTask(RUNNING, taskObject);
            }
            // baisui add 2019/01/22
            SolrParams solrParams = req.getParams();
            String action = solrParams.get(ICoreAdminAction.EXEC_ACTION);
            if (StringUtils.equals(ICoreAdminAction.ACTION_UPDATE_CONFIG, action)) {
                // 更新配置文件
                this.updateConfig(req, rsp);
                return;
            }
            // baisui add end
            // Pick the action
            CoreAdminOperation op = opMap.get(req.getParams().get(ACTION, STATUS.toString()).toLowerCase(Locale.ROOT));
            if (op == null) {
                // ▼▼▼ 百岁 20190213 add for asynchronize task
                try {
                    MDC.put("CoreAdminHandler.asyncId", taskId);
                    MDC.put("CoreAdminHandler.action", "handleCustomAction");
                    parallelExecutor.execute(new Runnable() {

                        @Override
                        public void run() {
                            boolean exceptionCaught = false;
                            try {
                                handleCustomAction(req, rsp);
                                taskObject.setRspObject(rsp);
                            } catch (Exception e) {
                                exceptionCaught = true;
                                taskObject.setRspObjectFromException(e);
                            } finally {
                                removeTask("running", taskObject.taskId);
                                if (exceptionCaught) {
                                    addTask(FAILED, taskObject, true);
                                } else {
                                    addTask(COMPLETED, taskObject, true);
                                }
                            }
                        }
                    });
                } finally {
                    MDC.remove("CoreAdminHandler.asyncId");
                    MDC.remove("CoreAdminHandler.action");
                }
                // ▲▲▲ 百岁 add for asynchronize task end
                return;
            }
            final CallInfo callInfo = new CallInfo(this, req, rsp, op);
            String coreName = req.getParams().get(CoreAdminParams.CORE);
            if (coreName == null) {
                coreName = req.getParams().get(CoreAdminParams.NAME);
            }
            MDCLoggingContext.setCoreName(coreName);
            if (taskId == null) {
                callInfo.call();
            } else {
                try {
                    MDC.put("CoreAdminHandler.asyncId", taskId);
                    MDC.put("CoreAdminHandler.action", op.action.toString());
                    parallelExecutor.execute(() -> {
                        boolean exceptionCaught = false;
                        try {
                            callInfo.call();
                            taskObject.setRspObject(callInfo.rsp);
                        } catch (Exception e) {
                            exceptionCaught = true;
                            taskObject.setRspObjectFromException(e);
                        } finally {
                            removeTask("running", taskObject.taskId);
                            if (exceptionCaught) {
                                addTask("failed", taskObject, true);
                            } else {
                                addTask("completed", taskObject, true);
                            }
                        }
                    });
                } finally {
                    MDC.remove("CoreAdminHandler.asyncId");
                    MDC.remove("CoreAdminHandler.action");
                }
            }
        } finally {
            rsp.setHttpCaching(false);
        }
    }

    /**
     * 百岁 add 2016/06/14 start 更新配置文件
     *
     * @param req
     * @param rsp
     * @throws Exception
     */
    private void updateConfig(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        SolrParams params = req.getParams();
        String cname = params.get(CoreAdminParams.CORE);
        String collection = params.get(CoreAdminParams.COLLECTION);
        boolean needReload = params.getBool("needReload", true);
        // TisSolrResourceLoader.getRemoteSnapshotId(collection);
        int snapshotId = params.getInt(ICoreAdminAction.TARGET_SNAPSHOT_ID, -1);
        if (snapshotId < 0) {
            throw new IllegalStateException("param " + ICoreAdminAction.TARGET_SNAPSHOT_ID + " can not be null");
        }
        updateConfig(req, rsp, collection, cname, needReload, snapshotId);
    }

    /**
     * @param req
     * @param rsp
     * @param collection
     * @param needReload
     * @param newSnapshotId
     * @throws RepositoryException
     * @throws IOException
     */
    protected boolean updateConfig(SolrQueryRequest req, SolrQueryResponse rsp, String collection, String cname, boolean needReload, int newSnapshotId) throws RepositoryException, IOException {
        // try (SolrCore core = coreContainer.getCore(cname)) {
        File collectionDir = TisSolrResourceLoader.getCollectionConfigDir(new File(coreContainer.getSolrHome()), collection);
        // 本地版本号
        int loalSnapshot = TisSolrResourceLoader.getConfigSnapshotId(collectionDir);
        if (loalSnapshot == newSnapshotId) {
            String errorMsg = "repository snapshot is same to local snapshotid:" + loalSnapshot + "shall not update config";
            log.warn(errorMsg);
            SimpleOrderedMap<String> errors = new SimpleOrderedMap<String>();
            errors.add("err1", errorMsg);
            rsp.add("failure", errors);
            return false;
        }
        PropteryGetter[] getters = TisSolrResourceLoader.configFileNames.values().toArray(new PropteryGetter[0]);
        // -1,
        // -1,
        TisSolrResourceLoader.downConfigFromConsoleRepository(newSnapshotId, collection, collectionDir, getters, true);
        try {
            if (needReload) {
                // 重新加载索引,当需要重新通过full build才能生效的时候，就先不reload
                // this.handleReloadAction(req, rsp);
                this.handReloadOperation(req, rsp);
            }
        } catch (Exception e) {
            // 回滚配置文件
            TisSolrResourceLoader.saveConfigFileSnapshotId(collectionDir, loalSnapshot);
            log.warn(e.getMessage(), e);
            SimpleOrderedMap<String> errors = new SimpleOrderedMap<String>();
            errors.add("err1", e.getMessage());
            rsp.add("failure", errors);
            rsp.setException(e);
            return false;
        }
        return true;
    // }
    }

    // 百岁 add 2016/06/14 end
    // baisui add for reload start
    public void handReloadOperation(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        final CallInfo callInfo = new CallInfo(this, req, rsp, CoreAdminOperation.RELOAD_OP);
        callInfo.call();
    }

    /**
     * Handle Custom Action.
     * <p>
     * This method could be overridden by derived classes to handle custom actions. <br> By default - this method throws a
     * solr exception. Derived classes are free to write their derivation if necessary.
     */
    protected void handleCustomAction(SolrQueryRequest req, SolrQueryResponse rsp) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unsupported operation: " + req.getParams().get(ACTION));
    }

    public static ImmutableMap<String, String> paramToProp = ImmutableMap.<String, String>builder().put(CoreAdminParams.CONFIG, CoreDescriptor.CORE_CONFIG).put(CoreAdminParams.SCHEMA, CoreDescriptor.CORE_SCHEMA).put(CoreAdminParams.DATA_DIR, CoreDescriptor.CORE_DATADIR).put(CoreAdminParams.ULOG_DIR, CoreDescriptor.CORE_ULOGDIR).put(CoreAdminParams.CONFIGSET, CoreDescriptor.CORE_CONFIGSET).put(CoreAdminParams.LOAD_ON_STARTUP, CoreDescriptor.CORE_LOADONSTARTUP).put(CoreAdminParams.TRANSIENT, CoreDescriptor.CORE_TRANSIENT).put(CoreAdminParams.SHARD, CoreDescriptor.CORE_SHARD).put(CoreAdminParams.COLLECTION, CoreDescriptor.CORE_COLLECTION).put(CoreAdminParams.ROLES, CoreDescriptor.CORE_ROLES).put(CoreAdminParams.CORE_NODE_NAME, CoreDescriptor.CORE_NODE_NAME).put(ZkStateReader.NUM_SHARDS_PROP, CloudDescriptor.NUM_SHARDS).put(CoreAdminParams.REPLICA_TYPE, CloudDescriptor.REPLICA_TYPE).build();

    protected static Map<String, String> buildCoreParams(SolrParams params) {
        Map<String, String> coreParams = new HashMap<>();
        // standard core create parameters
        for (Map.Entry<String, String> entry : paramToProp.entrySet()) {
            String value = params.get(entry.getKey(), null);
            if (StringUtils.isNotEmpty(value)) {
                coreParams.put(entry.getValue(), value);
            }
        }
        // extra properties
        Iterator<String> paramsIt = params.getParameterNamesIterator();
        while (paramsIt.hasNext()) {
            String param = paramsIt.next();
            if (param.startsWith(CoreAdminParams.PROPERTY_PREFIX)) {
                String propName = param.substring(CoreAdminParams.PROPERTY_PREFIX.length());
                String propValue = params.get(param);
                coreParams.put(propName, propValue);
            }
            if (param.startsWith(ZkController.COLLECTION_PARAM_PREFIX)) {
                coreParams.put(param, params.get(param));
            }
        }
        return coreParams;
    }

    protected static String normalizePath(String path) {
        if (path == null)
            return null;
        path = path.replace('/', File.separatorChar);
        path = path.replace('\\', File.separatorChar);
        return path;
    }

    public static ModifiableSolrParams params(String... params) {
        ModifiableSolrParams msp = new ModifiableSolrParams();
        for (int i = 0; i < params.length; i += 2) {
            msp.add(params[i], params[i + 1]);
        }
        return msp;
    }

    // ////////////////////// SolrInfoMBeans methods //////////////////////
    @Override
    public String getDescription() {
        return "Manage Multiple Solr Cores";
    }

    @Override
    public Category getCategory() {
        return Category.ADMIN;
    }

    @Override
    public Name getPermissionName(AuthorizationContext ctx) {
        String action = ctx.getParams().get(CoreAdminParams.ACTION);
        if (action == null)
            return CORE_READ_PERM;
        CoreAdminParams.CoreAdminAction coreAction = CoreAdminParams.CoreAdminAction.get(action);
        if (coreAction == null)
            return CORE_READ_PERM;
        return coreAction.isRead ? CORE_READ_PERM : CORE_EDIT_PERM;
    }

    /**
     * Helper class to manage the tasks to be tracked.
     * This contains the taskId, request and the response (if available).
     */
    static class TaskObject {

        String taskId;

        String rspInfo;

        // baisui add for 20160818 索引回流状态反馈
        private TisCoreAdminHandler.IndexBackflowStatus backflowStatus;

        public TisCoreAdminHandler.IndexBackflowStatus getBackflowStatus() {
            return backflowStatus;
        }

        public TaskObject(String taskId) {
            this.taskId = taskId;
        }

        public String getRspObject() {
            return rspInfo;
        }

        public void setRspObject(SolrQueryResponse rspObject) {
            this.rspInfo = rspObject.getToLogAsString("TaskId: " + this.taskId);
            this.backflowStatus = (TisCoreAdminHandler.IndexBackflowStatus) rspObject.getValues().get(TisCoreAdminHandler.KEY_INDEX_BACK_FLOW_STATUS);
        }

        public void setRspObjectFromException(Exception e) {
            this.rspInfo = e.getMessage();
        }
    }

    /**
     * Helper method to add a task to a tracking type.
     */
    void addTask(String type, TaskObject o, boolean limit) {
        synchronized (getRequestStatusMap(type)) {
            if (limit && getRequestStatusMap(type).size() == MAX_TRACKED_REQUESTS) {
                String key = getRequestStatusMap(type).entrySet().iterator().next().getKey();
                getRequestStatusMap(type).remove(key);
            }
            addTask(type, o);
        }
    }

    private void addTask(String type, TaskObject o) {
        synchronized (getRequestStatusMap(type)) {
            getRequestStatusMap(type).put(o.taskId, o);
        }
    }

    /**
     * Helper method to remove a task from a tracking map.
     */
    private void removeTask(String map, String taskId) {
        synchronized (getRequestStatusMap(map)) {
            getRequestStatusMap(map).remove(taskId);
        }
    }

    /**
     * Helper method to get a request status map given the name.
     */
    Map<String, TaskObject> getRequestStatusMap(String key) {
        return requestStatusMap.get(key);
    }

    /**
     * Method to ensure shutting down of the ThreadPool Executor.
     */
    public void shutdown() {
        if (parallelExecutor != null)
            ExecutorUtil.shutdownAndAwaitTermination(parallelExecutor);
    }

    private static final Map<String, CoreAdminOperation> opMap = new HashMap<>();

    static class CallInfo {

        final CoreAdminHandler handler;

        final SolrQueryRequest req;

        final SolrQueryResponse rsp;

        final CoreAdminOperation op;

        CallInfo(CoreAdminHandler handler, SolrQueryRequest req, SolrQueryResponse rsp, CoreAdminOperation op) {
            this.handler = handler;
            this.req = req;
            this.rsp = rsp;
            this.op = op;
        }

        void call() throws Exception {
            // preCoreAdminHandlerExecute(req, rsp, op);
            op.execute(this);
        }
    }

    @Override
    public Collection<Api> getApis() {
        return coreAdminHandlerApi.getApis();
    }

    static {
        for (CoreAdminOperation op : CoreAdminOperation.values()) opMap.put(op.action.toString().toLowerCase(Locale.ROOT), op);
    }

    /**
     * used by the INVOKE action of core admin handler
     */
    public interface Invocable {

        Map<String, Object> invoke(SolrQueryRequest req);
    }

    interface CoreAdminOp {

        /**
         * @param it request/response object
         *           <p>
         *           If the request is invalid throw a SolrException with SolrException.ErrorCode.BAD_REQUEST ( 400 )
         *           If the execution of the command fails throw a SolrException with SolrException.ErrorCode.SERVER_ERROR ( 500 )
         *           <p>
         *           Any non-SolrException's are wrapped at a higher level as a SolrException with SolrException.ErrorCode.SERVER_ERROR.
         */
        void execute(CallInfo it) throws Exception;
    }
}

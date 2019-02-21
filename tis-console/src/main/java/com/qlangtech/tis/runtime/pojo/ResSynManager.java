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
package com.qlangtech.tis.runtime.pojo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileContext.Header;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.ConfigFileValidateResult;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SchemaFileInvalidException;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.common.TerminatorRepositoryException;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.misc.MessageHandler;
import com.qlangtech.tis.runtime.module.screen.jarcontent.BasicContentScreen;
import junit.framework.Assert;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class ResSynManager {

    private final SnapshotDomain dailyRes;

    private final SnapshotDomain onlineResource;

    private final List<ResSyn> compareResults;

    private final String collectionName;

    private final RunContext runContext;

    // private final RunEnvironment targetRuntimer;
    private static final Logger logger = LoggerFactory.getLogger(ResSynManager.class);

    /**
     * 创建从日常更新配置同步到线上管理器(在日常执行)
     *
     * @param appName
     * @param runContext
     * @return
     * @throws Exception
     */
    public static RuntimeResSynManager createSynManagerOnlineFromDaily(String appName, RunContext runContext, RunEnvironment runtime, SnapshotDomain dailyResDomain) throws Exception {
        if (dailyResDomain == null) {
            throw new IllegalStateException("appName:" + appName + " is not exist");
        }
        // Config.getTerminatorRepositoryOnline()
        // String appName, RunEnvironment runtime, String terminatorRepositoryOnline
        // 线上的配置
        SnapshotDomain onlineRes = ResSynManager.getOnlineResourceConfig(appName, runtime, runtime.getPublicRepositoryURL());
        return new RuntimeResSynManager(runtime, create(appName, dailyResDomain, onlineRes, runContext));
    }

    public static final class RuntimeResSynManager {

        public final RunEnvironment runtime;

        public final ResSynManager sysManager;

        public RuntimeResSynManager(RunEnvironment runtime, ResSynManager sysManager) {
            super();
            this.runtime = runtime;
            this.sysManager = sysManager;
        }

        public RunEnvironment getRuntime() {
            return runtime;
        }

        public ResSynManager getSysManager() {
            return sysManager;
        }
    }

    public static ResSynManager create(String collectionName, SnapshotDomain dailyRes, SnapshotDomain onlineRes, RunContext runContext) throws TerminatorRepositoryException {
        return new ResSynManager(collectionName, dailyRes, onlineRes, runContext);
    }

    private ResSynManager(String collectionName, SnapshotDomain dailyResource, SnapshotDomain onlineResource, RunContext runContext) throws TerminatorRepositoryException {
        this.runContext = runContext;
        // 取得daily环境中的配置文件信息
        Assert.assertNotNull(dailyResource);
        this.dailyRes = dailyResource;
        this.onlineResource = onlineResource;
        this.compareResults = new ArrayList<ResSyn>();
        this.collectionName = collectionName;
        for (PropteryGetter getter : ConfigFileReader.getAry) {
            // daily中没有资源的话则退出
            if (getter.getUploadResource(dailyResource) == null) {
                continue;
            }
            this.compareResults.add(new ResSyn(getter.getFileName(), getter.getUploadResource(dailyResource), (onlineResource == null) ? null : getter.getUploadResource(onlineResource), getter));
        }
        dmp = new diff_match_patch();
    }

    /**
     * 取得daily日常环境下的配置文件(不需要合并全局参数)
     *
     * @param appName
     * @return
     * @throws TerminatorRepositoryException
     */
    public static SnapshotDomain getOnlineResourceConfig(String appName, RunEnvironment runtime, String tisRepositoryOnline) throws TerminatorRepositoryException {
        return HttpConfigFileReader.getResource(tisRepositoryOnline, appName, 0, /* groupIndex */
        runtime, true, /* unmergeglobalparams */
        false, /* reThrowNewException */
        ConfigFileReader.getAry);
    }

    /**
     * 取得daily环境中应用名称的suggest
     *
     * @param appNamePrefix
     * @return
     */
    // http://daily.terminator.admin.taobao.org/runtime/changedomain.action?action=change_domain_action&event_submit_do_app_name_suggest=y&query=search4sucai
    // public static List<Application> appSuggest(String appNamePrefix) {
    // 
    // final StringBuffer urlbuffer = new
    // StringBuffer(Config.getTerminatorRepositoryOnline());
    // // StringBuffer urlbuffer = new StringBuffer("http://localhost");
    // urlbuffer.append(
    // "/config/changedomain.action?action=app_relevant_action&event_submit_do_app_name_suggest=y&query=");
    // urlbuffer.append(appNamePrefix);
    // 
    // URL requestUrl;
    // try {
    // requestUrl = new URL(urlbuffer.toString());
    // } catch (MalformedURLException e) {
    // throw new RuntimeException(e);
    // }
    // 
    // return ConfigFileContext.processContent(requestUrl, new
    // StreamProcess<List<Application>>() {
    // @Override
    // public List<Application> p(int status, InputStream stream, String md5) {
    // List<Application> suggest = new ArrayList<Application>();
    // try {
    // JSONTokener tokener = new JSONTokener(IOUtils.toString(stream));
    // JSONObject json = new JSONObject(tokener);
    // JSONArray dataAry = (JSONArray) json.get("data");
    // JSONArray suggestAry = (JSONArray) json.get("suggestions");
    // for (int i = 0; i < dataAry.length(); i++) {
    // Application app = new Application();
    // app.setAppId(dataAry.getInt(i));
    // app.setProjectName(suggestAry.getString(i));
    // suggest.add(app);
    // }
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // 
    // return suggest;
    // }
    // });
    // }
    public SnapshotDomain getDailyRes() {
        return dailyRes;
    }

    public SnapshotDomain getOnlineResDomain() {
        return onlineResource;
    }

    /**
     * 执行将日常配置推送到线上的操作
     *
     * @param context
     * @param module
     * @return
     * @throws UnsupportedEncodingException
     * @throws SchemaFileInvalidException
     */
    public Boolean synchronizedOnlineSnapshot(final Context context, final BasicModule module, RunEnvironment targetRuntime) throws Exception {
        final SnapshotDomain onlineResDomain = this.getOnlineResDomain();
        List<ResSyn> reslist = this.getCompareResult();
        List<ResSyn> pushResource = new ArrayList<ResSyn>();
        for (ResSyn res : reslist) {
            if (!res.isSame()) {
                // 若日常和线上相等，则该资源就不推送了
                pushResource.add(res);
                continue;
            }
        // pushResource.add(res);
        }
        if (pushResource.size() < 1) {
            module.addErrorMessage(context, "all config resource has been updated already");
            return false;
        }
        // return snapshot;
        UploadResource push = null;
        List<UploadResource> uploadResources = new ArrayList<UploadResource>();
        for (ResSyn res : pushResource) {
            if (res.getDaily() == null) {
                continue;
            }
            push = new UploadResource();
            push.setResourceType(res.getGetter().getFileName());
            push.setContent(res.getDaily().getContent());
            push.setMd5Code(res.getDaily().getMd5Code());
            uploadResources.add(push);
        }
        ConfigPush configPush = new ConfigPush();
        configPush.setCollection(this.collectionName);
        configPush.setSnapshotId(this.getDailyRes().getSnapshot().getSnId());
        if (this.getOnlineResDomain() != null) {
            configPush.setRemoteSnapshotId(this.getOnlineResDomain().getSnapshot().getSnId());
        }
        configPush.setUploadResources(uploadResources);
        if (onlineResDomain == null) {
            // 说明线上还没有第一次发布
            Department dpt = new Department();
            // dpt.setDptId();
            // dpt.setFullName();
            Application app = this.runContext.getApplicationDAO().loadFromWriteDB(this.getDailyRes().getAppId());
            dpt.setFullName(app.getDptName());
            configPush.setDepartment(dpt);
            configPush.setReception(app.getRecept());
        } else {
            configPush.setReception(this.getDailyRes().getSnapshot().getCreateUserName());
        }
        Writer writer = null;
        boolean success = true;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer = new OutputStreamWriter(out, BasicModule.getEncode());
            BasicServlet.xstream.toXML(configPush, writer);
            writer.flush();
            byte[] content = out.toByteArray();
            // System.out.println(new String(out.toByteArray(),
            // BasicModule.getEncode()));
            // for (String onlineHost : Config.getTerminatorRepositoryOnline()) {
            String onlineHost = targetRuntime.getPublicRepositoryURL();
            final URL url = new URL(onlineHost + "/config/config.ajax?action=app_syn_action&event_submit_do_init_app_from_daily=y");
            if (!HttpUtils.post(url, content, new PostFormStreamProcess<Boolean>() {

                @Override
                public List<Header> getHeaders() {
                    return PostFormStreamProcess.HEADERS_multipart_byteranges;
                }

                @Override
                public Boolean p(int status, InputStream stream, String md5) {
                    try {
                        String result = IOUtils.toString(stream, BasicModule.getEncode());
                        JSONTokener tokener = new JSONTokener(result);
                        JSONObject json = new JSONObject(tokener);
                        JSONArray errs = json.getJSONArray("errormsg");
                        if (errs.length() > 0) {
                            for (int i = 0; i < errs.length(); i++) {
                                module.addErrorMessage(context, errs.getString(i));
                            }
                            return false;
                        }
                        JSONArray msg = json.getJSONArray("msg");
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            })) {
                success = false;
            }
            // }
            return success;
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * @param context
     * @param uploadContent
     * @param md5
     * @param fileGetter
     * @param messageHandler
     * @param runContext
     * @return 新创建的主键
     * @throws UnsupportedEncodingException
     * @throws SchemaFileInvalidException
     */
    public static Integer createNewResource(Context context, final byte[] uploadContent, final String md5, PropteryGetter fileGetter, MessageHandler messageHandler, RunContext runContext) throws UnsupportedEncodingException, SchemaFileInvalidException {
        UploadResource resource = new UploadResource();
        resource.setContent(uploadContent);
        resource.setCreateTime(new Date());
        resource.setResourceType(fileGetter.getFileName());
        resource.setMd5Code(md5);
        ConfigFileValidateResult validateResult = fileGetter.validate(resource);
        // 校验文件格式是否正确，通用用DTD来校验
        if (!validateResult.isValid()) {
            messageHandler.addErrorMessage(context, "更新流程中用DTD来校验XML的合法性，请先在文档头部添加<br/>“&lt;!DOCTYPE schema SYSTEM   &quot;solrres://tisrepository/dtd/solrschema.dtd&quot;&gt;”<br/>");
            messageHandler.addErrorMessage(context, validateResult.getValidateResult());
            BasicContentScreen.setConfigFileContent(context, new String(uploadContent, BasicModule.getEncode()), true);
            throw new SchemaFileInvalidException(validateResult.getValidateResult());
        // return -1;
        }
        return runContext.getUploadResourceDAO().insert(resource);
    }

    public List<ResSyn> getCompareResult() {
        return this.compareResults;
    }

    /**
     * 需要同步吗？
     *
     * @return
     */
    public boolean shallSynchronize() {
        if (this.dailyRes != null && this.onlineResource == null) {
            return true;
        }
        for (ResSyn res : getCompareResult()) {
            if (!res.isSame()) {
                return true;
            }
        }
        return false;
    }

    public static class CompareResult {

        private final PropteryGetter getter;

        private final StringBuffer result = new StringBuffer();

        public CompareResult(PropteryGetter getter) {
            super();
            this.getter = getter;
        }

        public String getFileName() {
            return getter.getFileName();
        }

        public String getHtmlDiffer() {
            return result.toString();
        }
    }

    private final diff_match_patch dmp;

    /**
     * 比较两组配置的内容的区别
     *
     * @return
     */
    public List<CompareResult> diff() {
        final List<CompareResult> result = new ArrayList<CompareResult>();
        try {
            for (ResSyn res : getCompareResult()) {
                if (res.isSame()) {
                    continue;
                }
                CompareResult compare = new CompareResult(res.getGetter());
//                if (ConfigFileReader.FILE_JAR.getFileName().equals(res.getGetter().getFileName())) {
//                    compare.result.append("内容不同");
//                    result.add(compare);
//                    continue;
//                }
                LinkedList<Diff> differ = dmp.diff_main(new String(res.getDaily().getContent(), BasicModule.getEncode()), new String(res.getOnline().getContent(), BasicModule.getEncode()), true);
                for (Diff d : differ) {
                    if (d.operation == Operation.EQUAL) {
                        compare.result.append(StringEscapeUtils.escapeXml(d.text));
                    } else if (d.operation == Operation.DELETE) {
                        compare.result.append("<strike>").append(StringEscapeUtils.escapeXml(d.text)).append("</strike>");
                    } else if (d.operation == Operation.INSERT) {
                        compare.result.append("<span class=\"add\">").append(StringEscapeUtils.escapeXml(d.text)).append("</span>");
                    }
                }
                result.add(compare);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // private static ServerGroup getServerGroup0(Integer appId,
    // RunEnvironment environment, RunContext context) {
    // ServerGroupCriteria query = new ServerGroupCriteria();
    // query.createCriteria().andAppIdEqualTo(appId)
    // .andRuntEnvironmentEqualTo(environment.getId())
    // .andGroupIndexEqualTo((short) 0);
    // 
    // List<ServerGroup> groupList = context.getServerGroupDAO()
    // .selectByExample(query);
    // for (ServerGroup group : groupList) {
    // return group;
    // }
    // 
    // return null;
    // }
    public static void main(String[] ar) throws Exception {
    // List<Application> applist = ResSynManager.appSuggest("search4sucai");
    // for (Application app : applist) {
    // System.out.println(app.getProjectName());
    // }
    }
}

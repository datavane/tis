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
package com.qlangtech.tis.manage.common;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.trigger.ODPSConfig;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Config {

    private static Config config;

    // 本地文件存储系统
    // private final String localRepository;
    private final String terminatorRepository;

    // Daily环境的仓库地址,写死
    // private final String[] terminatorRepositoryOnline;
    private Map<String, String> userToken;

    private final String ssoLoginUrl;

    private final String ssoLogoutUrl;

    private final String projectName;

    // hecla.url=http://auth.admin.taobao.org/console/login.htm
    // hecla.cookietKey=heclatest
    // 授权系统 相关参数
    // private final String heclaUrl;
    // private final String heclaCookietKey;
    private Map<RunEnvironment, String> publishHook;

    private Map<RunEnvironment, String> responseTimeHost;

    // private Map<RunEnvironment, String> zkAddress;
    private Map<RunEnvironment, String> hdfshost;

    private final List<FuncGroup> funcGroup = new ArrayList<FuncGroup>();

    // 洪震开发的center node节点地址
    // private final Map<RunEnvironment, String> centerNodeAddress = new
    // HashMap<RunEnvironment, String>();
    // hsf.monitor.host.daily
    private final Map<RunEnvironment, String> hsfMonotorHost = new HashMap<RunEnvironment, String>();

    // index.build.center.host.daily
    // 云就开发的buid中心地址
    // private final Map<RunEnvironment, String> indexBuildCenterUrl = new
    // HashMap<RunEnvironment, String>();
    // 终搜部门id
    private Integer dptTerminatorId;

    private static String bundlePath;

    private TddlDatasourceConfig termiantorDsConfig;

    // 通过appname来解析tddl拓扑图的服务端地址
    // private String tddlParseHost;
    private String outerDatatunelEndpoint;

    // // 南星查询： MQ中的状态 mq.statistics.host
    // private List<String> mqStatisticsHost;
    /**
     * 设置加载的路径config文件加载路径
     *
     * @param path
     */
    public static void setBundlePath(String path) {
        if (config != null) {
            throw new IllegalStateException("local config obj has been created,can not set path repeat");
        }
        bundlePath = path;
    }

    private Config() {
        ResourceBundle bundle = ResourceBundle.getBundle(StringUtils.defaultIfEmpty(bundlePath, System.getProperty("terminator_config", "com/taobao/terminator/manage/config")));
        // localRepository = bundle.getString("local.repository");
        P p = new P(bundle);
        // mq.statistics.host
        // this.mqStatisticsHost =
        // Lists.newArrayList(StringUtils.split(p.getString("mq.statistics.host"),
        // ","));
        this.terminatorRepository = p.getString("terminator.repository");
        // this.terminatorRepositoryOnline = StringUtils.split(p.getString("terminator.repository.special.online"), ",");
        this.projectName = p.getString("project.name");
        if (StringUtils.isEmpty(this.projectName)) {
            throw new IllegalStateException("config param projectname can not be null");
        }
        userToken = new HashMap<String, String>();
        publishHook = new HashMap<RunEnvironment, String>();
        responseTimeHost = new HashMap<RunEnvironment, String>();
        // zkAddress = new HashMap<RunEnvironment, String>();
        hdfshost = new HashMap<RunEnvironment, String>();
        // this.tddlParseHost = p.getString("tddl.parse.host");
        Enumeration<String> keys = bundle.getKeys();
        String nameKey = null;
        final String hookHostPrefix = "publish.hook.host.";
        final String queryResponseTimeHost = "query.response.time.host.";
        final String zkaddress = "zkaddress.host.";
        while (keys.hasMoreElements()) {
            nameKey = keys.nextElement();
            if (StringUtils.startsWith(nameKey, "user.")) {
                userToken.put(StringUtils.substringAfter(nameKey, "user."), bundle.getString(nameKey));
            }
            setKey(publishHook, bundle, nameKey, hookHostPrefix);
            setKey(responseTimeHost, bundle, nameKey, queryResponseTimeHost);
            // setKey(zkAddress, bundle, nameKey, zkaddress);
            // hdfs 地址
            setKey(hdfshost, bundle, nameKey, "hdfs.host.");
            setKey(hsfMonotorHost, bundle, nameKey, "hsf.monitor.host.");
        }
        try {
            this.dptTerminatorId = Integer.parseInt(bundle.getString("dpt.terminator.id"));
        } catch (Throwable e) {
        }
        // sso.login.url=
        // sso.logout.url=
        this.ssoLoginUrl = p.getString("sso.login.url");
        this.ssoLogoutUrl = p.getString("sso.logout.url");
        try {
            FuncGroup group = null;
            String[] groups = StringUtils.split(bundle.getString("func.groups"), ",");
            for (String g : groups) {
                group = new FuncGroup(Integer.parseInt(StringUtils.split(g, ":")[0]), StringUtils.split(g, ":")[1]);
                funcGroup.add(group);
            }
        } catch (Throwable e) {
        }
        // terminator.datasource.appname=TERMINATORHOME_APP
        // terminator.datasource.group=TERMINATORHOME_GROUP
        final String dsAppName = "terminator.datasource.appname";
        if (bundle.containsKey(dsAppName)) {
            this.termiantorDsConfig = new TddlDatasourceConfig(p.getString(dsAppName), p.getString("terminator.datasource.group"));
        }
        // terminator.end_point=http://service-corp.odps.aliyun-inc.com/api
        try {
            odpsConfig = new ODPSConfig();
            odpsConfig.setProject(p.getString("terminator.inner.project_name"));
            odpsConfig.setAccessId(p.getString("terminator.inner.access_id"));
            odpsConfig.setAccessKey(p.getString("terminator.inner.access_key"));
        // odpsConfig.setServiceEndPoint(p.getString("terminator.end_point"));
        } catch (Throwable e) {
        }
        // 弹外datatunelodps网关
        this.outerDatatunelEndpoint = p.getString("terminator.outer.datatunel.endpoint");
    }

    private ODPSConfig odpsConfig;

    public static ODPSConfig getOdpsConfig() {
        ODPSConfig config = getInstance().odpsConfig;
        if (StringUtils.isEmpty(config.getAccessId()) || StringUtils.isEmpty(config.getAccessKey()) || StringUtils.isEmpty(config.getProject()) || StringUtils.isEmpty(config.getAccessId())) {
            throw new IllegalStateException("one of odpsConfig property is null");
        }
        return getInstance().odpsConfig;
    }

    // /**
    // * 南星的MQ访问查询服務端地址
    // *
    // * @return
    // */
    // public static List<String> getMQStatisticsHost() {
    // List<String> mqStatisticsHost = getInstance().mqStatisticsHost;
    // if (mqStatisticsHost.size() < 1) {
    // throw new IllegalStateException("mqStatisticsHost size can not small than
    // 1");
    // }
    // return mqStatisticsHost;
    // }
    /**
     * 弹外odps数据通道网关
     *
     * @return
     */
    public static String getOuterDatatunelEndpoint() {
        return getInstance().outerDatatunelEndpoint;
    }

    public static class TddlDatasourceConfig {

        private final String appName;

        private final String groupName;

        public TddlDatasourceConfig(String appName, String groupName) {
            super();
            this.appName = appName;
            this.groupName = groupName;
        }

        public String getAppName() {
            return appName;
        }

        public String getGroupName() {
            return groupName;
        }
    }

    // public static String getTddlParseHost() {
    // return getInstance().tddlParseHost;
    // }
    public static TddlDatasourceConfig getTermiantorDsConfig() {
        return getInstance().termiantorDsConfig;
    }

    private class P {

        private final ResourceBundle bundle;

        public P(ResourceBundle bundle) {
            super();
            this.bundle = bundle;
        }

        public final String getString(String key) {
            try {
                return bundle.getString(key);
            } catch (Throwable e) {
            }
            return StringUtils.EMPTY;
        }
    }

    private void setKey(Map<RunEnvironment, String> store, ResourceBundle bundle, String nameKey, final String prefix) {
        if (StringUtils.startsWith(nameKey, prefix)) {
            RunEnvironment envir = RunEnvironment.getEnum(StringUtils.substringAfter(nameKey, prefix));
            store.put(envir, bundle.getString(nameKey));
        }
    }

    /**
     * 终搜部门id
     *
     * @return
     */
    public static int getDptTerminatorId() {
        return getInstance().dptTerminatorId;
    }

    // public static String getHeclaUrl() {
    // return getInstance().heclaUrl;
    // }
    // 
    // public static String getHeclaCookietKey() {
    // return getInstance().heclaCookietKey;
    // }
    public static List<FuncGroup> getFuncGroup() {
        return getInstance().funcGroup;
    }

    private static Config getInstance() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new Config();
                }
            }
        }
        return config;
    }

    // public static File getLocalRepository() {
    // return new File(getInstance().localRepository);
    // }
    public static Map<String, String> getUserToken() {
        return getInstance().userToken;
    }

    public static String getProjectName() {
        return getInstance().projectName;
    }

    public static Map<RunEnvironment, String> getPublishHook() {
        return getInstance().publishHook;
    }

    public static String getResponseTimeHost(RunEnvironment runtime) {
        return getInstance().responseTimeHost.get(runtime);
    }

    public static String getSSOLoginURL() {
        return getInstance().ssoLoginUrl;
    }

    public static String getSSOLogoutURL() {
        return getInstance().ssoLogoutUrl;
    }

    // /**
    // * 取得zk地址
    // *
    // * @param runtime
    // * @return
    // */
    // public static String getZkAddress(RunEnvironment runtime) {
    // return getInstance().zkAddress.get(runtime);
    // }
    public static String getHdfsNameNodeHost(RunEnvironment runtime) {
        return getInstance().hdfshost.get(runtime);
    }

    /**
     * hsf 地址
     *
     * @param runtime
     * @return
     */
    public static String getHsfMonotorHost(RunEnvironment runtime) {
        return getInstance().hsfMonotorHost.get(runtime);
    }

    // // 洪震开发的center node节点地址
    // public static String getCenterNodeAddress(RunEnvironment runtime) {
    // return getInstance().centerNodeAddress.get(runtime);
    // }
    /**
     * 取得终搜仓库地址，运行在不同环境会变化
     *
     * @return
     */
    public static String getTerminatorRepository() {
        return getInstance().terminatorRepository;
    }

    // /**
    // * 终搜Daily仓库地址，不会变化
    // *
    // * @return
    // */
    // public static String[] getTerminatorRepositoryOnline() {
    // return getInstance().terminatorRepositoryOnline;
    // }
    /**
     * 取得索引build中心 监控地址， 云就相关
     *
     * @param runtime
     * @return
     */
    // public static String getIndexBuildCenterUrl(RunEnvironment runtime) {
    // //return getInstance().indexBuildCenterUrl.get(runtime);
    // 
    // 
    // return TSearcherConfigFetcher.
    // }
    public static RunEnvironment getRunEnvironment() {
        return RunEnvironment.DAILY;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    // System.out.println(Config.getLocalRepository());
    // 
    // for (String key : Config.getUserToken().keySet()) {
    // System.out.println(key);
    // }
    }

    public static class FuncGroup {

        private final Integer key;

        private final String name;

        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        public FuncGroup(Integer key, String name) {
            super();
            this.key = key;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Integer getKey() {
            return key;
        }
    }
}

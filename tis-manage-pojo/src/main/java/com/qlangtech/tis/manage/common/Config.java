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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ResourceBundle;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Config {

    public static final String S4TOTALPAY = "search4totalpay";

    public static final String KEY_ZK_HOST = "zk.host";

    public static final String KEY_ASSEMBLE_HOST = "assemble.host";

    public static final String KEY_TIS_HOST = "tis.host";

    public static final String KEY_RUNTIME = "runtime";

    public static final String KEY_JAVA_RUNTIME_PROP_ENV_PROPS = "env_props";

    public static final String CONTEXT_TIS = "/tjs";

    public static final String CONTEXT_ASSEMBLE = "/tis-assemble";

    public static final String SUB_DIR_LIBS = "libs";

    public static final String SUB_DIR_CFG_REPO = "cfg_repo";

    private static String GENERATE_PARENT_PACKAGE = "com.qlangtech.tis.realtime.transfer";

    public static final int LogFlumeAddressPORT = 41414;

    private static boolean test = false;

    private static Config config;

    private static final String KEY_DATA_DIR = "data.dir";

    public static void setDataDir(String path) {
        System.setProperty(KEY_DATA_DIR, path);
    }

    public static void setTest(boolean val) {
        test = val;
    }

    public static boolean isTestMock() {
        return test;
    }

    /**
     * 本地基础配置目录
     *
     * @return
     */
    public static File getMetaCfgDir() {
        File dir = getDataDir();
        return new File(dir, SUB_DIR_CFG_REPO);
    }

    public static File getLibDir() {
        File dir = getDataDir();
        return new File(dir, SUB_DIR_LIBS);
    }

    public static File getDataDir() {
        File dir = new File(System.getProperty(KEY_DATA_DIR, "/opt/data/tis"));
        if (!(dir.isDirectory() && dir.exists())) {
            throw new IllegalStateException("dir:" + dir.getAbsolutePath() + " is invalid DATA DIR");
        }
        return dir;
    }

    private final String zkHost;

    private final String tisHost;

    private final String runtime;

    private static final String bundlePath = "tis-web-config/config";

    // 组装节点
    private final String assembleHost;

    public final TisDbConfig dbCfg;

    private Config() {
        P p = P.create();
        this.zkHost = p.getString(KEY_ZK_HOST, true);
        this.assembleHost = p.getString(KEY_ASSEMBLE_HOST, true);
        this.tisHost = p.getString(KEY_TIS_HOST, true);
        this.runtime = p.getString(KEY_RUNTIME, true);

//        tis.datasource.url=192.168.28.200
//        tis.datasource.username=root
//        tis.datasource.password=123456
//        tis.datasource.dbname=tis_console
        this.dbCfg = new TisDbConfig();
        try {
            this.dbCfg.port = Integer.parseInt(p.getString("tis.datasource.port"));
            this.dbCfg.url = p.getString("tis.datasource.url");
            this.dbCfg.userName = p.getString("tis.datasource.username");
            this.dbCfg.password = p.getString("tis.datasource.password");
            this.dbCfg.dbname = p.getString("tis.datasource.dbname");
        } catch (Exception e) {
           throw new IllegalStateException("please check the tis datasource cfg",e);
        }
    }


    public static String getRuntime() {
        return getInstance().runtime;
    }

    public static String getConfigRepositoryHost() {
        return "http://" + getInstance().tisHost + ":8080" + CONTEXT_TIS;
    }

    public static String getTisHost() {
        return getInstance().tisHost;
    }

    public static String getAssembleHost() {
        return getInstance().assembleHost;
    }

    public static String getAssembleHttpHost() {
        return "http://" + getInstance().assembleHost + ":8080" + CONTEXT_ASSEMBLE;
    }

    public static TisDbConfig getDbCfg() {
        return getInstance().dbCfg;
    }

    public static String getZKHost() {
        String zkAddress = getInstance().zkHost;
        if (StringUtils.isBlank(zkAddress)) {
            throw new IllegalStateException("zkAddress can not be null");
        }
        return zkAddress;
    }

    public static String getTerminatorRepositoryOnline() {
        throw new UnsupportedOperationException();
    }

    public static int getIndexBuildCenterUrl(RunEnvironment runEnvironment) {
        throw new UnsupportedOperationException();
    }

    public static Object getHdfsNameNodeHost(RunEnvironment runEnvironment) {
        throw new UnsupportedOperationException();
    }

    private abstract static class P {

        public static P create() {
            if (Boolean.getBoolean(KEY_JAVA_RUNTIME_PROP_ENV_PROPS)) {
                return new P() {

                    @Override
                    protected String getProp(String key) {
                        return System.getenv(key);
                    }
                };
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle(StringUtils.defaultIfEmpty(bundlePath, System.getProperty("terminator_config", "com/qlangtech/tis/manage/config")));
                return new P() {

                    @Override
                    protected String getProp(String key) {
                        return bundle.getString(key);
                    }
                };
            }
        }

        public P() {
            super();
        }

        public final String getString(String key) {
            return getString(key, false);
        }

        public final String getString(String key, boolean notEmpty) {
            String value = null;
            try {
                value = getProp(key);
            } catch (Throwable e) {
            }
            if (StringUtils.isEmpty(value) && notEmpty) {
                throw new IllegalStateException("key:" + key + " relevant can not be empty");
            }
            return value;
        }

        protected abstract String getProp(String key);
    }

    public static String getGenerateParentPackage() {
        return GENERATE_PARENT_PACKAGE;
    }

    public String getZkHost() {
        return this.zkHost;
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

    public static class TisDbConfig {
        //        tis.datasource.url=192.168.28.200
//        tis.datasource.username=root
//        tis.datasource.password=123456
//        tis.datasource.dbname=tis_console
        public int port;
        public String url;
        public String userName;
        public String password;
        public String dbname;
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

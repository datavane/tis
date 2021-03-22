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


import com.qlangtech.tis.org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
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
    public static final String DB_TYPE_MYSQL = "mysql";
    public static final String DB_TYPE_DERBY = "derby";

    private static String GENERATE_PARENT_PACKAGE = "com.qlangtech.tis.realtime.transfer";

    public static final int LogFlumeAddressPORT = 41414;

    private static boolean test = false;

    private static Config config;

    public static final String KEY_DATA_DIR = "data.dir";


    public static void setDataDir(String path) {
        System.setProperty(KEY_DATA_DIR, path);
    }

    public static void setTest(boolean val) {
        test = val;
    }

    public static boolean isTestMock() {
        return test;
    }

    public static File setTestDataDir() {
        String dataDir = null;
        if ((dataDir = System.getProperty(Config.KEY_DATA_DIR)) != null) {
            throw new RuntimeException("dataDir:" + dataDir + " must be empty");
        }
        try {
            File tmp = new File("/tmp/tis");
            FileUtils.deleteQuietly(tmp);
            FileUtils.forceMkdir(tmp);
            Config.setDataDir(tmp.getAbsolutePath());
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private final TisDbConfig dbCfg;

    private Config() {
        P p = P.create();
        this.zkHost = p.getString(KEY_ZK_HOST, true);
        this.assembleHost = p.getString(KEY_ASSEMBLE_HOST, true);
        this.tisHost = p.getString(KEY_TIS_HOST, true);
        this.runtime = p.getString(KEY_RUNTIME, true);

        this.dbCfg = new TisDbConfig();
        try {
            dbCfg.dbtype = p.getString("tis.datasource.type", true);
            dbCfg.dbname = p.getString("tis.datasource.dbname", true);
            if (DB_TYPE_MYSQL.equals(dbCfg.dbtype)) {
                dbCfg.port = Integer.parseInt(p.getString("tis.datasource.port"));
                dbCfg.url = p.getString("tis.datasource.url");
                dbCfg.userName = p.getString("tis.datasource.username");
                dbCfg.password = p.getString("tis.datasource.password");
            }
        } catch (Exception e) {
            throw new IllegalStateException("please check the tis datasource cfg", e);
        }
    }


    public String getRuntime() {
        return this.runtime;
    }

    private static final ThreadLocal<String> threadContext = new ThreadLocal<>();

    public static void setThreadContextTisHost(String tishost) {
        threadContext.set(tishost);
    }

    public static String getConfigRepositoryHost() {
        String tisHost = null;
        if ((tisHost = threadContext.get()) == null) {
            tisHost = getInstance().tisHost;
        } else {
            threadContext.remove();
        }
        return "http://" + tisHost + ":8080" + CONTEXT_TIS;
    }

    public static String getTisHost() {
        return getInstance().tisHost;
    }

    public static String getAssembleHost() {
        return getInstance().assembleHost;
    }

//    public static String getAssembleHttpHost() {
    //   return "http://" + getInstance().assembleHost + ":8080" + CONTEXT_ASSEMBLE;
//    }

    public static TisDbConfig getDbCfg() {
        return getInstance().getDbConfig();
    }

    public TisDbConfig getDbConfig() {
        return this.dbCfg;
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


    public static TestCfgStream openTestCfgStream() throws IOException {
        File f = new File("../tis-web-config/config.properties");
        TestCfgStream cfgStream = new TestCfgStream(f);
        if (!f.exists()) {
            return cfgStream;
        }
        return cfgStream.setPropsStream(FileUtils.openInputStream(f));
    }

    public static class TestCfgStream {
        private final File propsFile;
        private InputStream propsStream;

        public InputStream getPropsStream() {
            return propsStream;
        }

        public File getPropsFile() {
            return propsFile;
        }

        public void validate(Throwable ee) {
            if (this.propsStream == null) {
                URL cpRoot = Config.class.getResource("/");
                URL cpThreadRoot = Thread.currentThread().getContextClassLoader().getResource("/");
                throw new IllegalStateException("file relevant stream is null,confFile:" + this.propsFile.getAbsolutePath()
                        + ",current classpath root url:" + cpRoot.toString() + ",thread classpath:" + cpThreadRoot.toString(), ee);
            }
            //Objects.requireNonNull(this.propsStream, "file relevant stream is null,confFile:" + this.propsFile.getAbsolutePath());
        }

        public TestCfgStream(File propsFile) {
            this.propsFile = propsFile;
        }

        public TestCfgStream setPropsStream(InputStream propsStream) {
            this.propsStream = propsStream;
            return this;
        }
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
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);
                    return new P() {
                        @Override
                        protected String getProp(String key) {
                            return bundle.getString(key);
                        }
                    };
                } catch (Throwable ee) {
                    // 测试环境中取工程目录下的配置文件
                    Properties props = new Properties();
                    try {
                        TestCfgStream cfgStream = openTestCfgStream();
                        cfgStream.validate(ee);
                        try (InputStream input = cfgStream.propsStream) {
                            props.load(input);
                        }
                        Config.setTest(true);
                        return new P() {
                            @Override
                            protected String getProp(String key) {
                                return props.getProperty(key);
                            }
                        };
                    } catch (IOException ex) {
                        StringBuffer errMsg = new StringBuffer();
                        errMsg.append("config file err:\n");
                        errMsg.append("has tried twice\n");
                        errMsg.append("second err:").append(ex.getMessage()).append("\n");
                        throw new RuntimeException(errMsg.toString());
                    }

                }
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

    public static Config getInstance() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new Config();
                }
            }
        }
        return config;
    }

    public static void setConfig(Config cfg) {
        config = cfg;
    }

    public static class TisDbConfig {

        public String dbtype;
        public String dbname;

        //        tis.datasource.url=192.168.28.200
//        tis.datasource.username=root
//        tis.datasource.password=123456
//        tis.datasource.dbname=tis_console
        public int port;
        public String url;
        public String userName;
        public String password;

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

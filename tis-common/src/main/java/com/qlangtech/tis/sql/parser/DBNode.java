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
package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.db.parser.domain.DBConfig;
import com.qlangtech.tis.git.GitUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import com.qlangtech.tis.offline.DbScope;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DBNode {

    private final String dbName;

    private final int dbId;

    // private File daoDir;
    private long timestampVer;

    private static final Yaml yaml;

    static {
        yaml = new Yaml(new Constructor());
    }

    public String getFormatDBName() {
        return getFormatDBName(this.dbName);
    }

    public static String getFormatDBName(String dbName) {
        if (StringUtils.isEmpty(dbName)) {
            throw new IllegalArgumentException("param dbName can not be null");
        }
        return StringUtils.remove(StringUtils.lowerCase(dbName), "_");
    }

    public static String getDAOJarName(String dbName) {
        return getFormatDBName(dbName) + "-dao.jar";
    }

    public static void dump(List<DBNode> nodes, File f) throws Exception {
        try (Writer writer = new OutputStreamWriter(FileUtils.openOutputStream(f), TisUTF8.get()) {
        }) {
            yaml.dump(nodes.stream().map((r) -> {
                Map<String, Object> row = Maps.newHashMap();
                row.put("dbname", r.getDbName());
                row.put("dbid", r.getDbId());
                row.put("timestamp", new Long(r.getTimestampVer()));
                return row;
            }).collect(Collectors.toList()), writer);
        }
    }

    /**
     * @param collection
     * @param timestamp
     * @return
     * @throws Exception
     */
    public static void registerDependencyDbsFacadeConfig(String collection, long timestamp, DefaultListableBeanFactory factory) {
        try {
            List<DBNode> dbs = null;
            try (InputStream input = FileUtils.openInputStream(StreamContextConstant.getDbDependencyConfigMetaFile(collection, timestamp))) {
                dbs = DBNode.load(input);
                Map<String, DBConfig> dbConfigsMap
                        = dbs.stream().collect(Collectors.toMap((db) -> db.getDbName()
                        , (db) -> GitUtils.$().getDbLinkMetaData(db.getDbName(), DbScope.FACADE)));

                for (Map.Entry<String, DBConfig> entry : dbConfigsMap.entrySet()) {
//                    SpringDBRegister dbRegister = new SpringDBRegister(entry.getKey(), entry.getValue(), factory);
//                    dbRegister.visitFirst();
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new UnsupportedOperationException();
    }

    public static List<DBNode> load(InputStream inputstrea) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(inputstrea, TisUTF8.get()) {
        }) {
            Object o = yaml.load(reader);
            List<Map<String, Object>> configMap = (List) o;
            return configMap.stream().map((r) -> {
                String dbname = (String) r.get("dbname");
                Integer dbid = (Integer) r.get("dbid");
                DBNode db = new DBNode(dbname, dbid);
                db.setTimestampVer(Long.parseLong(String.valueOf(r.get("timestamp"))));
                return db;
            }).collect(Collectors.toList());
        }
    }

    public DBNode(String dbName, int dbId) {
        this.dbName = dbName;
        this.dbId = dbId;
    }

    public File getDaoDir() {
        return StreamContextConstant.getDAORootDir(this.getDbName(), this.getTimestampVer());
        // if (this.daoDir == null || !this.daoDir.exists()) {
        // throw new IllegalStateException("dao dir is not exist,dir:" + this.daoDir);
        // }
        // return daoDir;
    }

    // public void setDaoDir(File daoDir) {
    // this.daoDir = daoDir;
    // }
    public long getTimestampVer() {
        return timestampVer;
    }

    public DBNode setTimestampVer(long timestampVer) {
        this.timestampVer = timestampVer;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public int getDbId() {
        return dbId;
    }

    @Override
    public String toString() {
        return "{" + "dbName='" + dbName + '\'' + ", dbId=" + dbId + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DBNode dbNode = (DBNode) o;
        return dbId == dbNode.dbId;
    }

    @Override
    public int hashCode() {
        return this.dbId;
    }

    public static Set<String> appendDBDependenciesClasspath(Set<DBNode> dependencyDBNodes) {
        Set<String> classpathElements = Sets.newHashSet();
        for (DBNode db : dependencyDBNodes) {
            File jarFile = new File(db.getDaoDir(), db.getDbName() + "-dao.jar");
            if (!jarFile.exists()) {
                throw new IllegalStateException("jarfile:" + jarFile.getAbsolutePath() + " is not exist");
            }
            classpathElements.add(jarFile.getAbsolutePath());
        }
        return classpathElements;
    }
}

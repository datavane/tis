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
package com.qlangtech.tis.manage.spring;

import com.qlangtech.tis.manage.common.Config;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-10 14:30
 */
public class TISDataSourceFactory implements FactoryBean<BasicDataSource>, InitializingBean, DisposableBean {


  public static SystemDBInit createDataSource(String dbType, Config.TisDbConfig dbCfg, boolean useDBName, boolean dbAutoCreate) {
    if (StringUtils.isEmpty(dbType)) {
      throw new IllegalArgumentException("param dbType can not be null");
    }
    if (StringUtils.isEmpty(dbCfg.dbname)) {
      throw new IllegalArgumentException("param dbName can not be null");
    }
    BasicDataSource dataSource = new BasicDataSource();
    if (Config.DB_TYPE_MYSQL.equals(dbType)) {

      dataSource.setDriverClassName("com.mysql.jdbc.Driver");
      dataSource.setUrl("jdbc:mysql://" + dbCfg.url + ":" + dbCfg.port + (useDBName ? ("/" + dbCfg.dbname) : StringUtils.EMPTY)
        + "?useUnicode=yes&amp;characterEncoding=utf8");
      if (StringUtils.isBlank(dbCfg.dbname)) {
        throw new IllegalStateException("dbCfg.dbname in config.properites can not be null");
      }
      dataSource.setUsername(dbCfg.userName);
      dataSource.setPassword(dbCfg.password);
      dataSource.setValidationQuery("select 1");
      return new SystemDBInit(dataSource) {
        public boolean dbTisConsoleExist(Config.TisDbConfig dbCfg, Statement statement) throws SQLException {

          boolean containTisConsole = false;
          try (ResultSet showDatabaseResult = statement.executeQuery("show databases")) {
            while (showDatabaseResult.next()) {
              if (dbCfg.dbname.equals(showDatabaseResult.getString(1))) {
                containTisConsole = true;
              }
            }
          }
          return containTisConsole;
        }

        @Override
        public void dropDB(Config.TisDbConfig dbCfg, Statement statement) throws SQLException {
          statement.execute("drop database if exists " + dbCfg.dbname);
        }

        @Override
        public void createSysDB(Config.TisDbConfig dbCfg, Statement statement) throws SQLException {
          statement.addBatch("create database " + dbCfg.dbname + ";");
          statement.addBatch("use " + dbCfg.dbname + ";");
        }

        @Override
        public boolean shallSkip(String sql) {
          return false;
        }

        @Override
        public boolean needInitZkPath() {
          return true;
        }
      };
    } else if (Config.DB_TYPE_DERBY.equals(dbType)) {
      System.setProperty("derby.system.home", Config.getDataDir().getAbsolutePath());
//  <bean id="clusterStatusDatasource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
//    <property name="driverClassName" value="org.apache.derby.jdbc.EmbeddedDriver"/>
//    <property name="url" value="jdbc:derby:tis_console;create=true"/>
//  </bean>
      dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
      dataSource.setUrl("jdbc:derby:" + dbCfg.dbname + ";create=" + dbAutoCreate);
      return new SystemDBInit(dataSource) {
        @Override
        public boolean dbTisConsoleExist(Config.TisDbConfig dbCfg, Statement statement) throws SQLException {
          // derby 库肯定存在,但是里面的表不一定存在
          // s.execute("update WISH_LIST set ENTRY_DATE = CURRENT_TIMESTAMP, WISH_ITEM = 'TEST ENTRY' where 1=3");
          try {
            statement.executeQuery("select * from application FETCH NEXT 1 ROWS ONLY");
          } catch (SQLException e) {

            String theError = (e).getSQLState();
            //   System.out.println("  Utils GOT:  " + theError);
            /** If table exists will get -  WARNING 02000: No row was found **/
            if (theError.equals("42X05"))   // Table does not exist
            {
              return false;
            } else {
              // WwdChk4Table: Unhandled SQLException
              throw e;
            }
          }
          return true;
        }

        @Override
        public String processSql(StringBuffer result) {
          String sql = StringUtils.remove(super.processSql(result), "`");
          sql = StringUtils.substringBefore(sql, ";");
          return sql;
        }

        @Override
        public void dropDB(Config.TisDbConfig dbCfg, Statement statement) throws SQLException {
          try {
            statement.execute("drop table application ");
          } catch (SQLException e) {

          }
        }

        @Override
        public boolean needInitZkPath() {
          return true;
        }

        @Override
        public boolean shallSkip(String sql) {
          if (StringUtils.startsWithIgnoreCase(sql, "drop")) {
            // drop 语句需要跳过
            return true;
          }
          return false;
        }

        @Override
        public void createSysDB(Config.TisDbConfig dbCfg, Statement statement) throws SQLException {

        }
      };
    }

    throw new IllegalStateException("dbType:" + dbType + " is illegal");
  }


  public static abstract class SystemDBInit {
    private final BasicDataSource dataSource;

    public SystemDBInit(BasicDataSource dataSource) {
      this.dataSource = dataSource;
    }

    public BasicDataSource getDS() {
      return this.dataSource;
    }

    /**
     * 初始化过程中是否需要初始化 ZK节点中的值
     *
     * @return
     */
    public abstract boolean needInitZkPath();

    public abstract boolean dbTisConsoleExist(Config.TisDbConfig dbCfg, Statement statement) throws SQLException;

    public abstract void createSysDB(Config.TisDbConfig dbCfg, Statement statement) throws SQLException;

    public void close() {
      try {
        dataSource.close();
      } catch (SQLException e) {
      }
    }


    public abstract void dropDB(Config.TisDbConfig dbCfg, Statement statement) throws SQLException;

    /**
     * 处理执行SQL，derby需要将原先sql中 ` 字符去掉
     *
     * @param result
     * @return
     */
    public String processSql(StringBuffer result) {
      return result.toString();
    }

    public abstract boolean shallSkip(String sql);
  }

  @Override
  public void destroy() throws Exception {
    try {
      dataSource.close();
    } catch (Throwable e) {

    }
  }

  private BasicDataSource dataSource;

  @Override
  public void afterPropertiesSet() throws Exception {
    Config.TisDbConfig dbType = Config.getDbCfg();
    this.dataSource = createDataSource(dbType.dbtype, dbType, true, false).dataSource;
  }

  @Override
  public BasicDataSource getObject() throws Exception {
    return this.dataSource;
  }

  @Override
  public Class<BasicDataSource> getObjectType() {
    return BasicDataSource.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}

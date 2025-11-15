/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.plugin.ds;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.impl.AdapterMessageHandler;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract the dataSource modal
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 10:40
 **/
@Public
public abstract class DataSourceFactory implements Describable<DataSourceFactory>, Serializable, DBIdentity, DataSourceMeta, Wrapper {
    // public static final ZoneId DEFAULT_SERVER_TIME_ZONE = MQListenerFactory.DEFAULT_SERVER_TIME_ZONE; // ZoneId.systemDefault();// ZoneId.of("Asia/Shanghai");
    public static final String DS_TYPE_MYSQL = "MySQL";
    public static final String DS_TYPE_MYSQL_V8 = DS_TYPE_MYSQL + "-V8";

    /**
     * 加载DataSource
     *
     * @param dbName
     * @param <D>
     * @return
     */
    public static <D extends DataSourceFactory> D load(String dbName) {
        return TIS.getDataBasePlugin(PostedDSProp.parse(dbName));
    }

    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String name;

    @Override
    public final String identityValue() {
        return this.name;
    }


    /**
     * 查询Connection 的Statement
     *
     * @param stmt
     */
    public void setReaderStatement(Statement stmt) throws SQLException {

    }


    public abstract DBConfig getDbConfig();

    /**
     * DataSource like TiSpark has store format as RDD shall skip the phrase of data dump
     *
     * @return
     */
    public boolean skipDumpPhrase() {
        return false;
    }

    public abstract void visitFirstConnection(final IConnProcessor connProcessor);

    // public abstract void refectTableInDB(TableInDB tabs, Connection conn) throws SQLException;


    /**
     * Get all the dump
     *
     * @return
     */
    public DataDumpers getDataDumpers(TISTable table) {
        throw new UnsupportedOperationException("datasource:" + this.identityValue() + " is not support direct dump");
    }

    @Override
    public final Descriptor<DataSourceFactory> getDescriptor() {
        Descriptor<DataSourceFactory> descriptor = TIS.get().getDescriptor(this.getClass());
        Class<BaseDataSourceFactoryDescriptor> expectDesClass = getExpectDesClass();
        if (!(expectDesClass.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass().getName() + " must implement the Descriptor of " + expectDesClass.getSimpleName());
        }
        return descriptor;
    }

    protected <C extends BaseDataSourceFactoryDescriptor> Class<C> getExpectDesClass() {
        return (Class<C>) BaseDataSourceFactoryDescriptor.class;
    }

    protected void validateConnection(String jdbcUrl, IConnProcessor p) throws TableNotFoundException {
        JDBCConnection conn = null;
        try {
            conn = this.getConnection(jdbcUrl, Optional.empty(), true); // getConnection(jdbcUrl, true);
            p.vist(conn);
        } catch (TableNotFoundException e) {
            throw e;
        } catch (TisException e) {
            throw e;
        } catch (Exception e) {
            throw TisException.create(e.getMessage() + ",jdbcUrl:" + jdbcUrl, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    /**
     * 例子请查看：MangoDBDataSourceFactory包装了createMongoClient：MongoClient
     *
     * @param iface
     * @param <T>
     * @return
     * @throws SQLException
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取jdbc Connection
     * 为了驱动加载不出问题，需要每个实现类中拷贝一份这个代码，default implements:
     * <pre>
     *     @Override
     *     public Connection getConnection(String jdbcUrl, String username, String password) throws SQLException {
     *         return DriverManager.getConnection(jdbcUrl, StringUtils.trimToNull(username), StringUtils.trimToNull(password));
     *     }
     * </pre>
     * * @param verify 创建数据库阶段，验证数据库是否可用，需要数据库连接时设置超时时间
     *
     * @param jdbcUrl
     * @return
     */
    public final JDBCConnection getConnection(String jdbcUrl, Optional<Properties> props, boolean verify) throws SQLException {

        JDBCConnectionPool connectionPool = JDBCConnection.connectionPool.get();
        JDBCConnection conn = null;
        if (connectionPool != null) {
            conn = connectionPool.getConnection(jdbcUrl, verify);
            if (conn == null) {
                return connectionPool.getConnection(jdbcUrl, verify, (url) -> {
                    try {
                        return createConnection(jdbcUrl, props, verify);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                return conn;
            }
        } else {
            return createConnection(jdbcUrl, props, verify);
        }
    }

    /**
     * 创建jdbc连接器
     *
     * @param jdbcUrl
     * @param verify
     * @return
     * @throws SQLException
     */
    protected JDBCConnection createConnection(String jdbcUrl, Optional<Properties> props, boolean verify) throws SQLException {
        throw new UnsupportedOperationException("class:" + this.getClass().getName() + ",jdbcUrl:" + jdbcUrl);
    }

    public JDBCConnection getConnection(String jdbcUrl, Optional<Properties> props, boolean usingPool, boolean verify) throws SQLException {
        return this.getConnection(jdbcUrl, props, verify);
    }

//        // 密码可以为空
//        return DriverManager.getConnection(jdbcUrl, username, StringUtils.trimToNull(password));
//    }

    protected List<ColumnMetaData> parseTableColMeta(
            boolean inSink, String jdbcUrl
            , JDBCConnection conn, EntityName table) throws SQLException, TableNotFoundException {
        table = logicTable2PhysicsTable(jdbcUrl, table);


//        validateConnection(jdbcUrl, (conn) -> {
        DatabaseMetaData metaData1 = null;
        ResultSet primaryKeys = null;
        ResultSet columns1 = null;
        ColumnMetaData colMeta = null;
        String comment = null;
        String typeName = null;

        try {
            metaData1 = conn.getConnection().getMetaData();
            try (ResultSet tables = metaData1.getTables(null, getDbSchema(), table.getTableName(), null)) {
                int count = 0;
                List<String> matchEntries = Lists.newArrayList();
                while (tables.next()) {
                    matchEntries.add(tables.getString("TABLE_NAME")
                            + "(" + tables.getString("TABLE_TYPE")
                            + "," + tables.getString("TABLE_SCHEM") + ")");
                    count++;
                }
                if (count < 1) {
                    throw new TableNotFoundException(this, table.getFullName() + ",url:" + conn.getUrl());
                } else if (count > 1) {
                    throw new IllegalStateException("duplicate table entities exist:" + String.join(",", matchEntries));
                }
            }

            primaryKeys = getPrimaryKeys(table, metaData1);
            columns1 = getColumnsMeta(table, conn, metaData1);
            Set<String> pkCols = createAddedCols(table);
            while (primaryKeys.next()) {
                // $NON-NLS-1$
                String columnName = primaryKeys.getString("COLUMN_NAME");
                pkCols.add(columnName);
            }

            return wrapColsMeta(inSink, table, columns1, pkCols, conn);
        } finally {
            closeResultSet(columns1);
            closeResultSet(primaryKeys);
        }
        // });
        //  return columns;
    }

    public List<ColumnMetaData> wrapColsMeta(
            boolean inSink, EntityName table, ResultSet columns1, JDBCConnection conn) throws SQLException, TableNotFoundException {
        return wrapColsMeta(inSink, table, columns1, Collections.emptySet(), conn);
    }

    public static final String KEY_COLUMN_NAME = "COLUMN_NAME";
    public static final String KEY_REMARKS = "REMARKS";
    public static final String KEY_NULLABLE = "NULLABLE";

    public static final String KEY_DECIMAL_DIGITS = "DECIMAL_DIGITS";
    public static final String KEY_TYPE_NAME = "TYPE_NAME";

    public static final String KEY_DATA_TYPE = "DATA_TYPE";
    public static final String KEY_COLUMN_SIZE = "COLUMN_SIZE";

    public final List<ColumnMetaData> wrapColsMeta(
            boolean inSink, EntityName table, ResultSet columns1, Set<String> pkCols, JDBCConnection conn) throws SQLException, TableNotFoundException {
        return this.wrapColsMeta(inSink, table, columns1, createColumnMetaBuilder(table, columns1, pkCols, conn));
    }

    protected CreateColumnMeta createColumnMetaBuilder(EntityName table, ResultSet columns1, Set<String> pkCols, JDBCConnection conn) {
        return new CreateColumnMeta(pkCols, columns1);
    }

    public List<ColumnMetaData> wrapColsMeta(
            boolean inSink, EntityName table, ResultSet columns1, CreateColumnMeta columnMetaCreator) throws SQLException, TableNotFoundException {

        ColumnMetaData colMeta;
        String colName = null;

//                ResultSetMetaData metaData = columns1.getMetaData();
//                System.out.println("getColumnCount:" + metaData.getColumnCount());
//                for (int ii = 1; ii <= metaData.getColumnCount(); ii++) {
//                    System.out.println(metaData.getColumnName(ii));
//                }

        /** for mysql:
         * TABLE_CAT
         * TABLE_SCHEM
         * TABLE_NAME
         * COLUMN_NAME
         * DATA_TYPE
         * TYPE_NAME
         * COLUMN_SIZE
         * BUFFER_LENGTH
         * DECIMAL_DIGITS
         * NUM_PREC_RADIX
         * NULLABLE
         * REMARKS
         * COLUMN_DEF
         * SQL_DATA_TYPE
         * SQL_DATETIME_SUB
         * CHAR_OCTET_LENGTH
         * ORDINAL_POSITION
         * IS_NULLABLE
         * SCOPE_CATALOG
         * SCOPE_SCHEMA
         * SCOPE_TABLE
         * SOURCE_DATA_TYPE
         * IS_AUTOINCREMENT
         * IS_GENERATEDCOLUMN
         * */
        int i = 0;
        final List<ColumnMetaData> columns = Lists.newArrayList();
        // 防止有col重复，测试中有用户取出的cols会有重复的
        final Set<String> addedCols = createAddedCols(table);
        while (columns1.next()) {
            colName = columns1.getString(KEY_COLUMN_NAME);

            // 如果有重复的col已经添加则直接跳过
            if (addedCols.add(colName)) {
                columns.add(columnMetaCreator.create(colName, i++));
            }
        }
        return columns;
    }

    protected HashSet<String> createAddedCols(EntityName table) throws TableNotFoundException {
        return Sets.newHashSet();
    }

    /**
     * 逻辑表名切换成物理表名
     *
     * @param table
     * @return
     */
    protected EntityName logicTable2PhysicsTable(String jdbcUrl, EntityName table) {
        return table;
    }

    /**
     * 取得对应的物理表集合
     *
     * @param tabEntity 逻辑表
     * @return
     */
    public List<String> getAllPhysicsTabs(DataXJobSubmit.TableDataXEntity tabEntity) {
        return Collections.singletonList(tabEntity.getSourceTableName());
    }

    private String getDbSchema() {
        String dbSchema = null;
        if (this instanceof ISchemaSupported) {
            dbSchema = ((ISchemaSupported) this).getDBSchema();
        }
        return dbSchema;
    }

    protected List<ColumnMetaData> parseTableColMeta(boolean inSink, EntityName table, String jdbcUrl) throws TableNotFoundException {

        AtomicReference<List<ColumnMetaData>> ref = new AtomicReference<>();
        validateConnection(jdbcUrl, (conn) -> {
            List<ColumnMetaData> columnMetaData = parseTableColMeta(inSink, jdbcUrl, conn, table);
            ref.set(columnMetaData);
        });
        return ref.get();
    }

    protected ResultSet getColumnsMeta(EntityName table, JDBCConnection conn, DatabaseMetaData metaData1) throws SQLException {
        return metaData1.getColumns(null, getDbSchema(), table.getTableName(), null);
    }

    protected ResultSet getPrimaryKeys(EntityName table, DatabaseMetaData metaData1) throws SQLException {
        return metaData1.getPrimaryKeys(null, getDbSchema(), table.getTableName());
    }


    protected void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
                ;
            }
        }
    }

    /**
     * 某些数据库是支持schema概念，如支持则实现该接口
     */
    @Public
    public interface ISchemaSupported {

         public static Optional<ISchemaSupported> schemaSupported(DataSourceFactory dsFactory) {
            if (dsFactory instanceof ISchemaSupported && !"MySQLV8DataSourceFactory".equals(dsFactory.getClass().getSimpleName())) {
                return Optional.of((ISchemaSupported) dsFactory);
            }
            return Optional.empty();
        }

        public String getDBSchema();

        /**
         * 例如：在mysql8数据源中使用dbName作为schemaName，所以需要使用schema的地方直接使用dbName就可以了
         *
         * @return
         */
        public default boolean isUseDBNameAsSchemaName() {
            return false;
        }
    }

    public interface IConnProcessor {
        void vist(JDBCConnection conn) throws SQLException, TableNotFoundException;
    }

    public abstract static class BaseDataSourceFactoryDescriptor<T extends DataSourceFactory> extends Descriptor<T>
            implements IEndTypeGetter, IDescribableManipulate<DataSourceFactoryManipulate> {
        private static final Logger logger = LoggerFactory.getLogger(BaseDataSourceFactoryDescriptor.class);


        @Override
        public final String getDisplayName() {
            return this.getDataSourceName();
        }

        @Override
        public Class<DataSourceFactoryManipulate> getManipulateExtendPoint() {
            return DataSourceFactoryManipulate.class;
        }

        @Override
        public final Optional<IPluginStore<DataSourceFactoryManipulate>> getManipulateStore() {
            return Optional.empty();
        }

        /**
         * 通过DataSource关联默认的DataXReader Desc 名称
         *
         * @return
         */
        public Optional<String> getDefaultDataXReaderDescName() {
            return Optional.empty();
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = super.getExtractProps();

            // this.getEndType().appendProps(eprops);

//            eprops.put(KEY_END_TYPE, this.getEndType().getVal());
//            eprops.put(KEY_SUPPORT_ICON, this.getEndType().getIcon() != null);
            eprops.put("supportFacade", this.supportFacade());
            eprops.put("facadeSourceTypes", this.facadeSourceTypes());
            Optional<String> dataXReaderDesc = this.getDefaultDataXReaderDescName();
            if (dataXReaderDesc.isPresent()) {
                eprops.put("dataXReaderDesc", dataXReaderDesc.get());
            }
            return eprops;
        }

        /**
         * Get DB name
         *
         * @return
         */
        protected abstract String getDataSourceName();

        /**
         * Support facade datasource for incr process
         *
         * @return
         */
        protected abstract boolean supportFacade();

        protected List<String> facadeSourceTypes() {
            if (supportFacade()) {
                throw new UnsupportedOperationException("shall overwrite facadeSourceTypes");
            }
            return Collections.emptyList();
        }


        @Override
        protected final boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            T instance = postFormVals.newInstance();
            // this.newInstance((IPluginContext) msgHandler, postFormVals.rawFormData, Optional.empty());
            // = (T) dsFactory.getInstance();
//            if (!msgHandler.validateBizLogic(IFieldErrorHandler.BizLogic.DB_NAME_DUPLICATE, context
//                    , this.getIdentityField().displayName, instance.identityValue())) {
//                return false;
//            }
            boolean valid = validateDSFactory(msgHandler, context, instance);
            return valid;
        }

        /**
         * @param conn
         * @throws TisException
         */
        protected boolean validateConnection(JDBCConnection conn, T dsFactory, IControlMsgHandler msgHandler, Context context) throws TisException {
            return true;
        }

        protected boolean validateDSFactory(IControlMsgHandler msgHandler, Context context, T dsFactory) {

            DBConfig dbConfig = Objects.requireNonNull(dsFactory.getDbConfig(), "dbConfig can not be null");
            Throwable[] faild = new Throwable[1];
            boolean[] validateResult = new boolean[1];
            final Object actionContext = context.getContext();

            try {
                if (!dbConfig.vistDbURL(false, (dbName, dbHost, jdbcUrl) -> {
                    try (JDBCConnection conn = dsFactory.getConnection(jdbcUrl, Optional.empty(), true)) {
                        // 由于不在同一个线程内需要重新绑定线程
                        context.setContext(actionContext);
                        validateResult[0] = validateConnection(conn, dsFactory, msgHandler, context);
                    } catch (Throwable e) {
                        faild[0] = e;
                        logger.warn(e.getMessage(), e);
                    }
                }, false, msgHandler, context, 5)) {
                    // throw TisException.create("error:" + err[0]);
                    return false;
                }
            } catch (JDBCConnectionException e) {
                // throw new RuntimeException(e);
                logger.error(e.getMessage(), e);
                msgHandler.addErrorMessage(context, e.getMessage());
                return false;
            }
//
//            dbConfig.vistDbURL(false, 5, (dbName, dbHost, jdbcUrl) -> {
//                try (JDBCConnection conn = dsFactory.getConnection(jdbcUrl, Optional.empty(), true)) {
//                    // 由于不在同一个线程内需要重新绑定线程
//                    context.setContext(actionContext);
//                    validateResult[0] = validateConnection(conn, dsFactory, msgHandler, context);
//                } catch (Throwable e) {
//                    faild[0] = e;
//                    logger.warn(e.getMessage(), e);
//                }
//            });


            if (faild[0] != null) {
                msgHandler.addErrorMessage(context, "请确认连接参数是否正确:" + faild[0].getMessage());
                return false;
            }

            if (!validateResult[0]) {
                // 校验出错
                return false;
            }


            return true;
        }

    }


    public static class CreateColumnMeta {

        protected final Set<String> pkCols;
        protected final ResultSet columns1;

        public CreateColumnMeta(Set<String> pkCols, ResultSet columns1) {
            this.pkCols = pkCols;
            this.columns1 = columns1;
        }

        public ColumnMetaData create(String colName, int index) throws SQLException {
            String comment = columns1.getString(KEY_REMARKS);
            ColumnMetaData colMeta = new ColumnMetaData((index), colName, getDataType(colName), pkCols.contains(colName), columns1.getBoolean(KEY_NULLABLE));
            if (StringUtils.isNotEmpty(comment)) {
                colMeta.setComment(comment);
            }
            return colMeta;
        }

        protected DataType getDataType(String colName) throws SQLException {
            // decimal 的小数位长度
            int decimalDigits = columns1.getInt(KEY_DECIMAL_DIGITS);
            //数据如果是INT类型，但如果是UNSIGNED，那实际类型需要转换成Long,INT UNSIGNED
            String typeName = columns1.getString(KEY_TYPE_NAME);
            DataType colType = createColDataType(colName, typeName
                    , columns1.getInt(KEY_DATA_TYPE), columns1.getInt(KEY_COLUMN_SIZE), decimalDigits);
            if (decimalDigits > 0) {
                colType.setDecimalDigits(decimalDigits);
            }
            return colType;
        }

        protected DataType parseDataType(int dbColType, String typeName, int colSize) {
            return DataType.create(dbColType, typeName, colSize);
        }

        protected DataType createColDataType(
                String colName, String typeName, int dbColType, int colSize, int decimalDigits) throws SQLException {
            // 类似oracle驱动内部有一套独立的类型 oracle.jdbc.OracleTypes,有需要可以在具体的实现类里面去实现
            DataType type = this.parseDataType(dbColType, typeName, colSize);
            type.setDecimalDigits(decimalDigits);

            if ((type.getJdbcType() == JDBCTypes.DECIMAL || type.getJdbcType() == JDBCTypes.NUMERIC)
                    && decimalDigits == 0) {
                // 例如：Decimal(19,0) 直接视作为 BigInt
                return DataType.create((type.getColumnSize() > 10 ? JDBCTypes.BIGINT : JDBCTypes.INTEGER).getType()
                        , type.typeName, type.getColumnSize());
            }
            return type;
        }
    }


}

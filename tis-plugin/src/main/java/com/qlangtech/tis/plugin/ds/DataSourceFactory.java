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
package com.qlangtech.tis.plugin.ds;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Abstract the dataSource modal
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 10:40
 **/
public abstract class DataSourceFactory implements Describable<DataSourceFactory>, IdentityName, DataSourceMeta ,Wrapper {

    public static final String DS_TYPE_MYSQL = "MySQL";

//    public static List<DataSourceFactory> all() {
//        return TIS.get().getExtensionList(DataSourceFactory.class);
//    }


    /**
     * DataSource like TiSpark has store format as RDD shall skip the phrase of data dump
     *
     * @return
     */
    public boolean skipDumpPhrase() {
        return false;
    }

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
            throw new IllegalStateException(descriptor.getClass().getName() + " must implement the Descriptor of "
                    + expectDesClass.getSimpleName());
        }
        return descriptor;
    }

    protected <C extends BaseDataSourceFactoryDescriptor> Class<C> getExpectDesClass() {
        return (Class<C>) BaseDataSourceFactoryDescriptor.class;
    }

    protected void validateConnection(String jdbcUrl, BasicDataSourceFactory.IConnProcessor p) {
        Connection conn = null;
        try {
            conn = getConnection(jdbcUrl);
            p.vist(conn);
        } catch (Exception e) {
            throw new TisException(e.getMessage() + ",jdbcUrl:" + jdbcUrl, e);
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
     *
     * @param jdbcUrl
     * @return
     */
    public Connection getConnection(String jdbcUrl) throws SQLException {
        throw new UnsupportedOperationException("jdbcUrl:" + jdbcUrl);
    }
//        // 密码可以为空
//        return DriverManager.getConnection(jdbcUrl, username, StringUtils.trimToNull(password));
//    }


    protected List<ColumnMetaData> parseTableColMeta(String table, String jdbcUrl) {
        final List<ColumnMetaData> columns = Lists.newArrayList();
        validateConnection(jdbcUrl, (conn) -> {
            DatabaseMetaData metaData1 = null;
            ResultSet primaryKeys = null;
            ResultSet columns1 = null;
            try {
                metaData1 = conn.getMetaData();
                primaryKeys = metaData1.getPrimaryKeys(null, null, table);
                columns1 = metaData1.getColumns(null, null, table, null);
                Set<String> pkCols = Sets.newHashSet();
                while (primaryKeys.next()) {
                    // $NON-NLS-1$
                    String columnName = primaryKeys.getString("COLUMN_NAME");
                    pkCols.add(columnName);
                }
                int i = 0;
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
                while (columns1.next()) {

                    columns.add(new ColumnMetaData((i++), (colName = columns1.getString("COLUMN_NAME"))
                            , getDataType(columns1), pkCols.contains(colName)));
                }

            } finally {
                closeResultSet(columns1);
                closeResultSet(primaryKeys);
            }
        });
        return columns;
    }

    protected ColumnMetaData.DataType getDataType(ResultSet cols) throws SQLException {
        return new ColumnMetaData.DataType(cols.getInt("DATA_TYPE"), cols.getInt("COLUMN_SIZE"));
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

    public abstract static class BaseDataSourceFactoryDescriptor<T extends DataSourceFactory> extends Descriptor<T> {
        private static final Logger logger = LoggerFactory.getLogger(BaseDataSourceFactoryDescriptor.class);

        @Override
        public final String getDisplayName() {
            return this.getDataSourceName();
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = new HashMap<>();
            eprops.put("supportFacade", this.supportFacade());
            eprops.put("facadeSourceTypes", this.facadeSourceTypes());
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
            ParseDescribable<T> dsFactory = this.newInstance((IPluginContext) msgHandler, postFormVals.rawFormData, Optional.empty());
            T instance = dsFactory.instance;
//            if (!msgHandler.validateBizLogic(IFieldErrorHandler.BizLogic.DB_NAME_DUPLICATE, context
//                    , this.getIdentityField().displayName, instance.identityValue())) {
//                return false;
//            }
            return validateDSFactory(msgHandler, context, instance);
        }

        protected boolean validateDSFactory(IControlMsgHandler msgHandler, Context context, T dsFactory) {
            try {
                List<String> tables = dsFactory.getTablesInDB();
                // msgHandler.addActionMessage(context, "find " + tables.size() + " table in db");
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                msgHandler.addErrorMessage(context, e.getMessage());
                return false;
            }
            return true;
        }

    }
}

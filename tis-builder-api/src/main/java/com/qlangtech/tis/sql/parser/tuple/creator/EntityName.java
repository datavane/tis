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
package com.qlangtech.tis.sql.parser.tuple.creator;

import com.qlangtech.tis.dump.INameWithPathGetter;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表對象名稱
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月14日
 */
public class EntityName implements IDumpTable, INameWithPathGetter {

    public static final String ROW_MAP_CLASS_NAME = "RowMap";

    // public static final String DEFAULT_DB_NAME = "default";
    private final String dbname;

    private final String tabName;

    // 是否设置了dbname
    private boolean dft = false;

    public static EntityName createSubQueryTable() {
        return new SubTableQueryEntity();
    }

    public static EntityName create(String dbname, String tabName) {
        if (StringUtils.isEmpty(dbname)) {
            throw new IllegalArgumentException("param dbName can not be null");
        }
        if (StringUtils.isEmpty(tabName)) {
            throw new IllegalArgumentException("param tabName can not be null");
        }
        return parse(dbname + "." + tabName);
    }

    /**
     * @param entityName
     * @return
     */
    public static EntityName parse(String entityName) {
        final String[] entitInfo = StringUtils.split(entityName, ".");
        EntityName entity = null;
        if (entitInfo.length == 1) {
            entity = new EntityName(entitInfo[0]);
        } else if (entitInfo.length == 2) {
            entity = new EntityName(entitInfo[0], entitInfo[1]);
        } else {
            throw new IllegalStateException("line:" + entityName + " is not valid");
        }
        return entity;
    }

    @Override
    public String getDbName() {
        return dbname;
    }

    @Override
    public String getTableName() {
        return this.tabName;
    }

    @Override
    public String getFullName() {
        return this.toString();
    }

    public String createNewLiteriaToken() {
        StringBuffer buffer = new StringBuffer("EntityName.parse(\"");
        if (this.dft) {
            buffer.append(this.getTabName());
        } else {
            buffer.append(this.toString());
        }
        buffer.append("\")");
        return buffer.toString();
    }

    public static EntityName createFuncEntity(final StringBuffer buffer) {
        EntityName funcEntity = new EntityName("func") {

            @Override
            public String toString() {
                return buffer.toString();
            }
        };
        return funcEntity;
    }

    private EntityName(String tabName) {
        this(DEFAULT_DATABASE_NAME, tabName);
        this.dft = true;
    }

    public boolean useDftDbName() {
        return this.dft;
    }

    private EntityName(String dbname, String tabName) {
        super();
        this.dbname = dbname;
        this.tabName = tabName;
    }

    public String getDbname() {
        return dbname;
    }

    public String facadeDAOInstanceName() {
        return facadeDAOInstanceName(this.getDbname());
    }

    public static String facadeDAOInstanceName(String dbName) {
        if (DEFAULT_DATABASE_NAME.equals(dbName)) {
            throw new IllegalStateException("dbname:" + dbName + " can not equal with '" + DEFAULT_DATABASE_NAME + "'");
        }
        return UnderlineUtils.removeUnderline(dbName) + "DAOFacade";
    }

    // pkColGetter + ".getVal(" + headerEntity.getJavaEntityName() + ")
    public static String createColValLiteria(String colTransferName, String colName, String valToken) {
        return createColGetterLiteria(colTransferName, colName) + ".getVal(" + valToken + ",true)";
    }

    public String createColValLiteria(String colName, String valToken) {
        return createColGetterLiteria(colName) + ".getVal(" + valToken + ",true)";
    }

    public String createColValLiteria(String colName) {
        return createColValLiteria(colName, this.getJavaEntityName());
    }

    public String createColGetterLiteria(String colName) {
        return createColGetterLiteria(this.getJavaEntityName() + "Meta", colName);
    }

    public static String createColGetterLiteria(String colTransferName, String colName) {
        return colTransferName + ".getColMeta(\"" + colName + "\")";
    }

    public String getJavaEntityName() {
        return UnderlineUtils.removeUnderline(tabName).toString();
    }

    public String getTabName() {
        return this.tabName;
    }

    // 实体复数
    public String entities() {
        return this.getJavaEntityName() + "s";
    }

    public String buildDefineRowMapListLiteria() {
        return "var " + this.entities() + ": List[" + ROW_MAP_CLASS_NAME + "]  = null";
    }

    public String buildExecuteQueryDAOLiteria() {
        return (this.entities() + " = this." + this.facadeDAOInstanceName() + ".get" + (this.capitalizeEntityName()) + "DAO().selectColsByExample(" + this.getJavaEntityName() + "Criteria,1,100)");
    }

    public String buildAddSelectorColsLiteria(Set<String> selCols) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getJavaEntityName()).append("Criteria.addSelCol(").append(selCols.stream().map((rr) -> this.javaPropTableName() + "ColEnum." + org.apache.commons.lang.StringUtils.upperCase(rr)).collect(Collectors.joining(" ,"))).append(")");
        return buffer.toString();
    }

    public String buildDefineCriteriaEqualLiteria() {
        return "var " + this.getJavaEntityName() + "Criteria :" + this.capitalizeEntityName() + "Criteria = new " + this.capitalizeEntityName() + "Criteria()";
    }

    public String buildCreateCriteriaLiteria() {
        return this.getJavaEntityName() + "Criteria.createCriteria()";
    }

    public final String javaPropTableName() {
        if (this.dft) {
            throw new IllegalStateException("table:" + this.getJavaEntityName() + " shall set dbName");
        }
        return StringUtils.capitalize(this.getJavaEntityName());
    }

    public String capitalizeEntityName() {
        return org.apache.commons.lang.StringUtils.capitalize(this.getJavaEntityName());
    }

    public final String javaPropDbName() {
        if (this.dft) {
            throw new IllegalStateException("table:" + this.getJavaEntityName() + " shall set dbName");
        }
        return UnderlineUtils.removeUnderline(this.getDbname()).toString();
    }

    @Override
    public String getNameWithPath() {
        return this.dbname + "/" + this.tabName;
    }

    @Override
    public int hashCode() {
        return (this.toString()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntityName)) {
            throw new IllegalStateException("obj" + obj + ",[" + obj.getClass() + "] is not type of EntityName");
        }
        return this.hashCode() == ((EntityName) obj).hashCode();
    }

    @Override
    public String toString() {
        return dbname + "." + tabName;
    }

    public interface SubTableQuery {
    }

    public static final String KeySubTableQuery = "SubTableQuery";

    private static class SubTableQueryEntity extends EntityName implements SubTableQuery {

        public SubTableQueryEntity() {
            super("SubTableQuery");
        }

        @Override
        public String getTabName() {
            // return super.getTabName();
            throw new UnsupportedOperationException(KeySubTableQuery + "'s get TabName is not supported");
        }
    }

    private static class UnderlineUtils {

        private UnderlineUtils() {
        }

        public static StringBuffer removeUnderline(String value) {
            StringBuffer parsedName = new StringBuffer();
            char[] nameAry = value.toCharArray();
            boolean findUnderChar = false;
            for (int i = 0; i < nameAry.length; i++) {
                if (nameAry[i] == '_') {
                    findUnderChar = true;
                } else {
                    if (findUnderChar) {
                        parsedName.append(Character.toUpperCase(nameAry[i]));
                        findUnderChar = false;
                    } else {
                        parsedName.append(nameAry[i]);
                    }
                }
            }
            return parsedName;
        }
    }
}

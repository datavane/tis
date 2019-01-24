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
package com.qlangtech.tis.fullbuild.taskflow.hive;

import com.qlangtech.tis.dump.hive.HiveColumn;
import com.qlangtech.tis.fullbuild.taskflow.TaskConfigParser;
import com.qlangtech.tis.fullbuild.taskflow.TemplateContext;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2/18/2017.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UnionHiveTask extends JoinHiveTask {

    private String tableName;

    private String partition;

    private Set<String> columnSet = new LinkedHashSet<>();

    private Set<String> partitionColumns = new HashSet<>();

    private List<String> subTaskSqls = new ArrayList<>();

    private List<HiveInsertFromSelectParser> parserList = new ArrayList<>();

    private static final String RECORD_NAME = "record";

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
        if (!StringUtils.isBlank(partition)) {
            Arrays.stream(partition.split(",")).map(String::trim).forEach(s -> partitionColumns.add(s));
        }
    }

    private List<String> getSubTaskSqls() {
        return subTaskSqls;
    }

    void setSubTaskSqls(List<String> subTaskSqls) {
        this.subTaskSqls = subTaskSqls;
        parseSubTab();
    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    private void parseSubTab() {
        for (String subTaskSql : getSubTaskSqls()) {
            HiveInsertFromSelectParser parser = new HiveInsertFromSelectParser();
            try {
                parser.start(subTaskSql);
                parserList.add(parser);
                parser.getCols().stream().filter(column -> !partitionColumns.contains(column.getName())).forEach(column -> columnSet.add(column.getName()));
            } catch (IOException | ParseException e) {
                throw new IllegalStateException("parse sub table " + e.getMessage(), e);
            }
        }
        partitionColumns.forEach(column -> columnSet.add(column));
        setContent(getUnionSql());
    }

    private String getUnionSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT OVERWRITE TABLE ").append(getTableName());
        if (!StringUtils.isBlank(partition)) {
            sb.append(" PARTITION (").append(partition).append(")");
        }
        sb.append("\n");
        Set<String> columnSet = getColumnSet();
        sb.append("SELECT ").append(getSetString(columnSet)).append(" FROM (\n");
        sb.append(getParsersString()).append(") AS ").append(RECORD_NAME);
        return sb.toString();
    }

    private Set<String> getColumnSet() {
        return columnSet;
    }

    private String getParsersString() {
        StringBuilder sb = new StringBuilder();
        int parserSize = parserList.size();
        int parserCnt = 0;
        for (HiveInsertFromSelectParser parser : parserList) {
            Map<String, HiveColumn> columnMap = parser.getColsMap();
            sb.append("SELECT ");
            int columnSize = columnSet.size();
            int columnCnt = 0;
            for (String column : columnSet) {
                if (columnMap.containsKey(column)) {
                    HiveColumn hiveColumn = columnMap.get(column);
                    if (hiveColumn.hasAliasName()) {
                        sb.append(hiveColumn.getRawName()).append(" AS ").append(column);
                    } else if (hiveColumn.hasDefaultValue()) {
                        sb.append(hiveColumn.getDefalutValue()).append(" AS ").append(column);
                    } else {
                        sb.append(hiveColumn.getName());
                    }
                } else {
                    sb.append("'' AS ").append(column);
                }
                if (++columnCnt < columnSize) {
                    sb.append(", ");
                }
            }
            sb.append(" FROM `").append(parser.getSourceTableName()).append("`");
            if (parser.getWhere() != null) {
                sb.append(" where ").append(getConditionString(parser.getWhere().getChild(0)));
            }
            if (++parserCnt < parserSize) {
                sb.append("\nUNION ALL\n");
            }
        }
        return sb.toString();
    }

    private static String getSetString(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        int size = set.size();
        int cnt = 0;
        for (String column : set) {
            sb.append(RECORD_NAME).append(".").append(column);
            if (++cnt < size) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static String getConditionString(Tree root) {
        if (root.getChildCount() == 2) {
            if (root.getType() == HiveParser.TOK_FUNCTION) {
                return String.format("%s(%s)", getConditionString(root.getChild(0)), getConditionString(root.getChild(1)));
            } else {
                return String.format("%s %s %s", getConditionString(root.getChild(0)), root.getText(), getConditionString(root.getChild(1)));
            }
        } else if (root.getChildCount() == 1 && HiveParser.TOK_TABLE_OR_COL == root.getType()) {
            return HiveInsertFromSelectParser.getTokTableOrTypeString(root);
        } else if (root.getChildCount() == 0) {
            return root.getText();
        } else {
            return "";
        }
    }

    public static void main(String[] args) throws Exception {
        TaskConfigParser parse = TaskConfigParser.getInstance();
        UnionHiveTask unionTask = parse.getUnionHiveTask("search4supplyUnionTabs");
        System.out.println(unionTask.getUnionSql());
        HiveInsertFromSelectParser unionParser = unionTask.getSQLParserResult(new TemplateContext(null));
        List<HiveColumn> columns = unionParser.getColsExcludePartitionCols();
        String blank = "                                         ";
        for (HiveColumn c : unionParser.getCols()) {
            System.out.println("<field name=\"" + c.getName() + "\" " + StringUtils.substring(blank, 0, 20 - StringUtils.length(c.getName())) + " type=\"string\" stored=\"true\" indexed=\"false\" />");
        }
    }
}

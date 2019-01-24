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
import com.qlangtech.tis.dump.hive.HiveTableBuilder;
import org.antlr.runtime.tree.Tree;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 宽表解析成AST之后的遍历语意树之后生成的语义模型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveInsertFromSelectParser {

    private final List<HiveColumn> cols = new ArrayList<>();

    private final Map<String, HiveColumn> colsMap = new HashMap<>();

    private int colIndex = 0;

    private String targetTableName;

    private String sourceTableName;

    private List<String> ps = new ArrayList<>();

    private ASTNode where;

    private static final ParseDriver parseDriver;

    private static final Context parseContext;

    static {
        try {
            parseDriver = new ParseDriver();
            Configuration config = new Configuration();
            config.set("_hive.hdfs.session.path", "/user");
            config.set("_hive.local.session.path", "/user");
            parseContext = new Context(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public List<String> getPs() {
        return Collections.unmodifiableList(ps);
    }

    public void setPs(List<String> ps) {
        this.ps = ps;
    }

    public List<HiveColumn> getCols() {
        return cols;
    }

    public Map<String, HiveColumn> getColsMap() {
        return colsMap;
    }

    public ASTNode getWhere() {
        return where;
    }

    public void setWhere(ASTNode where) {
        this.where = where;
    }

    /**
     * 除去ps列
     *
     * @return
     */
    public List<HiveColumn> getColsExcludePartitionCols() {
        List<HiveColumn> result = new ArrayList<>();
        for (HiveColumn c : getCols()) {
            if (!this.ps.contains(c.getName())) {
                result.add(c);
            }
        }
        return result;
    }

    private void parseCreateTable(Node node) {
        ASTNode astNode = null;
        for (Node n : node.getChildren()) {
            astNode = ((ASTNode) n);
            if (HiveParser.TOK_QUERY == astNode.getType()) {
                parseCreateTable(astNode);
            }
            // 20170201
            if (HiveParser.TOK_FROM == astNode.getType()) {
                parseSourceTable(astNode, false);
            }
            if (HiveParser.TOK_INSERT == astNode.getType()) {
                parseCreateTable(astNode);
            }
            if (HiveParser.TOK_DESTINATION == astNode.getType()) {
                parseTargetTable(astNode, false, false);
            }
            if (HiveParser.TOK_SELECT == astNode.getType()) {
                parseCreateTable(astNode);
            }
            if (HiveParser.TOK_SELEXPR == astNode.getType()) {
                parseColumn(astNode);
            }
            // 20170201
            if (HiveParser.TOK_WHERE == astNode.getType()) {
                where = astNode;
            }
        }
    }

    private void parseSourceTable(ASTNode node, boolean isTableName) {
        for (Node n : node.getChildren()) {
            ASTNode astNode = (ASTNode) n;
            if (HiveParser.TOK_TABREF == astNode.getType()) {
                parseSourceTable(astNode, false);
                continue;
            }
            if (HiveParser.TOK_TABNAME == astNode.getType()) {
                parseSourceTable(astNode, true);
                continue;
            }
            if (isTableName) {
                setSourceTableName(astNode.getText());
            }
        }
    }

    private void parseTargetTable(ASTNode node, boolean isTableName, boolean isPartition) {
        ASTNode astNode = null;
        for (Node n : node.getChildren()) {
            astNode = (ASTNode) n;
            if (HiveParser.TOK_TAB == astNode.getType()) {
                parseTargetTable(astNode, false, false);
                continue;
            }
            if (HiveParser.TOK_TABNAME == astNode.getType()) {
                parseTargetTable(astNode, true, false);
                continue;
            }
            if (HiveParser.TOK_PARTSPEC == astNode.getType()) {
                parseTargetTable(astNode, false, false);
                continue;
            }
            if (HiveParser.TOK_PARTVAL == astNode.getType()) {
                parseTargetTable(astNode, false, true);
                continue;
            }
            if (isPartition) {
                ps.add(astNode.getText());
            }
            if (isTableName) {
                targetTableName = astNode.getText();
            }
        }
    }

    /*
	 * colume有三种状态 '' AS id id1 AS id id record.id
	 */
    private void parseColumn(ASTNode astNode) {
        HiveColumn column = new HiveColumn();
        for (Node n : astNode.getChildren()) {
            ASTNode node = (ASTNode) n;
            if (HiveParser.Identifier == node.getType()) {
                column.setName(node.getText());
            } else if (HiveParser.TOK_TABLE_OR_COL == node.getType()) {
                column.setRawName(getTokTableOrTypeString(node));
            } else if (HiveParser.StringLiteral == node.getType()) {
                column.setDefalutValue(node.getText());
            } else if (HiveParser.DOT == node.getType()) {
                column.setRawName(node.getChild(1).getText());
            }
        }
        column.setIndex(colIndex++);
        column.setType(HiveTableBuilder.HIVE_TYPE_STRING);
        this.cols.add(column);
        this.colsMap.put(column.getName(), column);
    }

    public static String getTokTableOrTypeString(Tree node) {
        assert (HiveParser.TOK_TABLE_OR_COL == node.getType());
        return node.getChild(0).getText();
    }

    public static void main(String[] args) throws Exception {
        // System.out.println(.getClass());
        HiveInsertFromSelectParser parse = new HiveInsertFromSelectParser();
        // parse.start("INSERT OVERWRITE TABLE union_tab_name PARTITION (col1,col2)\n"
        // +
        // "SELECT bb.id, bb.goods_id, bb.entity_id, bb.self_entity_id, bb.is_valid,
        // bb.sg_supplier_id, bb.wg_warehouse_id FROM (\n"
        // +
        // "SELECT id1 as id, aa as goods_id, entity_id, self_entity_id, is_valid,
        // sg_supplier_id, null as wg_warehouse_id FROM supplier_goods WHERE id=1\n"
        // +
        // "UNION ALL\n" +
        // "SELECT id, goods_id, entity_id, self_entity_id, is_valid, null as
        // sg_supplier_id, wg_warehouse_id FROM warehouse_goods WHERE id=1) as bb");
        parse.start("INSERT OVERWRITE TABLE totalpay_summary PARTITION (pt,pmod)\n" + "  SELECT tp.totalpay_id,tp.curr_date,tp.outfee,tp.source_amount,tp.discount_amount,tp.coupon_discount\n" + "         ,tp.result_amount,tp.recieve_amount,tp.ratio,tp.status,tp.entity_id\n" + "         ,tp.is_valid,tp.op_time,tp.last_ver,tp.op_user_id,tp.discount_plan_id\n" + "         ,tp.operator,tp.operate_date,tp.card_id,tp.card,tp.card_entity_id\n" + "         ,tp.is_full_ratio,tp.is_minconsume_ratio,tp.is_servicefee_ratio,tp.invoice_code\n" + "         ,tp.invoice_memo,tp.invoice,tp.over_status,tp.is_hide,tp.load_time,tp.modify_time\n" + "         ,o.order_id,o.seat_id,o.area_id,o.is_valido ,o.instance_count,o.all_menu,o.all_fee,o.people_count \n" + "         ,o.order_from \n" + "         ,o.order_kind\n" + "         ,o.inner_code\n" + "         ,o.open_time\n" + "         ,o.end_time\n" + "         ,o.order_status\n" + "         ,o.code,o.seat_code\n" + "         ,p.kindpay\n" + "         ,sp.special_fee_summary\n" + "         ,cc.code as card_code,cc.inner_code as card_inner_code,cc.customer_id as card_customer_id\n" + "         ,cc.name as card_customer_name,cc.spell AS card_customer_spell\n" + "         ,cc.mobile AS card_customer_moble,cc.phone AS card_customer_phone\n" + "         ,tp.pt,tp.pmod\n" + "    FROM totalpay tp INNER JOIN order_instance o ON (tp.totalpay_id = o.totalpay_id and length(o.all_menu)<10000)\n" + "                     LEFT JOIN tmp_pay p ON (tp.totalpay_id = p.totalpay_id)\n" + "                     LEFT JOIN tmp_group_specialfee sp ON( tp.totalpay_id = sp.totalpay_id )\n" + "                     LEFT JOIN tmp_customer_card AS cc on(tp.card_id = cc.id AND tp.entity_id= cc.entity_id)");
        List<HiveColumn> columns = parse.getColsExcludePartitionCols();
        int i = 1;
    }

    /**
     * @throws IOException
     * @throws ParseException
     */
    public void start(String sql) throws IOException, ParseException {
        try {
            ASTNode astNode = parseDriver.parse(sql, parseContext, true);
            // System.out.println(astNode.dump());
            parseCreateTable(astNode);
        } catch (Exception e) {
            throw new RuntimeException(sql, e);
        }
    // System.out.println("targetTableName:");
    // System.out.println(this.targetTableName);
    // System.out.println("partition:");
    // for (String p : this.ps) {
    // System.out.println(p);
    // }
    // 
    // for (HiveColumn column : this.cols) {
    // System.out.println(column.getName() + ":" + column.getType());
    // }
    }
}

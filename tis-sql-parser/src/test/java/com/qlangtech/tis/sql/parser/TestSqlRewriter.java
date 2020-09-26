/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.sql.parser;

import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.Expression;
import com.google.common.collect.ImmutableMap;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.order.center.TestJoinTaskContext;
import com.qlangtech.tis.sql.parser.SqlRewriter.AliasTable;
import com.qlangtech.tis.sql.parser.SqlRewriter.RewriterDumpTable;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta.SqlDataFlowTopology;
import com.qlangtech.tis.sql.parser.er.ERRules;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年8月27日
 */
public class TestSqlRewriter extends TestCase {

    private final SqlParser sqlParser = new SqlParser();

    private static final Map<IDumpTable, ITabPartition> tabPartition;

    private static final SqlDataFlowTopology topology;

    // private static final Map<EntityName, ERRules.TabFieldProcessor> tabFieldProcessorMap;
    private static final ERRules totalpayERRules;

    static {
        System.setProperty("data.dir", "dataflow");
        ImmutableMap.Builder<IDumpTable, ITabPartition> mapBuilder = ImmutableMap.builder();
        // 全部是原始DUMP表
        mapBuilder.put(RewriterDumpTable.create("order", "orderdetail"), () -> "20190827111159");
        mapBuilder.put(RewriterDumpTable.create("order", "order_bill"), () -> "20190828111159");
        mapBuilder.put(RewriterDumpTable.create("tis", "order_customers"), () -> "20190829111159");
        mapBuilder.put(RewriterDumpTable.create("tis", "takeout_order_extra"), () -> "2019083027111159");
        mapBuilder.put(RewriterDumpTable.create("tis", "ent_expense_order"), () -> "2019073027111159");
        mapBuilder.put(RewriterDumpTable.create("tis", "ent_expense"), () -> "20190730111159");
        mapBuilder.put(RewriterDumpTable.create("xxxxx", "instancedetail"), () -> "20190630111159");
        mapBuilder.put(RewriterDumpTable.create("aaaa", "card"), () -> "20190530111159");
        mapBuilder.put(RewriterDumpTable.create("member", "customer"), () -> "20190430111159");
        mapBuilder.put(RewriterDumpTable.create("bbb", "specialfee"), () -> "20190230111159");
        mapBuilder.put(RewriterDumpTable.create("ccc", "payinfo"), () -> "20190330111159");
        mapBuilder.put(RewriterDumpTable.create("kkkk", "totalpayinfo"), () -> "20180330111159");
        mapBuilder.put(RewriterDumpTable.create("hhhh", "order_instance"), () -> "20180329111159");
        mapBuilder.put(RewriterDumpTable.create("yyyyy", "tmp_pay"), () -> "20180328111159");
        mapBuilder.put(RewriterDumpTable.create("yyyyy", "tmp_group_specialfee"), () -> "20180328111159");
        mapBuilder.put(RewriterDumpTable.create("uuuu", "tmp_customer_card"), () -> "20180328111159");
        mapBuilder.put(RewriterDumpTable.create("oooo", "servicebillinfo"), () -> "20180327111159");
        mapBuilder.put(RewriterDumpTable.create("oooo", "card_expense_relative"), () -> "20180326111159");
        mapBuilder.put(RewriterDumpTable.create("shop", "mall_shop"), () -> "20180325111159");
        tabPartition = mapBuilder.build();
        try {
            topology = SqlTaskNodeMeta.getSqlDataFlowTopology("totalpay");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Optional<ERRules> totalpayERRulesOption = ERRules.getErRule("totalpay");
        totalpayERRules = totalpayERRulesOption.get();
    // tabFieldProcessorMap = totalpayERRules.getTabFieldProcessors().stream().collect(Collectors.toMap((r) -> r.tabName, (r) -> r));
    }

    public void testTmp_pay() {
        final String tmpPay = "tmp_pay";
        SqlTaskNodeMeta nodeMeta = getSqlTaskNodeMeta(tmpPay);
        TestJoinTaskContext taskContext = new TestJoinTaskContext();
        assertFull(tmpPay, nodeMeta, taskContext, nodeMeta.getSql());
    }

    public void testOrder_instance() {
        final String order_instance = "order_instance";
        SqlTaskNodeMeta nodeMeta = getSqlTaskNodeMeta(order_instance);
        TestJoinTaskContext taskContext = new TestJoinTaskContext();
        assertFull(order_instance, nodeMeta, taskContext, nodeMeta.getSql());
    }

    public void testTotalpaySummaryRewrite() {
        final String totalpaySummary = "totalpay_summary";
        SqlTaskNodeMeta nodeMeta = getSqlTaskNodeMeta(totalpaySummary);
        final int sharedCount = 999;
        TestJoinTaskContext taskContext = new TestJoinTaskContext() {

            @Override
            public int getIndexShardCount() {
                return sharedCount;
            }
        };
        // taskContext.setAttribute(IFullBuildContext.KEY_APP_SHARD_COUNT, String.valueOf(999));
        // Optional<ERRules> totalpay = ERRules.getErRule("totalpay");
        // totalpay.get().getTabFieldProcessors()
        // 过滤依赖的表有ExtraMeta属性，且有colTransfer属性
        // Map<EntityName, ERRules.TabFieldProcessor> dumpNodeExtraMetaMap = SqlFormatter.getDumpNodeExtraMetaMap(nodeMeta);
        // assertEquals(1, dumpNodeExtraMetaMap.size());
        assertFull(totalpaySummary, true, nodeMeta, taskContext, nodeMeta.getSql());
    }

    public void testRewriteTable() throws Exception {
        TestJoinTaskContext taskContext = new TestJoinTaskContext();
        // taskContext
        topology.getNodeMetas().stream().forEach((meta) -> {
            // waitProcessAliasTabsSetSize);
            if ("totalpay_summary".equals(meta.getExportName())) {
                return;
            }
            assertFull(meta.getExportName(), meta, taskContext, meta.getSql());
        });
        String extraSql = processFileContent(getScriptContent("sqlrewrite.txt"));
        String extraSqlAssert = processFileContent(getScriptContent("sqlrewrite_assert.txt"));
        MetaContent meta = this.getMetaContent("sqlrewrite_meta.txt");
        final String orderInstance = "order_instance";
        SqlTaskNodeMeta nodeMeta = getSqlTaskNodeMeta(orderInstance);
        rewriteAssert(orderInstance, nodeMeta, extraSqlAssert, extraSql, meta, taskContext);
    }

    private SqlTaskNodeMeta getSqlTaskNodeMeta(String orderInstance) {
        Optional<SqlTaskNodeMeta> first = topology.getNodeMetas().stream().filter((r) -> orderInstance.equals(r.getExportName())).findFirst();
        if (!first.isPresent()) {
            throw new IllegalStateException("can not find " + orderInstance);
        }
        return first.get();
    }

    private void assertFull(String exportName, SqlTaskNodeMeta nodeMeta, TestJoinTaskContext taskContext, String... extraSql) {
        assertFull(exportName, false, nodeMeta, taskContext, extraSql);
    }

    private void assertFull(String exportName, boolean finalNode, SqlTaskNodeMeta nodeMeta, TestJoinTaskContext taskContext, String... extraSql) {
        if (extraSql.length < 1) {
            extraSql = new String[] { processFileContent(getScriptContent(exportName + ".txt")) };
        }
        String extraSqlAssert = processFileContent(getScriptContent(exportName + "_assert.txt"));
        // int waitProcessAliasTabsSetSize = 0;
        // try {
        // waitProcessAliasTabsSetSize = Integer.parseInt(getScriptContent(exportName +
        // "_meta.txt"));
        // } catch (NumberFormatException e) {
        // }
        MetaContent metaContent = this.getMetaContent(exportName + "_meta.txt");
        // try {
        rewriteAssert(exportName, finalNode, nodeMeta, extraSqlAssert, extraSql[0], metaContent, taskContext);
    // } catch (Throwable e) {
    // throw new RuntimeException(exportName + "\n" + extraSql[0], e);
    // }
    }

    protected SqlRewriter rewriteAssert(String exportName, SqlTaskNodeMeta nodeMeta, String extraSqlAssert, final String extraSql, MetaContent metaContent, TestJoinTaskContext taskContext) {
        return rewriteAssert(exportName, false, nodeMeta, extraSqlAssert, extraSql, metaContent, taskContext);
    }

    protected SqlRewriter rewriteAssert(String exportName, boolean isFinal, SqlTaskNodeMeta nodeMeta, String extraSqlAssert, final String extraSql, MetaContent metaContent, TestJoinTaskContext taskContext) {
        SqlStringBuilder builder;
        SqlRewriter rewriter;
        Optional<List<Expression>> parameters = Optional.empty();
        Assert.assertNotNull(extraSqlAssert);
        Assert.assertNotNull(extraSql);
        builder = new SqlStringBuilder();
        // totalpayERRules.getPrimaryTab();
        // totalpayERRules.get
        // totalpayERRules
        // DefaultChainContext
        rewriter = new SqlRewriter(builder, tabPartition, totalpayERRules, parameters, isFinal, taskContext);
        // 执行rewrite
        rewriter.process(sqlParser.createStatement(extraSql, new ParsingOptions()), 0);
        final String rewriteSql = processFileContent(builder.toString());
        System.out.println("<<" + exportName);
        System.out.println(extraSql);
        System.out.println("---------------------------------------------");
        System.out.println(rewriteSql);
        System.out.println("==========================");
        // ▼▼▼▼▼▼ 终极Assert
        assertEquals(exportName, extraSqlAssert, rewriteSql);
        // ▲▲▲▲▲▲
        Assert.assertEquals(exportName, metaContent.waitProcessAliasTabsSetSize, rewriter.waitProcessAliasTabsSet.size());
        for (AliasTable a : rewriter.waitProcessAliasTabsSet) {
            Assert.assertTrue("exportName:" + exportName + "," + a.toString(), a.isPtRewriterOver());
        }
        AliasTable primaryTable = rewriter.getPrimayTable();
        Assert.assertNotNull(exportName, primaryTable);
        Assert.assertEquals(exportName, metaContent.exportPartition, primaryTable.getTabPartition());
        return rewriter;
    }

    public static String processFileContent(String content) {
        return content.replace("\r\n", "\n");
    }

    public static String getScriptContent(String fileName) {
        try {
            // orderInstance.get().getSql();
            String extraSql = null;
            try (InputStream input = TestSqlRewriter.class.getResourceAsStream(fileName)) {
                extraSql = processFileContent(IOUtils.toString(input, TisUTF8.get()));
            }
            return extraSql;
        } catch (Exception e) {
            throw new RuntimeException("fileName:" + fileName, e);
        }
    }

    public MetaContent getMetaContent(String fileName) {
        MetaContent result = new MetaContent();
        LineIterator it = null;
        try {
            int index = 0;
            try (InputStream input = TestSqlRewriter.class.getResourceAsStream(fileName)) {
                it = IOUtils.lineIterator(input, TisUTF8.get());
                while (it.hasNext()) {
                    if (index < 1) {
                        index++;
                        try {
                            result.waitProcessAliasTabsSetSize = Integer.parseInt(it.nextLine());
                        } catch (Throwable e) {
                        }
                    } else if (index == 1) {
                        result.exportPartition = StringUtils.trim(it.next());
                    } else {
                        break;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("fileName:" + fileName, e);
        }
    }

    public static class MetaContent {

        public int waitProcessAliasTabsSetSize;

        public String exportPartition = "";
    }
}

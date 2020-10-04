package com.qlangtech.tis.sql.parser;

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: baisui 百岁
 * @create: 2020-10-02 07:36
 **/
public class TabPartitions {
    private final Map<IDumpTable, ITabPartition> tabPartition;

    public TabPartitions(Map<IDumpTable, ITabPartition> tabPartition) {
        this.tabPartition = tabPartition;
    }

    public int size() {
        return tabPartition.size();
    }

    public final Optional<Map.Entry<IDumpTable, ITabPartition>> findTablePartition(String dbName, String tableName) {
        return findTablePartition(true, dbName, tableName);
    }


    protected Optional<Map.Entry<IDumpTable, ITabPartition>> findTablePartition(boolean dbNameCriteria, String dbName, String tableName) {
        return tabPartition.entrySet().stream().filter((r) -> {
            return (!dbNameCriteria || StringUtils.equals(r.getKey().getDbName(), dbName))
                    && StringUtils.equals(r.getKey().getTableName(), tableName);
        }).findFirst();
    }

    public final Optional<Map.Entry<IDumpTable, ITabPartition>> findTablePartition(String tableName) {
        return this.findTablePartition(false, null, tableName);
    }

    public String joinFullNames() {
        return tabPartition.keySet().stream().map((r) -> r.getFullName()).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.tabPartition.entrySet().stream().map((ee) -> "[" + ee.getKey() + "->" + ee.getValue().getPt() + "]").collect(Collectors.joining(","));
    }
}

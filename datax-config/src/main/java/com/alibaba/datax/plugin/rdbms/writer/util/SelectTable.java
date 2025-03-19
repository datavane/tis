package com.alibaba.datax.plugin.rdbms.writer.util;

import com.qlangtech.tis.plugin.ds.IDBReservedKeys;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-10-03 10:34
 **/
public class SelectTable extends EscapeableEntity {

    private final String tabName;

    public static SelectTable create(String table, IDBReservedKeys escapeChar) {
        return new SelectTable(table, escapeChar);
    }

    public String getTabName() {
        return this.escapeEntity(tabName);
    }

    public String getUnescapeTabName() {
        return this.tabName;
    }

    @Override
    public String toString() {
        return getTabName();
    }

    public SelectTable(String tabName, IDBReservedKeys escapeChar) {
        super(escapeChar);
        if (StringUtils.isEmpty(tabName)) {
            throw new IllegalArgumentException("param tabName can not be empty");
        }
        this.tabName = tabName;
    }
}

package com.qlangtech.tis.sql.parser.exception;

import com.facebook.presto.sql.tree.NodeLocation;

import java.util.Optional;

/**
 * 代表在编写joinSql,的sql语句中中存在的错误
 *
 * @author: baisui 百岁
 * @create: 2020-09-30 14:09
 **/
public class TisSqlFormatException extends RuntimeException {
    private final Optional<NodeLocation> location;

    public TisSqlFormatException(String msg, Optional<NodeLocation> location) {
        super(msg);
        this.location = location;
    }

    public String summary() {
        if (location.isPresent()) {
            return this.getMessage() + ",位置，行:" + location.get().getLineNumber() + ",列:" + location.get().getColumnNumber();
        } else {
            return this.getMessage();
        }
    }
}

package com.qlangtech.tis.plugin.ds;

import com.alibaba.fastjson.annotation.JSONField;

public interface IDbMeta {
    @JSONField(serialize = false)
    String getFormatDBName();

    String getName();

    public String getDAOJarName();
}

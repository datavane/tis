package com.qlangtech.tis.datax;

import com.qlangtech.tis.plugin.IdentityName;

/**
 *
 */
public interface IDataxGlobalCfg extends IdentityName {
    int getChannel();

    int getErrorLimitCount();

    float getErrorLimitPercentage();

    /**
     * 默认的模版
     *
     * @return
     */
    public static String getDefaultTemplate() {
        return com.qlangtech.tis.extension.impl.IOUtils.loadResourceFromClasspath(
                IDataxGlobalCfg.class, "datax-tpl.vm");
    }

    String getTemplate();
}

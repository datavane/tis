package com.qlangtech.tis.order.center;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 安装系统的时候需要进行初始化
 *
 * @author: baisui 百岁
 * @create: 2020-10-20 10:12
 **/
public class InitSys {
    private static final Logger logger = LoggerFactory.getLogger(InitSys.class);

    public static void main(String[] args) throws Exception {
        IndexSwapTaskflowLauncher launcher = new IndexSwapTaskflowLauncher();
        launcher.afterPropertiesSet();
        logger.info("zk initialize has successful");
    }
}

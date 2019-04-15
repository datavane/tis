package com.qlangtech.tis.health.check;

public interface IStatusChecker {
	/**
     * 初始化
     */
    void init();

    /**
     * 自己在StatusChecker 列表的排序位置，越靠前越小<br>
     * 从1开始
     * 
     * @return
     */
    int order();

    /**
     * StatusChecker支持的模型
     * 
     * @return
     * @see com.dihuo.app.common.monitor.enums.Mode
     */
    Mode mode();

    /**
     * 具体业务实现类，实现监控检查
     * 
     * @return
     */
    StatusModel check();
}

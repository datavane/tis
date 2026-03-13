package com.qlangtech.tis.plugins.incr.flink.cdc.pipeline;

import com.qlangtech.tis.plugin.datax.SelectedTab;

import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/13
 */
public interface ICDCPipelineTableOptionsCreator {
    public Function<SelectedTab, Map<String, String>> createTabOpts();
}

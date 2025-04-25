package com.alibaba.datax.core.job;

import com.qlangtech.tis.datax.IDataXNameAware;
import com.qlangtech.tis.datax.IDataXTaskRelevant;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-02-23 10:06
 **/
public interface IJobContainerContext extends IDataXTaskRelevant, IDataXNameAware, ISourceTable {
    <T extends ITransformerBuildInfo> Optional<T> getTransformerBuildCfg();


}

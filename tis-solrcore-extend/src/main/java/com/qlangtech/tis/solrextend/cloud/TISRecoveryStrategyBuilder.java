package com.qlangtech.tis.solrextend.cloud;

import org.apache.solr.cloud.RecoveryStrategy;
import org.apache.solr.cloud.RecoveryStrategy.RecoveryListener;
import org.apache.solr.common.cloud.ZkNodeProps;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;

/**
 * @author 百岁
 *
 * @date 2019年1月30日
 */
public class TISRecoveryStrategyBuilder extends RecoveryStrategy.Builder {

	@Override
	protected RecoveryStrategy newRecoveryStrategy(CoreContainer cc, CoreDescriptor cd,
			RecoveryListener recoveryListener) {
		return new TISRecoveryStrategy(cc, cd, recoveryListener);
	}

	private class TISRecoveryStrategy extends RecoveryStrategy {

		protected TISRecoveryStrategy(CoreContainer cc, CoreDescriptor cd, RecoveryListener recoveryListener) {
			super(cc, cd, recoveryListener);

		}

	}

}

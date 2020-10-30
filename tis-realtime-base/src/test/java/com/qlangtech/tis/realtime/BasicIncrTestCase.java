package com.qlangtech.tis.realtime;

import com.qlangtech.tis.cloud.ITisCloudClientFactory;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.realtime.transfer.BasicRMListener;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.sql.parser.DBNode;
import com.qlangtech.tis.wangjubao.jingwei.Table;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: baisui 百岁
 * @create: 2020-10-16 14:43
 **/
public abstract class BasicIncrTestCase extends BasicTestCase {
    protected ApplicationContext appContext;
    protected BasicRMListener listenerBean = null;

    static {
        BasicRMListener.CloudClientTypeCode = ITisCloudClientFactory.TEST_MOCK;
    }

    @Override
    protected final Table getTableRowProcessor(String tabName) {
        return listenerBean.getTableProcessor(tabName);
    }

    public BasicIncrTestCase(boolean shallRegisterMQ, String collectionName, long wfTimestamp, String... configLocations) {

        // com.qlangtech.tis.realtime.transfer.MQListenerFactory 中不会启动mq去读mq的消息
        Config.setTest(!shallRegisterMQ);
        AbstractTisCloudSolrClient.initHashcodeRouter();
        try {
            final TisIncrLauncher incrLauncher = new TisIncrLauncher(collectionName, wfTimestamp, false);
            incrLauncher.downloadDependencyJarsAndPlugins();

            // 启动增量任务
            BeanFactory incrContainer = incrLauncher.launchIncrChannel();
            listenerBean = incrContainer.getBean(BasicRMListener.class);
            assertNotNull(listenerBean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        appContext = new // /
                ClassPathXmlApplicationContext(configLocations//"/conf/order-test-dao-context.xml", "/conf/member-test-dao-context.xml", "/conf/shop-test-dao-context.xml"
                ) {
                    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
                        // DataSourceRegister.setApplicationContext(factory,
                        // dbMetaList);
                        DBNode.registerDependencyDbsFacadeConfig(collectionName, wfTimestamp, factory);
                        // SpringDBRegister dbRegister = new SpringDBRegister(dbLinkMetaData.getName(), dbLinkMetaData, factory);
                        // dbRegister.visitAll();
                        // registerExtraBeanDefinition(factory);
                        super.prepareBeanFactory(beanFactory);
                    }
                };
        System.out.println("create listenerBean successful");
    }

}

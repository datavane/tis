/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer;

import org.apache.commons.modeler.BaseModelMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import java.net.Inet4Address;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月23日 上午11:18:18
 */
public class TransferStatusMBean extends BaseModelMBean implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(TransferStatusMBean.class);

    private final List<String> indexNames;

    private final Map<String, IOnsListenerStatus> listernsMap;

    private final Map<String, String> /* index UUID */
    indexUUID;

    public final Collection<IOnsListenerStatus> getAllIncrListener() {
        return Collections.unmodifiableCollection(listernsMap.values());
    }

    public TransferStatusMBean(List<String> indexNames, Collection<IOnsListenerStatus> listerns) throws MBeanException, RuntimeOperationsException {
        super();
        if (indexNames.size() > listerns.size()) {
            throw new IllegalStateException("indexNames size:" + indexNames.size() + " is big than listener size:" + listerns.size());
        }
        this.indexNames = indexNames;
        this.listernsMap = new HashMap<>();
        this.indexUUID = new HashMap<>();
        StringBuffer indexName = new StringBuffer();
        for (String s : indexNames) {
            indexName.append(s).append(",");
        }
        IOnsListenerStatus onsListerner = null;
        StringBuffer loadedConsume = new StringBuffer();
        try {
            for (IOnsListenerStatus l : listerns) {
                // (BasicONSListener) consumeField.get(l);
                onsListerner = l;
                listernsMap.put(onsListerner.getCollectionName(), onsListerner);
                loadedConsume.append(onsListerner.getCollectionName()).append(",");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("loaded index:" + indexName);
        log.info("loaded consume listener:" + loadedConsume.toString());
    }

    // http://www.boyunjian.com/javasrc/org.apache.tomcat/tomcat-catalina/7.0.33/_/org/apache/catalina/mbeans/GroupMBean.java
    @Override
    public void afterPropertiesSet() throws Exception {
        // URL url = this.getClass().getResource("/conf/mbeans-descriptors.xml");
        // Registry registry = Registry.getRegistry();
        // registry.loadMetadata(url);
        // ManagedBean manageBean = registry.findManagedBean("transferStatus");
        // Group group = ... managed component instance ...;
        // MBeanServer mserver = registry.getMBeanServer();
        // String oname = "Users:name=Group";
        // final int port = NetUtils.getFreeSocketPort();
        // final HtmlAdaptorServer htmlAdaptor = new HtmlAdaptorServer();
        // htmlAdaptor.setPort(50001);
        // registry.registerComponent(htmlAdaptor, "tis:type=increase,name=htmladapter", null);
        // htmlAdaptor.start();
        // 这个变量应该要能拿到
        // final String incrExecGroup = StringUtils.defaultIfEmpty(
        // System.getenv(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP),
        // System.getProperty(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP));
        // if (StringUtils.isBlank(incrExecGroup)) {
        // throw new IllegalStateException("incrExecGroup can not be null");
        // }
        IOnsListenerStatus onsListener = null;
        for (String index : indexNames) {
            // 这个随便写
            String oname = "tis:type=increase,name=" + index;
            onsListener = listernsMap.get(index);
            if (onsListener == null) {
                throw new IllegalStateException("index:" + index + " relevant onsListener can not be null");
            }
            // registry.registerComponent(onsListener, oname, "transferStatus");
            // final String collectionName = index;
            // final String zkPath = "/tis/incr-transfer-group/" + incrExecGroup
            // + "/"
            // + collectionName;
            // ZkUtils.registerAddress2ZK(zookeeper, zkPath, port);
            // final String zkPath = "/tis/incr-transfer-group/" + index +
            // "/consume";
            // .toString();
            String uuid = String.valueOf(UUID.randomUUID());
            // if (StringUtils.equals("127.0.0.1", ip)) {
            // throw new IllegalStateException("ip can not be 127.0.0.1");
            // }
            // JSONObject registerContent = new JSONObject();
            // registerContent.put("id", uuid);
            // registerContent.put("group", incrExecGroup);
            // registerContent.put("host", ip);
            // registerContent.put("jmxport", port);
            // ZkUtils.registerTemporaryContent(zookeeper, zkPath,
            // registerContent.toString(1));
            this.indexUUID.put(index, uuid);
        }
        log.info("mbean initial successful");
    }

    public Map<String, String> getIndexUUID() {
        return indexUUID;
    }

    public String getUniqueId(String collection) {
        return this.getIndexUUID().get(collection);
    }

    public static void main(String[] args) throws Exception {

    }
}

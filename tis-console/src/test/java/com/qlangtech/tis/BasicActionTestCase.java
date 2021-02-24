/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.spring.EnvironmentBindService;
import com.qlangtech.tis.manage.spring.MockClusterStateReader;
import com.qlangtech.tis.manage.spring.MockZooKeeperGetter;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.cloud.*;
import org.apache.struts2.StrutsSpringTestCase;
import org.apache.zookeeper.data.Stat;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-06 18:04
 */
public class BasicActionTestCase extends StrutsSpringTestCase {

  private static List<Object> mocks = Lists.newArrayList();
  protected RunContext runContext;

  static {
    CenterResource.setNotFetchFromCenterRepository();
    HttpUtils.addMockGlobalParametersConfig();
  }

  protected void setCollection(String collection) {
    request.addHeader("appname", collection);
    DefaultFilter.AppAndRuntime app = new DefaultFilter.AppAndRuntime();
    app.setAppName(collection);
    DefaultFilter.setAppAndRuntime(app);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("==============execute setUp");
    this.clearMocks();
    EnvironmentBindService.cleanCacheService();
    this.runContext = Objects.requireNonNull(applicationContext.getBean("runContextGetter", RunContextGetter.class)
      , "runContextGetter can not be null").get();
  }

  protected void clearMocks() {
    this.mocks = Lists.newArrayList();
  }

  protected void verifyAll() {
    this.mocks.forEach((r) -> {
      EasyMock.verify(r);
    });
  }

  public <T> T mock(String name, Class<?> toMock) {
    Object mock = EasyMock.createMock(name, toMock);
    this.mocks.add(mock);
    return (T) mock;
  }

  public void replay() {
    mocks.forEach((r) -> {
      EasyMock.replay(r);
    });
  }

  protected AjaxValve.ActionExecResult showBizResult() {
    AjaxValve.ActionExecResult actionExecResult = MockContext.getActionExecResult();
    if (!actionExecResult.isSuccess()) {
      System.err.println(AjaxValve.buildResultStruct(MockContext.instance));
      // actionExecResult.getErrorPageShow()
    } else {
      System.out.println(AjaxValve.buildResultStruct(MockContext.instance));
    }
    return actionExecResult;
  }

  protected DocCollection createMockCollection(String collection) throws Exception {
    return this.createMockCollection(collection, true);
  }

  protected DocCollection createMockCollection(String collection, boolean fetchCollectionState) throws Exception {
    TISZkStateReader tisZkStateReader = this.mock("tisZkStateReader", TISZkStateReader.class);
    //getClusterState
    ClusterState clusterState = this.mock("clusterState", ClusterState.class);
    EasyMock.expect(tisZkStateReader.getClusterState()).andReturn(clusterState).anyTimes();

    ClusterState.CollectionRef collectionRef
      = this.mock(collection + "CollectionRef", ClusterState.CollectionRef.class);
    EasyMock.expect(clusterState.getCollectionRef(collection)).andReturn(collectionRef).anyTimes();
    DocCollection coll = null;
    coll = buildDocCollectionMock(false, fetchCollectionState, collection, tisZkStateReader);
    EasyMock.expect(collectionRef.get()).andReturn(coll).anyTimes();

    MockClusterStateReader.mockStateReader = tisZkStateReader;
    return coll;
  }

  protected static final String replica_core_url = "http://192.168.28.200:8080/solr/search4employees_shard1_replica_n1/";
  public static final String name_shard1 = "shard1";

  protected DocCollection buildDocCollectionMock(boolean getSlicesMap, boolean fetchCollectionState, String collectionName, TISZkStateReader tisZkStateReader) throws Exception {
    DocCollection docCollection = this.mock("docCollection", DocCollection.class);
    if (fetchCollectionState) {
      Map<String, Slice> sliceMap = Maps.newHashMap();
      Slice slice = this.mock("shard1Slice", Slice.class);
      Replica replica = this.mock("core_node2_replica", Replica.class);
      sliceMap.put(name_shard1, slice);
      IExpectationSetters<Collection<Replica>> collectionIExpectationSetters
        = EasyMock.expect(slice.getReplicas()).andReturn(Collections.singleton(replica));
      collectionIExpectationSetters.anyTimes();
//      if (!getSlicesMap) {
//        collectionIExpectationSetters.times(2);
//      }
      IExpectationSetters<Boolean> leaderSetters
        = EasyMock.expect(replica.getBool("leader", false)).andReturn(true);
      leaderSetters.anyTimes();
//      if (!getSlicesMap) {
//        leaderSetters.times(1);
//      }
      IExpectationSetters<String> getCoreUrlExpectationSetters
        = EasyMock.expect(replica.getCoreUrl()).andReturn(replica_core_url);
      getCoreUrlExpectationSetters.anyTimes();
      if (!getSlicesMap) {
        String shard1 = name_shard1;

        EasyMock.expect(slice.getName()).andReturn(name_shard1).anyTimes();

        EasyMock.expect(replica.getName()).andReturn(collectionName + "_shard1_replica_n1").anyTimes();
        EasyMock.expect(replica.getBaseUrl()).andReturn("baseurl").anyTimes();
        EasyMock.expect(replica.getCollection()).andReturn(collectionName).anyTimes();
        EasyMock.expect(replica.getCoreName()).andReturn(collectionName + "_shard1_replica_n1").anyTimes();
        EasyMock.expect(replica.getState()).andReturn(Replica.State.ACTIVE).anyTimes();
        EasyMock.expect(replica.getNodeName()).andReturn("192.168.28.200:8080_solr").anyTimes();
        EasyMock.expect(replica.getProperties()).andReturn(Collections.emptyMap()).anyTimes();
        EasyMock.expect(replica.getType()).andReturn(Replica.Type.NRT).anyTimes();
        EasyMock.expect(replica.getSlice()).andReturn(shard1).anyTimes();

        EasyMock.expect(docCollection.getName()).andReturn(collectionName).anyTimes();
        EasyMock.expect(docCollection.getSlices()).andReturn(sliceMap.values()).anyTimes();
      } else {
        //  getCoreUrlExpectationSetters.times(1);
        EasyMock.expect(replica.getBaseUrl()).andReturn("baseurl").anyTimes();
      }

      EasyMock.expect(docCollection.getSlicesMap()).andReturn(sliceMap);


      EasyMock.expect(tisZkStateReader.fetchCollectionState(collectionName, null))
        .andReturn(docCollection);
    }
    return docCollection;
  }

  protected IExpectationSetters<byte[]> createCoordinatorMock(Consumer<ITISCoordinator> consumer) throws IOException {
    return createCoordinatorMock(true, consumer);
  }

  protected IExpectationSetters<byte[]> createCoordinatorMock(boolean overseer_elect_leader, Consumer<ITISCoordinator> consumer) throws IOException {
    ITISCoordinator zkCoordinator = mock("zkCoordinator", ITISCoordinator.class);
    MockZooKeeperGetter.mockCoordinator = zkCoordinator;
    consumer.accept(zkCoordinator);
    if (overseer_elect_leader) {
      try (InputStream input = this.getClass().getResourceAsStream("/com/qlangtech/tis/overseer_elect_leader.json")) {
        IExpectationSetters<byte[]> expect = EasyMock.expect(
          zkCoordinator.getData(CoreAction.ZK_PATH_OVERSEER_ELECT_LEADER, null, new Stat(), true));
        expect.andReturn(IOUtils.toByteArray(input));
        return expect;
      }
    }
    return null;
  }

  @Override
  protected String[] getContextLocations() {
    return new String[]{"classpath:/tis.application.context.xml", "classpath:/tis.test.context.xml"};
  }
}

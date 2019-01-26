/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.coredefine.module.screen;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;

import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;
import com.qlangtech.tis.runtime.pojo.ServerGroupAdapter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
@InterceptorRefs({ @InterceptorRef("tisStack"), @InterceptorRef("serverGroupExist") })
public abstract class CoreDefineScreen extends BasicScreen {

    // private static final CoreManagerClient clientProtocol;
    private static final long serialVersionUID = 1L;

    private ServerGroupAdapter groupConfig;

    public ServerGroupAdapter getConfigGroup0() {
        if (groupConfig == null) {
            groupConfig = this.getServerGroup0();
        }
        return groupConfig;
    }

    /**
     * 该应用下的第0组配置
     *
     * @return
     */
    protected ServerGroupAdapter getServerGroup0() {
        final AppDomainInfo domain = CheckAppDomainExistValve.getAppDomain(this);
        List<ServerGroupAdapter> groupList = BasicScreen.createServerGroupAdapterList(new BasicScreen.ServerGroupCriteriaSetter() {

            @Override
            public void process(ServerGroupCriteria.Criteria criteria) {
                criteria.andAppIdEqualTo(domain.getAppid()).andRuntEnvironmentEqualTo(domain.getRunEnvironment().getId());
                criteria.andGroupIndexEqualTo((short) 0);
                // if (publishSnapshotIdIsNotNull) {
                criteria.andPublishSnapshotIdIsNotNull();
            // }
            }

            @Override
            public List<Server> getServers(RunContext daoContext, ServerGroup group) {
                return Collections.emptyList();
            }

            @Override
            public int getMaxSnapshotId(ServerGroup group, RunContext daoContext) {
                SnapshotCriteria snapshotCriteria = new SnapshotCriteria();
                snapshotCriteria.createCriteria().andAppidEqualTo(group.getAppId());
                return daoContext.getSnapshotDAO().getMaxSnapshotId(snapshotCriteria);
            }
        }, true, CoreDefineScreen.this);
        for (ServerGroupAdapter group : groupList) {
            return group;
        }
        return null;
    }

    // static {
    // 
    // coreManage = RpcCoreManageImpl.create();
    // coreManage = new MockRpcCoreManage();
    // coreManage = (RpcCoreManage) Proxy.newProxyInstance(
    // CoreDefineScreen.class.getClassLoader(),
    // new Class<?>[] { RpcCoreManage.class },
    // new InvocationHandler() {
    // @Override
    // public Object invoke(Object proxy, Method method,
    // Object[] args) throws Throwable {
    // 
    // method.getReturnType();
    // 
    // return null;
    // }
    // 
    // });
    // coreManage = new MockRpcCoreManage();
    // System
    // .setProperty("javax.xml.parsers.DocumentBuilderFactory",
    // "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    // 
    // Configuration config = new Configuration();
    // config.set("ns.default.name", "terminator://10.232.21.117:9000");
    // 
    // // ns.default.name=terminator://10.232.21.117:9000
    // 
    // 
    // try {
    // InetSocketAddress addr = new InetSocketAddress("10.232.21.117",
    // 9000);
    // ClientProtocol client = (ClientProtocol) RPC.waitForProxy(
    // ClientProtocol.class, 1, addr, new Configuration());
    // ClassPathResource resource = new ClassPathResource();
    // File confFile = new
    // File("D:\\study\\terminator\\terminator\\terminator-3.0-cloud\\terminator-cloud-client\\src\\test\\resources\\cn.properties");
    // ClassPathContextResource resouce = new
    // ClassPathContextResource("cn.properties",null);
    // DefaultResourceLoader loader = new DefaultResourceLoader();
    // Resource resourceByPath = loader.getResource("cn.properties");
    // File confFile = resourceByPath.getFile();
    // InputStream input = new FileInputStream(confFile);
    // Properties properties = new Properties();
    // properties.load(input);
    // for (Entry<Object, Object> entry : properties.entrySet()) {
    // config.set((String) entry.getKey(), (String) entry.getValue());
    // }
    // String cnURI =
    // properties.getProperty(NSConfigKeys.NS_DEFAULT_NAME_KEY);
    // CenterNodeAddressUtil.setDefaultUri(config, cnURI);
    // clientProtocol = CoreManagerClient.newInstance(config);
    // clientProtocol = new CoreManagerClient();
    // clientProtocol = EasyMock.createMock(CoreManagerClient.class);
    // clientProtocol = new CoreManagerClient();
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // @Override
    // public void execute(Context context) throws Exception {
    // 
    // context.put("group0", getServerGroup0());
    // }
    /**
     * 取得第0组配置文件，应用端默认取得0组配置文件
     *
     * @param publishSnapshotIdIsNotNull
     * @return
     */
    public static void main(String[] arg) throws Exception {
    // RpcCoreManage coreManage = (RpcCoreManage) Proxy.newProxyInstance(
    // CoreDefineScreen.class.getClassLoader(),
    // new Class<?>[] { RpcCoreManage.class },
    // new InvocationHandler() {
    // @Override
    // public Object invoke(Object proxy, Method method,
    // Object[] args) throws Throwable {
    // 
    // Class<?> rt = method.getReturnType();
    // 
    // System.out.println(rt);
    // if(){
    // 
    // }
    // 
    // return 1000;
    // }
    // 
    // });
    // 
    // coreManage.coreConfigChange("aaa");
    // System.out.println();
    }
    // Configuration config = new Configuration();
    // 
    // // ns.default.name=terminator://10.232.21.117:9000
    // 
    // config.set("ns.default.name", "terminator://10.232.21.117:9000");
    // 
    // CoreManagerClient clientProtocol = CoreManagerClient
    // .newInstance(config);
    // System.out.println("================================================");
    // IMocksControl controller = EasyMock.createControl();
    // 
    // CoreManagerClient clientProtocol = controller
    // .create(CoreManagerClient.class);
    // 
    // clientProtocol.getCoreClient();
    // CoreClient coreClient = controller.createMock(CoreClient.class);
    // EasyMock.expectLastCall().andReturn(coreClient);
    // 
    // ClientProtocol target = coreClient.getCenterNode();
    // EasyMock.expectLastCall().andReturn(
    // controller.createMock(ClientProtocol.class)).anyTimes();
    // 
    // target.addCoreNums();
    // EasyMock.expectLastCall().
    // 
    // controller.replay();
    // 
    // System.out.println(clientProtocol);
    // coreClient = clientProtocol.getCoreClient();
    // System.out.println(coreClient);
    // System.out.println(coreClient.getCenterNode());
    // System.out.println(coreClient.getCenterNode());
    // 
    // CoreRequest request = new CoreRequest();
    // 
    // coreClient.getCenterNode().addCoreNums(request, (short) 3);
    // System.out.println();
    // }
    // public
    // this.getClientProtocol().getCoreClient().getCenterNode()
    // public void setClientProtocol(ClientProtocol clientProtocol) {
    // this.clientProtocol = clientProtocol;
    // }
}

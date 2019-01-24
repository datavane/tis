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
package org.apache.solr.core;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisCoreContainer extends CoreContainer {
    // public TisCoreContainer() {
    // super();
    // 
    // }
    // 
    // public TisCoreContainer(NodeConfig config, Properties properties,
    // boolean asyncSolrCoreLoad) {
    // super(config, properties, asyncSolrCoreLoad);
    // }
    // 
    // public TisCoreContainer(NodeConfig config, Properties properties,
    // CoresLocator locator, boolean asyncSolrCoreLoad) {
    // super(config, properties, locator, asyncSolrCoreLoad);
    // 
    // }
    // 
    // public TisCoreContainer(NodeConfig config, Properties properties,
    // CoresLocator locator) {
    // super(config, properties, locator);
    // 
    // }
    // 
    // public TisCoreContainer(NodeConfig config, Properties properties) {
    // super(config, properties);
    // 
    // }
    // 
    // public TisCoreContainer(NodeConfig config) {
    // super(config);
    // 
    // }
    // 
    // public TisCoreContainer(Object testConstructor) {
    // super(testConstructor);
    // 
    // }
    // 
    // public TisCoreContainer(SolrResourceLoader loader) {
    // super(loader);
    // 
    // }
    // 
    // public TisCoreContainer(String solrHome) {
    // super(solrHome);
    // 
    // }
    // 
    // static final Field coreConfigServiceField;
    // static {
    // try {
    // coreConfigServiceField = CoreContainer.class
    // .getDeclaredField("coreConfigService");
    // coreConfigServiceField.setAccessible(true);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // 
    // private ConfigSetService getCoreConfigService() throws Exception {
    // return (ConfigSetService) coreConfigServiceField.get(this);
    // }
    // 
    // /**
    // * 将core注册到zk中
    // *
    // * @param core
    // */
    // public void registerCoreInZk(SolrCore core) {
    // zkSys.registerInZk(core, false);
    // }
    // @Override
    // public SolrCore create(CoreDescriptor dcore, boolean publishState) {
    // if (isShutDown()) {
    // throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE,
    // "Solr has been shutdown.");
    // }
    // 
    // SolrCore core = null;
    // try {
    // MDCLoggingContext.setCore(core);
    // if (zkSys.getZkController() != null) {
    // zkSys.getZkController().preRegister(dcore);
    // }
    // 
    // ConfigSet coreConfig = getCoreConfigService().getConfig(dcore);
    // log.info("Creating SolrCore '{}' using configuration from {}",
    // dcore.getName(), coreConfig.getName());
    // 
    // /* 百岁 修改 让update handler 在solrcore外部初始化 ，这样能够定制 recovery的逻辑 start */
    // // core = new SolrCore(dcore, coreConfig);
    // 
    // // this(cd.getName(), null, coreConfig.getSolrConfig(),
    // // coreConfig.getIndexSchema(), coreConfig.getProperties(),
    // // cd, null, null, null);
    // UpdateHandler updateHandler = SolrCore.createInstance(
    // TisDirectUpdateHandler2.class.getName(),
    // UpdateHandler.class, "Update Handler", null,
    // getResourceLoader());
    // 
    // core = new SolrCore(dcore.getName(), null,
    // coreConfig.getSolrConfig(), coreConfig.getIndexSchema(),
    // coreConfig.getProperties(), dcore, updateHandler, null,
    // null);
    // 
    // /* 2015/10/17 end */
    // solrCores.addCreated(core);
    // 
    // // always kick off recovery if we are in non-Cloud mode
    // if (!isZooKeeperAware()
    // && core.getUpdateHandler().getUpdateLog() != null) {
    // core.getUpdateHandler().getUpdateLog().recoverFromLog();
    // }
    // 
    // registerCore(dcore.getName(), core, publishState);
    // 
    // return core;
    // } catch (Exception e) {
    // coreInitFailures
    // .put(dcore.getName(), new CoreLoadFailure(dcore, e));
    // log.error("Error creating core [{}]: {}", dcore.getName(),
    // e.getMessage(), e);
    // final SolrException solrException = new SolrException(
    // ErrorCode.SERVER_ERROR, "Unable to create core ["
    // + dcore.getName() + "]", e);
    // if (core != null && !core.isClosed())
    // IOUtils.closeQuietly(core);
    // throw solrException;
    // } catch (Throwable t) {
    // SolrException e = new SolrException(ErrorCode.SERVER_ERROR,
    // "JVM Error creating core [" + dcore.getName() + "]: "
    // + t.getMessage(), t);
    // log.error("Error creating core [{}]: {}", dcore.getName(),
    // t.getMessage(), t);
    // coreInitFailures
    // .put(dcore.getName(), new CoreLoadFailure(dcore, e));
    // if (core != null && !core.isClosed())
    // IOUtils.closeQuietly(core);
    // throw t;
    // } finally {
    // MDCLoggingContext.clear();
    // }
    // // return super.create(dcore, publishState);
    // }
    // @Override
    // public void reload(String name) {
    // 
    // SolrCore core = solrCores.getCoreFromAnyList(name, false);
    // if (core == null)
    // throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
    // "No such core: " + name);
    // 
    // CoreDescriptor cd = core.getCoreDescriptor();
    // try {
    // solrCores.waitAddPendingCoreOps(name);
    // ConfigSet coreConfig = coreConfigService.getConfig(cd);
    // log.info("Reloading SolrCore '{}' using configuration from {}",
    // cd.getName(), coreConfig.getName());
    // SolrCore newCore = core.reload(coreConfig);
    // 
    // newCore.getUpdateHandler().getUpdateLog().recoverFromLog();
    // 
    // 
    // registerCore(name, newCore, false);
    // } catch (Exception e) {
    // coreInitFailures.put(cd.getName(), new CoreLoadFailure(cd, e));
    // throw new SolrException(ErrorCode.SERVER_ERROR,
    // "Unable to reload core [" + cd.getName() + "]", e);
    // } finally {
    // solrCores.removeFromPendingOps(name);
    // }
    // }
    // @Override
    // public SolrCore create(CoreDescriptor dcore, boolean publishState) {
    // 
    // SolrCore newCore = super.create(dcore, publishState);
    // 
    // return newCore;
    // }
    // @Override
    // public void load() {
    // super.load();
    // Field coreConfigServiceField = null;
    // try {
    // coreConfigServiceField = CoreContainer.class
    // .getDeclaredField("coreConfigService");
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // 
    // Assert.assertNotNull("coreConfigServiceField can not be null",
    // coreConfigServiceField);
    // 
    // coreConfigServiceField.setAccessible(true);
    // // SolrResourceLoader loader,ZkController zkController
    // TisConfigSetService configSetService = new TisConfigSetService(loader,
    // this.getZkController());
    // 
    // try {
    // coreConfigServiceField.set(this, configSetService);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
}

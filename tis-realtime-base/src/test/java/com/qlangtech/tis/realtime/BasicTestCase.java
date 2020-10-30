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
package com.qlangtech.tis.realtime;

import com.qlangtech.tis.async.message.client.consumer.AsyncMsg;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.realtime.s4employee.TestS4employee;
import com.qlangtech.tis.realtime.s4totalpay.AbstractTestS4totalpayIncr;
import com.qlangtech.tis.realtime.test.util.DefaultRowValueGetter;
import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer;
import com.qlangtech.tis.realtime.transfer.DTO.EventType;
import com.qlangtech.tis.realtime.transfer.IRowValueGetter;
import com.qlangtech.tis.realtime.transfer.UnderlineUtils;
import com.qlangtech.tis.realtime.transfer.ruledriven.BinlogRecord;
import com.qlangtech.tis.wangjubao.jingwei.Table;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class BasicTestCase extends TestCase {

    // 15秒时间
    public static final long Time_Window_15000 = 6000;

    protected static final String DATA_TYPE_PARENT = "p";

    protected static final String DATA_TYPE_CHILD = "c";

    protected static final String KEY_DATA_TYLE = "data_type";

    // protected static final TisCloudSolrClient client = new TisCloudSolrClient(
    // "zk1.2dfire-daily.com:2181,zk2.2dfire-daily.com:2181,zk3.2dfire-daily.com:2181/tis/cloud");
    // protected static final IComDfireTisMenuDAOFacade menuDAOFacade;
    // protected static final IComDfireTisRealtimeDAOFacade orderDAOFacade;
    // protected static final IComDfireTisRealtimeRefundDAOFacade refundDAOFacade;
    // protected static final IItemSkuDAO itemSKUDAO;
    // protected static final ApplicationContext context;
    // protected static final IComDfireTisRealtimeTransferDalDAOFacade waitDAOFacade;
    // protected static final IComDfireTisRealtimeWaitDAOFacade wait2DAOFacade;
    public static final String COLLECTION_search4totalpay = "search4totalpay";

    static {
        HttpUtils.addMockGlobalParametersConfig();
        HttpUtils.addMockApply(1, COLLECTION_search4totalpay + "/0/daily/schema.xml", "schema-xstream.xml", AbstractTestS4totalpayIncr.class);
        //http://192.168.28.200:8080/tjs/download/appconfig/search4test2/0/daily/schema.xml?snapshotid=-1
        HttpUtils.addMockApply(2, "search4test2/0/daily/schema.xml", "schema-xstream.xml", TestS4employee.class);
        // HttpUtils.mockConnMaker = new MockConnectionMaker() {
        // @Override
        // public MockHttpURLConnection create(URL url, List<ConfigFileContext.Header> heads, ConfigFileContext.HTTPMethod method, byte[] content) {
        // //http://127.0.0.1:8080/tjs/config/config.ajax?action=global_parameters_config_action&event_submit_do_get_all=y&runtime=daily
        // if (url.toString().indexOf("global_parameters_config_action") > -1) {
        // 
        // return new MockHttpURLConnection(TestS4totalpayIncr.class.getResourceAsStream("global_params.json"));
        // 
        // }
        // 
        // if (url.toString().indexOf("schema.xml") > -1) {
        // 
        // //                    try () {
        // //                        parseResult = solrFieldsParser.parseSchema(reader, false /* shallValidate */);
        // //                    }
        // 
        // return new MockHttpURLConnection(TestS4totalpayIncr.class.getResourceAsStream("schema-xstream.xml"));
        // 
        // }
        // 
        // return null;
        // }
        // };
        // 本地就是远程仓库，不需要再同步了
        CenterResource.setNotFetchFromCenterRepository();
        System.out.println("mockConnMaker set===================================================================");
        System.setProperty("notdownloadjar", "true");
        // if (!TisIncrLauncher.notDownload) {
        // // System.setProperty("data.dir", "/tmp/tis");
        // Config.setDataDir("/tmp/tis");
        // }
    }

    // public static final String menuId = "999320135eb8b638015ebd85b1c39999";
    public static final String entityId = "99999999";

    public static final String Key_entityId = "entity_id";

    protected static final long _ver_ = 20171011112542l;

    // protected static QueryResponse query(SolrQuery query) {
    // try {
    // return client.query(COLLECTION_search4retailOrder, entityId, query);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // protected ItemSku createItemSKU() {
    // return this.createItemSKU("item_sku_1.txt");
    // }
    // protected ItemSku createItemSKU(String itemSkuFileName) {
    // ItemSku itemSku = deserializeBean(itemSkuFileName, ItemSku.class);
    // Assert.assertNotNull(itemSku.getId());
    // itemSKUDAO.deleteByPrimaryKey(itemSku.getId());
    // itemSKUDAO.insertSelective(itemSku);
    // return itemSku;
    // }
    // protected void deleteSolr(String id) {
    // try {
    // client.deleteById(COLLECTION_search4retailOrder, id, entityId, 1);
    // } catch (Throwable e) {
    // // try {
    // // client.deleteById(COLLECTION_search4retailOrder, id, entityId, 1);
    // // } catch (Throwable e1) {
    // //
    // // // throw new RuntimeException(e);
    // // }
    // 
    // }
    // 
    // }
    // protected void commit() {
    // try {
    // client.commit(COLLECTION_search4retailOrder);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    protected SolrDocument deseriablizeDoc(String cpPath) throws IOException {
        SolrDocument solrDocument = new SolrDocument();
        LineIterator lineIterator = null;
        String line = null;
        String key = null;
        Object val = null;
        try (InputStream reader = this.getClass().getResourceAsStream(cpPath)) {
            lineIterator = IOUtils.lineIterator(reader, TisUTF8.getName());
            while (lineIterator.hasNext()) {
                line = lineIterator.nextLine();
                key = StringUtils.substringBefore(line, ":");
                val = StringUtils.substringAfter(line, ":");
                if (BasicPojoConsumer.VERSION_FIELD_NAME.equals(key)) {
                    val = Long.parseLong((String) val);
                }
                solrDocument.put(key, val);
            }
        }
        if (solrDocument.size() < 1) {
            throw new IllegalStateException("solrDocument.size() can not small than 1");
        }
        return solrDocument;
    }

    public void sleep20s() {
        this.sleepSec((int) (20));
    }

    public void sleepSec(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
        }
    }

    public BasicTestCase() {
        super();
    }

    public BasicTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        // initData();
    }

    // protected void cleanExistPersistenceData() throws Exception {
    // // 先删除================================================
    // MenuCriteria c = createMenuCriteria();
    // //menuDAOFacade.getMenuDAO().deleteByExample(c);
    // deleteSpec();
    // deleteUnitTab();
    // processMake(true/* create */);
    // this.deleteKindMenu();
    // clearTisRecord();
    // Thread.sleep(7000);
    // // 先删除================================================
    // }
    protected void clearTisRecord() {
        // 在TIS中将这条索引干掉
        try {
            // client.deleteById(COLLECTION_MENU, MenuPk.DEFAULT_LANG_PREFIX + "_" + menuId,
            // entityId, 1);
            // client.deleteById(COLLECTION_MENU, "en_US_" + menuId, entityId, 1);
            // client.deleteById("search4menu", "default_999320135eb8b638015ebd85b1c30007",
            // entityId, 1);
            // client.deleteById("search4menu", "en_US_999320135eb8b638015ebd85b1c30007",
            // entityId, 1);
        } catch (Throwable e) {
        }
    }

    // /**
    // * 校验本地菜单的规格
    // *
    // * @param specList
    // * @param m
    // */
    // protected void localValidateChildSpec(List<SpecDetail> specList, TisMenu m) {
    // Assert.assertEquals(4, specList.size());
    // // 规格确认
    // String allChildSpec = m.getAllChildSpec();
    // Assert.assertNotNull(allChildSpec);
    // for (SpecDetail spec : specList) {
    // Assert.assertTrue(
    // "\"" + spec.getSpecDetailId() + "_" + spec.getName() + "\" can not be find in
    // " + allChildSpec,
    // allChildSpec.indexOf(spec.getSpecDetailId() + "_" + spec.getName()) > -1);
    // }
    // }
    // /**
    // * 校验子做法是否正确
    // *
    // * @param specList
    // * @param m
    // */
    // protected void localValidateChildMake(List<Make> makes, TisMenu m) {
    // Assert.assertEquals(3, makes.size());
    // // 规格确认
    // String allChildMakes = m.getAllChildMake();
    // Assert.assertNotNull(allChildMakes);
    // for (Make make : makes) {
    // Assert.assertTrue(make.getName() + " can not be find in " + allChildMakes,
    // allChildMakes.indexOf(make.getMakeId() + "_" + make.getName()) > -1);
    // }
    // }
    // protected Menu createMenu() throws Exception {
    // Menu menu = deserializeBean("db_menu_" + menuId + ".txt", Menu.class);
    // // 创建新的menu
    // menuDAOFacade.getMenuDAO().insertSelective(menu);
    // return menu;
    // }
    // 
    // public TisMenu queryItem(String menuId, String lang, String entityId, int
    // expectVer) {
    // return queryItem(menuId, lang, entityId, expectVer, 0 /* tryCount */);
    // }
    // 
    // protected void createKindMenu() {
    // KindMenu kindMenu = this.deserializeBean("db_kind_menu_1.txt",
    // KindMenu.class);
    // menuDAOFacade.getKindMenuDAO().insertSelective(kindMenu);
    // 
    // kindMenu = this.deserializeBean("db_kind_menu_2.txt", KindMenu.class);
    // menuDAOFacade.getKindMenuDAO().insertSelective(kindMenu);
    // }
    // 
    // protected void deleteKindMenu() {
    // KindMenu kindMenu = this.deserializeBean("db_kind_menu_1.txt",
    // KindMenu.class);
    // menuDAOFacade.getKindMenuDAO().deleteByPrimaryKey(kindMenu.getKindMenuId());
    // kindMenu = this.deserializeBean("db_kind_menu_2.txt", KindMenu.class);
    // menuDAOFacade.getKindMenuDAO().deleteByPrimaryKey(kindMenu.getKindMenuId());
    // }
    // 
    // /**
    // * 构建规格
    // */
    // protected List<SpecDetail> createSpec() throws Exception {
    // List<SpecDetail> specDetailList = Lists.newArrayList();
    // SpecDetail specDetail = null;
    // MenuSpecDetail menuSpecDetail = null;
    // for (int i = 1; i < 5; i++) {
    // specDetail = deserializeBean("spec/spec_detail_" + i + ".txt",
    // SpecDetail.class);
    // menuSpecDetail = deserializeBean("spec/menu_spec_detail_" + i + ".txt",
    // MenuSpecDetail.class);
    // Assert.assertEquals(specDetail.getSpecDetailId(),
    // menuSpecDetail.getSpecDetailId());
    // Assert.assertEquals(menuId, menuSpecDetail.getMenuId());
    // specDetailList.add(specDetail);
    // menuDAOFacade.getSpecDetailDAO().insertSelective(specDetail);
    // menuDAOFacade.getMenuSpecDetailDAO().insertSelective(menuSpecDetail);
    // }
    // return specDetailList;
    // }
    // /**
    // * 构建规格
    // */
    // protected void deleteSpec() throws Exception {
    // 
    // for (int i = 1; i < 5; i++) {
    // menuDAOFacade.getMenuSpecDetailDAO().deleteByPrimaryKey(
    // deserializeBean("spec/menu_spec_detail_" + i + ".txt",
    // MenuSpecDetail.class).getMenuSpecDetailId());
    // menuDAOFacade.getSpecDetailDAO().deleteByPrimaryKey(
    // deserializeBean("spec/spec_detail_" + i + ".txt",
    // SpecDetail.class).getSpecDetailId());
    // }
    // }
    // protected TisMenu queryItem(String menuId, String lang, String entityId, long
    // expectVer_yyyyMMddHHmmss,
    // int tryCount) {
    // 
    // SolrQuery query = new SolrQuery();
    // query.setQuery("menu_id:" + menuId + " AND lang:" + lang);
    // QueryResponse response = null;
    // SolrDocumentList result = null;
    // SolrDocument solrDocument = null;
    // if (tryCount < 5) {
    // try {
    // 
    // response = client.query(COLLECTION_MENU, entityId, query);
    // 
    // result = response.getResults();
    // if (result.getNumFound() < 1) {
    // TimeUnit.SECONDS.sleep(5);
    // return queryItem(menuId, lang, entityId, expectVer_yyyyMMddHHmmss,
    // ++tryCount);
    // }
    // 
    // Assert.assertEquals("result list count", 1, result.getNumFound());
    // 
    // solrDocument = result.get(0);
    // 
    // long ver =
    // Long.parseLong(String.valueOf(solrDocument.getFirstValue("_version_")));
    // if (expectVer_yyyyMMddHHmmss < 0 || ver == expectVer_yyyyMMddHHmmss) {
    // return response.getBeans(TisMenu.class).get(0);
    // } else {
    // TimeUnit.SECONDS.sleep(5);
    // return queryItem(menuId, lang, entityId, expectVer_yyyyMMddHHmmss,
    // ++tryCount);
    // }
    // 
    // // TisCloudSolrClient.SimpleQueryResult<TisMenu> search4menu =
    // // client.query("search4menu", entityId, query,
    // // TisMenu.class);
    // // List<TisMenu> result = search4menu.getResult();
    // // Assert.assertNotNull(result);
    // // Assert.assertEquals(result.size(), 1);
    // 
    // // return result.get(0);
    // } catch (Exception e) {
    // e.printStackTrace();
    // Assert.assertTrue(false);
    // 
    // }
    // } else {
    // // Assert.fail("trycount has exceed max try count,expectVer_yyyyMMddHHmmss:"
    // +
    // // expectVer_yyyyMMddHHmmss);
    // }
    // return null;
    // 
    // }
    // public TisMenu queryItem(String menuId, String lang, String entityId, int
    // expectVer, int tryCount) {
    // long expectVer_yyyyMMddHHmmss = -1;
    // if (expectVer > -1) {
    // expectVer_yyyyMMddHHmmss = formatTimeExpectVer_yyyyMMddHHmmss(expectVer);
    // }
    // 
    // return queryItem(menuId, lang, entityId, expectVer_yyyyMMddHHmmss, tryCount);
    // }
    protected long formatTimeExpectVer_yyyyMMddHHmmss(int expectVer) {
        long expectVer_yyyyMMddHHmmss;
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        expectVer_yyyyMMddHHmmss = Long.parseLong(f.format(new Date(((long) expectVer) * 1000)));
        return expectVer_yyyyMMddHHmmss;
    }

    // protected MenuCriteria createMenuCriteria() {
    // MenuCriteria c = new MenuCriteria();
    // c.createCriteria().andMenuIdEqualTo(menuId).andEntityIdEqualTo(entityId);
    // return c;
    // }
    protected abstract Table getTableRowProcessor(String tabName);

    protected DefaultRowValueGetter deserializeBean(String tableName, Table tableRowProcessor, String path) {
        String line = null;
        try {
            DefaultRowValueGetter i = new DefaultRowValueGetter(tableRowProcessor);
            i.updateProps = new DefaultRowValueGetter.UpdatePropsCollector(tableRowProcessor);
            String colName = null;
            String colValue = null;
            LineIterator it = null;
            try (InputStream input = this.getClass().getResourceAsStream(path)) {
                it = IOUtils.lineIterator(input, "utf8");
                while (it.hasNext()) {
                    line = it.nextLine();
                    colName = StringUtils.trimToEmpty(StringUtils.substringBefore(line, ":"));
                    colValue = StringUtils.trimToEmpty(StringUtils.substringAfter(line, ":"));
                    if (!"null".equalsIgnoreCase(colValue)) {
                        i.put(colName, colValue);
                        // i.put(BasicPojoConsumer.removeUnderline(colName).toString(), colValue);
                        // BeanUtils.copyProperty(i, BasicPojoConsumer.removeUnderline(colName).toString(), colValue);
                    }
                }
            }
            return i;
        } catch (Exception e) {
            throw new RuntimeException("path:" + path + ",line:" + line, e);
        }
    }

    protected <T> AsyncMsg createInsertMQMessage(String tabName, DTO<T> pojo) {
        return createMsg(tabName, com.qlangtech.tis.realtime.transfer.DTO.EventType.ADD, pojo, (r) -> r);
    }

    // protected AsyncMsg createUpdateMQMessage(String tabName, DefaultRowValueGetter pojo, ICallback updateCaller) {
    // return createMsg(tabName, EventType.UPDATE, pojo, updateCaller);
    // }
    protected <T extends IRowValueGetter> AsyncMsg createUpdateMQMessage(String tabName, DTO<T> dto, ICallback updateCaller) {
        return createMsg(tabName, com.qlangtech.tis.realtime.transfer.DTO.EventType.UPDATE, dto, updateCaller);
    }

    private <T> AsyncMsg createMsg(String tabName, EventType event, DTO<T> dto, ICallback updateCaller) {
        return (AsyncMsg) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader()
                , new Class<?>[]{AsyncMsg.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getContent".equals(method.getName())) {
                            BinlogRecord binlog = new BinlogRecord();
                            binlog.setEventType(event.getTypeName());
                            binlog.setOrginTableName(tabName);
                            Map<String, String> before = Collections.emptyMap();
                            if (event == com.qlangtech.tis.realtime.transfer.DTO.EventType.UPDATE) {
                                dto.startCollectUpdateProp();
                                before = serialize2Map(dto.vals);
                            }
                            binlog.setBefore(before);
                            try {
                                binlog.setAfter(serialize2Map(updateCaller.process(dto.vals)));
                                updateCaller.process(dto.junitValsExample);
                            } finally {
                                if (event == com.qlangtech.tis.realtime.transfer.DTO.EventType.UPDATE) {
                                    T update = dto.stopCollectUpdateProp();
                                    dto.pojoCUD.updateByExampleSelective(update, dto.pojo);
                                }
                            }
                            // return JSON.toJSON(binlog);
                            return binlog;
                        }
                        if ("getMsgID".equals(method.getName())) {
                            return String.valueOf(UUID.randomUUID());
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }

                    protected Map<String, String> serialize2Map(DefaultRowValueGetter pojo) throws Exception {
                        return pojo.vals;
                        // final Map<String, String> result = Maps.newHashMap();
                        // String val = null;
                        // BeanInfo beaninfo = Introspector.getBeanInfo(pojo.getClass(), Object.class);
                        //
                        // for (PropertyDescriptor pdesc : beaninfo.getPropertyDescriptors()) {
                        // val = pojo.getColumn(pdesc.getName());
                        // if (StringUtils.isNotBlank(val)) {
                        // result.put(BasicPojoConsumer.addUnderline(pdesc.getName()).toString(), val);
                        // }
                        // }
                        // return result;
                    }
                });
    }

    public static class DTO<T> {

        public final DefaultRowValueGetter vals;

        public final DefaultRowValueGetter junitValsExample;

        public final T pojo;

        private final Class<T> clazz;

        // 流式处理将DTO转化成的抽象，内部已经用columnAliasTransfer 处理过了
        // public DefaultTable tableRecord;
        public final PojoCUD<T> pojoCUD;

        public void startCollectUpdateProp() {
            this.vals.startCollectUpdateProp();
        }

        public T stopCollectUpdateProp() {
            return DTO.pojo(vals.stopCollectUpdateProp(), clazz);
        }

        /**
         * @param valGetter      已经经过aliasTransfer处理过的vals,单元测试过程中需要用这个vals
         * @param rawValueGetter 没有经过aliasTransfer处理过的value
         * @param clazz
         * @param pojo
         * @param pojoCUD
         */
        public DTO(DefaultRowValueGetter valGetter, DefaultRowValueGetter rawValueGetter, Class<T> clazz, T pojo, PojoCUD<T> pojoCUD) {
            this.junitValsExample = valGetter;
            this.vals = rawValueGetter;
            this.pojo = pojo;
            this.pojoCUD = pojoCUD;
            this.clazz = clazz;
        }

        public static <T> T pojo(DefaultRowValueGetter vals, Class<T> clazz) {
            Map.Entry<String, String> lastEntry = null;
            try {
                T result = clazz.newInstance();
                for (Map.Entry<String, String> entry : vals.vals.entrySet()) {
                    lastEntry = entry;
                    BasicPojoConsumer.beanDescLocal.get().setProperty(result, UnderlineUtils.removeUnderline(entry.getKey()).toString(), entry.getValue());
                }
                return result;
            } catch (Exception e) {
                StringBuffer errBuffer = new StringBuffer(clazz.getName());
                if (lastEntry != null) {
                    errBuffer.append(",key:" + lastEntry.getKey() + ",val:" + lastEntry.getValue());
                }
                throw new RuntimeException(errBuffer.toString(), e);
            }
        }
    }

    protected interface ICallback {

        DefaultRowValueGetter process(DefaultRowValueGetter pojo);
    }
    // protected interface IClassAwareCallback {
    // DefaultRowValueGetter process(Class<?> clazz, DefaultRowValueGetter pojo);
    // }
}

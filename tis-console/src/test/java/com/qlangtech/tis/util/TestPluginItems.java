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
package com.qlangtech.tis.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.coredefine.module.action.DataxAction;
import com.qlangtech.tis.datax.ISelectedTab;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;

import java.io.File;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestPluginItems extends TestCase {
  static final String dataXName = "kk";
  static final UploadPluginMeta subFieldPluginMeta = UploadPluginMeta.parse("dataxReader:require,subFormFieldName_selectedTabs,targetDescriptorName_MySQL,dataxName_" + dataXName);

  static {
    CenterResource.setNotFetchFromCenterRepository();
    HttpUtils.addMockGlobalParametersConfig();
  }

  KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(null, dataXName);

  @Override
  public void setUp() throws Exception {
    List<Option> opts = Lists.newArrayList();
    opts.add(new Option("orderdb", "orderdb"));
    OfflineDatasourceAction.existDbs = opts;
    DataxAction.deps = Collections.emptyList();

    File targetFile = readerStore.getTargetFile();
    File parentDir = targetFile.getParentFile();
    FileUtils.forceDelete(parentDir);
  }

  public void testDataXItemsSave() throws Exception {
    validateRootFormSave();
    validateSubFormSave();
    validatePluginValue();
    validateSubField2Json();
  }

  private void validatePluginValue() throws Exception {

    DataxReader reader = readerStore.getPlugin();
    assertNotNull(reader);
    PluginFormProperties rootPropertyTypes = reader.getDescriptor().getPluginFormPropertyTypes();

    assertTrue("get RootFormProperties process result", rootPropertyTypes.accept(new PluginFormProperties.IVisitor() {
      @Override
      public Boolean visit(RootFormProperties props) {
        Map<String, PropertyType> propertiesType = props.propertiesType;
        validatePropertyValue(propertiesType, "dbName", "order3", reader);
        validatePropertyValue(propertiesType, "splitPk", true, reader);

//        validatePropertyValue(propertiesType, "template"
//          , IOUtils.loadResourceFromClasspath(TestPluginItems.class, "datax_reader_mysql_prop_template.json"), reader);

        assertEquals(3, propertiesType.size());

        return true;
      }
    }));
    Optional<IPropertyType.SubFormFilter> subFormFilter = subFieldPluginMeta.getSubFormFilter();
    assertTrue(subFormFilter.isPresent());
    PluginFormProperties pluginFormPropertyTypes = reader.getDescriptor().getPluginFormPropertyTypes(subFormFilter);
    assertTrue("get SuFormProperties process result", pluginFormPropertyTypes.accept(new PluginFormProperties.IVisitor() {
      @Override
      public Boolean visit(SuFormProperties props) {
        assertEquals("selectedTabs", props.getSubFormFieldName());
        Map<String, PropertyType> fieldsType = props.fieldsType;
        assertEquals(3, fieldsType.size());

        List<ISelectedTab> selectedTabs = reader.getSelectedTabs();
        assertNotNull("selectedTabs can not be null", selectedTabs);
        assertEquals(1, selectedTabs.size());
        ISelectedTab selectedTab = selectedTabs.get(0);


        assertEquals("customer_order_relation", selectedTab.getName());
        assertEquals("1=999", selectedTab.getWhere());

        List<ISelectedTab.ColMeta> cols = selectedTab.getCols();
        assertEquals(5, cols.size());
        Set<String> selectedCols = Sets.newHashSet("customerregister_id", "waitingorder_id", "kind", "create_time", "last_ver");
        cols.forEach((c) -> assertTrue(selectedCols.contains(c.getName())));

        return true;
      }
    }));


  }

  private void validateSubField2Json() throws Exception {
    IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);
    // EasyMock.expect(pluginContext.isDataSourceAware()).andReturn(false);
    EasyMock.expect(pluginContext.isCollectionAware()).andReturn(false).anyTimes();
    EasyMock.expect(pluginContext.getRequestHeader(DataxReader.HEAD_KEY_REFERER)).andReturn("/x/" + dataXName + "/config").times(2);


    EasyMock.replay(pluginContext);
    HeteroList<?> heteroList = subFieldPluginMeta.getHeteroList(pluginContext);
    JSONObject pluginDesc = heteroList.toJSON();


    JsonUtil.assertJSONEqual(this.getClass(), "datax_reader_mysql_post_subfield_to_json.json", pluginDesc.toJSONString(), (m, e, a) -> {
      assertEquals(m, e, a);
    });

    EasyMock.verify(pluginContext);
  }

  private void validatePropertyValue(Map<String, PropertyType> propertiesType, String key, Object value, DataxReader reader) {
    PropertyType pt = null;
    pt = propertiesType.get(key);
    assertNotNull(pt);
    assertEquals(value, pt.getVal(reader));
  }

  private void validateSubFormSave() {
    IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);
    EasyMock.expect(pluginContext.isDataSourceAware()).andReturn(false);
    EasyMock.expect(pluginContext.isCollectionAware()).andReturn(false).anyTimes();
    EasyMock.expect(pluginContext.getRequestHeader(DataxReader.HEAD_KEY_REFERER)).andReturn("/x/" + dataXName + "/config").times(2);
    Context context = EasyMock.createMock("context", Context.class);
    //targetDescriptorName_MySQL,subFormFieldName_selectedTabs

    Optional<IPropertyType.SubFormFilter> subFormFilter = subFieldPluginMeta.getSubFormFilter();
    assertTrue("subFormFilter.isPresent():true", subFormFilter.isPresent());
    PluginItems pluginItems = new PluginItems(pluginContext, subFieldPluginMeta);
    IControlMsgHandler fieldErrorHandler = EasyMock.createMock("fieldErrorHandler", IControlMsgHandler.class);

    JSONArray jsonArray = IOUtils.loadResourceFromClasspath(TestPluginItems.class
      , "datax_reader_mysql_post_subfield_form.json", true, (input) -> {
      return JSON.parseArray(org.apache.commons.io.IOUtils.toString(input, TisUTF8.get()));
    });

    JSONArray itemsArray = jsonArray.getJSONArray(0);
    // Optional.empty();
    List<AttrValMap> items = AttrValMap.describableAttrValMapList(fieldErrorHandler, itemsArray, subFormFilter);
    pluginItems.items = items;

    EasyMock.replay(pluginContext, context, fieldErrorHandler);
    pluginItems.save(context);


    EasyMock.verify(pluginContext, context, fieldErrorHandler);
  }

  private void validateRootFormSave() {

    IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);
    EasyMock.expect(pluginContext.isDataSourceAware()).andReturn(false);
    EasyMock.expect(pluginContext.isCollectionAware()).andReturn(false).anyTimes();
    EasyMock.expect(pluginContext.getRequestHeader(DataxReader.HEAD_KEY_REFERER)).andReturn("/x/" + dataXName + "/config").anyTimes();
    Context context = EasyMock.createMock("context", Context.class);
    //targetDescriptorName_MySQL,subFormFieldName_selectedTabs
    UploadPluginMeta pluginMeta = UploadPluginMeta.parse("dataxReader:require,dataxName_" + dataXName);

    PluginItems pluginItems = new PluginItems(pluginContext, pluginMeta);
    IControlMsgHandler fieldErrorHandler = EasyMock.createMock("fieldErrorHandler", IControlMsgHandler.class);

    JSONArray jsonArray = IOUtils.loadResourceFromClasspath(TestPluginItems.class, "datax_reader_mysql_post.json", true, (input) -> {
      return JSON.parseArray(org.apache.commons.io.IOUtils.toString(input, TisUTF8.get()));
    });

    JSONArray itemsArray = jsonArray.getJSONArray(0);
    Optional<IPropertyType.SubFormFilter> subFormFilter = pluginMeta.getSubFormFilter();
    assertFalse("subFormFilter.isPresent():false", subFormFilter.isPresent());
    List<AttrValMap> items = AttrValMap.describableAttrValMapList(fieldErrorHandler, itemsArray, subFormFilter);
    pluginItems.items = items;

    EasyMock.replay(pluginContext, context, fieldErrorHandler);
    pluginItems.save(context);


    EasyMock.verify(pluginContext, context, fieldErrorHandler);
  }
}

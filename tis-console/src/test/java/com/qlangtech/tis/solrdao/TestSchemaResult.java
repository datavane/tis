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
package com.qlangtech.tis.solrdao;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.solr.schema.FieldTypeFactory;
import com.qlangtech.tis.runtime.module.action.SchemaAction;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.solrdao.impl.ParseResult;
import com.qlangtech.tis.util.HeteroEnum;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-27 14:01
 */
public class TestSchemaResult extends TestCase {
  static final String collection = "search4totalpay";

  static {
    CenterResource.setNotFetchFromCenterRepository();
  }

  /**
   * 在schema配置文件中已经添加了 <br/>
   * <fieldType name="test" class="plugin:test" precisionStep="0" positionIncrementGap="0"/>
   * 该类型也要能自动加入到傻瓜模式下，string类型的联动下拉框 <br/>
   *
   * @throws Exception
   */
  public void testParseSchemaResultAlreadyContainFieldPlugin() throws Exception {
    IMessageHandler msgHandler = EasyMock.createMock("msgHandler", IMessageHandler.class);
    Context context = EasyMock.createMock("context", Context.class);

    assertEquals(FieldTypeFactory.class, HeteroEnum.SOLR_FIELD_TYPE.extensionPoint);

    PluginStore<FieldTypeFactory> fieldTypePluginStore = TIS.getPluginStore(collection, FieldTypeFactory.class);
    assertNotNull(fieldTypePluginStore);
    final String testFieldTypeName = "test";

    final List<FieldTypeFactory> plugins = fieldTypePluginStore.getPlugins();
    assertTrue(plugins.size() > 0);

    FieldTypeFactory fieldType = fieldTypePluginStore.find(testFieldTypeName);
    assertNotNull("fieldType can not be null", fieldType);

    try (InputStream schema = this.getClass().getResourceAsStream("s4totalpay-schema-already-contain-fieldtype-plugin.xml")) {
      EasyMock.replay(msgHandler, context);

      SchemaResult schemaResult = SchemaAction.parseSchemaResultWithPluginCfg(collection, msgHandler, context, IOUtils.toByteArray(schema));

      Collection<SolrFieldsParser.SolrType> fieldTypes = ((ParseResult) schemaResult.getParseResult()).getFieldTypes();
      assertEquals("fieldTypes size", 2, fieldTypes.size());

      String content = (com.alibaba.fastjson.JSON.toJSONString(schemaResult.toJSON()
        , SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat));

      try (InputStream assertSchemaResultInput = this.getClass().getResourceAsStream("s4totalpay-schema-already-contain-fieldtype-plugin-schema-result.json")) {
        assertNotNull(assertSchemaResultInput);
        FileUtils.write(new File("test.json"), content, TisUTF8.get());
        assertEquals(StringUtils.trim(IOUtils.toString(assertSchemaResultInput, TisUTF8.get())), content);
      }
    }

    EasyMock.verify(msgHandler, context);
  }

  /**
   * 模拟用户刚添加fieldtype plugin，则用户第一次家在schema中要自动加入 fieldtype
   *
   * @throws Exception
   */
  public void testParseSchemaResult() throws Exception {
    IMessageHandler msgHandler = EasyMock.createMock("msgHandler", IMessageHandler.class);
    Context context = EasyMock.createMock("context", Context.class);

    assertEquals(FieldTypeFactory.class, HeteroEnum.SOLR_FIELD_TYPE.extensionPoint);

    final String collection = "search4totalpay";

    PluginStore<FieldTypeFactory> fieldTypePluginStore = TIS.getPluginStore(collection, FieldTypeFactory.class);
    assertNotNull(fieldTypePluginStore);
    final String testFieldTypeName = "test";

    final List<FieldTypeFactory> plugins = fieldTypePluginStore.getPlugins();
    assertTrue(plugins.size() > 0);

    FieldTypeFactory fieldType = fieldTypePluginStore.find(testFieldTypeName);
    assertNotNull("fieldType can not be null", fieldType);

    try (InputStream schema = this.getClass().getResourceAsStream("s4totalpay-schema.xml")) {
      EasyMock.replay(msgHandler, context);

      SchemaResult schemaResult = SchemaAction.parseSchemaResultWithPluginCfg(collection, msgHandler, context, IOUtils.toByteArray(schema));

      Collection<SolrFieldsParser.SolrType> fieldTypes = ((ParseResult) schemaResult.getParseResult()).getFieldTypes();
      assertEquals("fieldTypes size", 16, fieldTypes.size());

      String content = (com.alibaba.fastjson.JSON.toJSONString(schemaResult.toJSON()
        , SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat));

      try (InputStream assertSchemaResultInput = this.getClass().getResourceAsStream("assertSchemaResult.json")) {
        assertNotNull(assertSchemaResultInput);
        assertEquals(IOUtils.toString(assertSchemaResultInput, TisUTF8.get()), content);
      }
    }

    EasyMock.verify(msgHandler, context);

  }
}

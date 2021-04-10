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
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.manage.impl.DataXAppSource;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.util.DescriptorsJSON;

import java.util.List;
import java.util.Objects;

/**
 * manage datax pipe process logic
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-08 15:04
 */
public class DataxAction extends BasicModule {

  public void doGetSupportedReaderWriterTypes(Context context) {

    DescriptorExtensionList<DataxReader, Descriptor<DataxReader>> readerTypes = TIS.get().getDescriptorList(DataxReader.class);
    DescriptorExtensionList<DataxWriter, Descriptor<DataxWriter>> writerTypes = TIS.get().getDescriptorList(DataxWriter.class);

    this.setBizResult(context, new DataxPluginDescMeta(readerTypes, writerTypes));
  }

  /**
   * submit reader type and writer type form for validate
   *
   * @param context
   */
  public void doValidateReaderWriter(Context context) {

  }

  /**
   * get cols meta
   *
   * @param context
   */
  public void doGetReaderTableSelectableCols(Context context) {
    String dataxName = this.getString(DataXAppSource.DATAX_NAME);
    String tableName = this.getString("tableName");
    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(dataxName);
    DataxReader reader = readerStore.getPlugin();
    Objects.requireNonNull(reader, "reader can not be null");
    List<ColumnMetaData> tableMeta = reader.getTableMetadata(tableName);
    this.setBizResult(context, tableMeta);
  }


  public static class DataxPluginDescMeta extends PluginDescMeta<DataxReader> {
    private final DescriptorsJSON writerTypesDesc;

    public DataxPluginDescMeta(DescriptorExtensionList<DataxReader, Descriptor<DataxReader>> readerTypes
      , DescriptorExtensionList<DataxWriter, Descriptor<DataxWriter>> writerTypes) {
      super(readerTypes);
      this.writerTypesDesc = new DescriptorsJSON(writerTypes);
    }

    @JSONField(serialize = false)
    public com.alibaba.fastjson.JSONObject getPluginDesc() {
      throw new UnsupportedOperationException();
    }

    public com.alibaba.fastjson.JSONObject getReaderDesc() {
      return pluginDesc.getDescriptorsJSON();
    }


    public com.alibaba.fastjson.JSONObject getWriterDesc() {
      return writerTypesDesc.getDescriptorsJSON();
    }
  }

}

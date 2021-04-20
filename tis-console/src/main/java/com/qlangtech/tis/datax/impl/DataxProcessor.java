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
package com.qlangtech.tis.datax.impl;

import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.*;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 16:46
 */
public abstract class DataxProcessor implements IdentityName, IDataxProcessor, IAppSource {


  private transient static final VelocityEngine velocityEngine;

  static {
    try {
      velocityEngine = new VelocityEngine();
      Properties prop = new Properties();
      prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
      velocityEngine.init(prop);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<TableAlias> tableMaps;

  @Override
  public Map<String, TableAlias> getTabAlias() {
    if (tableMaps == null) {
      return Collections.emptyMap();
    }
    return this.tableMaps.stream().collect(Collectors.toMap((m) -> m.getFrom(), (m) -> m));
  }

  public void setTableMaps(List<TableAlias> tableMaps) {
    this.tableMaps = tableMaps;
  }

  private VelocityContext createContext(IDataxContext reader, IDataxContext writer) {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("reader", reader);
    velocityContext.put("writer", writer);
    velocityContext.put("cfg", this);
    return velocityContext;
  }

  public void startGenerateCfg(File parentDir) throws Exception {
    IDataxReader reader = this.getReader();
    Iterator<IDataxReaderContext> subTasks = reader.getSubTasks();
    IDataxReaderContext readerContext = null;
    File configFile = null;
    while (subTasks.hasNext()) {
      readerContext = subTasks.next();
      configFile = new File(parentDir, readerContext.getName() + ".json");
      FileUtils.write(configFile, generateDataxConfig(readerContext, Optional.empty()), TisUTF8.get(), false);
    }

  }

  protected String generateDataxConfig(IDataxReaderContext readerContext, Optional<TableMap> tableMap) throws IOException {

    IDataxWriter writer = this.getWriter();
    final String tpl = this.getTemplateContent();
    VelocityContext mergeData = createContext(readerContext, writer.getSubTask(tableMap));


    StringWriter writerContent = new StringWriter();
    velocityEngine.evaluate(mergeData, writerContent, "tablex-writer.vm", tpl);

    return writerContent.toString();
  }


//     "setting": {
//        "speed": {
//            "channel": 3
//        },
//        "errorLimit": {
//            "record": 0,
//                    "percentage": 0.02
//        }
//    },

  protected abstract int getChannel();

  protected abstract int getErrorLimitCount();

  protected abstract int getErrorLimitPercentage();

  public final Map<String, Object> getSetting() {
    Map<String, Object> setting = Maps.newHashMap();
    Map<String, Object> speed = Maps.newHashMap();
    speed.put("channel", this.getChannel());
    Map<String, Object> errorLimit = Maps.newHashMap();
    errorLimit.put("record", this.getErrorLimitCount());
    errorLimit.put("percentage", this.getErrorLimitPercentage());
    setting.put("speed", speed);
    setting.put("errorLimit", errorLimit);
    return setting;
  }

  public String getTemplateContent() {
    final String tpl = getTemplate();
    String template = StringUtils.replace(tpl, "<!--reader-->", this.getReader().getTemplate());
    template = StringUtils.replace(template, "<!--writer-->", this.getWriter().getTemplate());
    return template;
  }

  /**
   * 却默认的模版
   *
   * @return
   */
  public static String getTemplate() {
    return com.qlangtech.tis.extension.impl.IOUtils.loadResourceFromClasspath(
      DataxProcessor.class, "datax-tpl.vm");
  }


  @Override
  public final Descriptor<IAppSource> getDescriptor() {
    return TIS.get().getDescriptor(this.getClass());
  }

//    private static class MergeData {
//        private IDataxContext reader;
//        private IDataxContext writer;
//
//        public IDataxContext getReader() {
//            return reader;
//        }
//
//        public void setReader(IDataxContext reader) {
//            this.reader = reader;
//        }
//
//        public IDataxContext getWriter() {
//            return writer;
//        }
//
//        public void setWriter(IDataxContext writer) {
//            this.writer = writer;
//        }
//    }
}

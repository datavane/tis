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
import com.qlangtech.tis.datax.IDataxContext;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 16:46
 */
public abstract class DataxProcessor implements Describable<DataxProcessor>, IdentityName, IDataxProcessor {


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

    private VelocityContext createContext(IDataxContext reader, IDataxContext writer) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("reader", reader);
        velocityContext.put("writer", writer);
        velocityContext.put("cfg", this);
        return velocityContext;
    }

    public void start() throws Exception {
        IDataxReader reader = this.getReader();
        Iterator<IDataxContext> subTasks = reader.getSubTasks();

        while (subTasks.hasNext()) {
            generateDataxConfig(subTasks.next(), Optional.empty());
        }

    }

    protected String generateDataxConfig(IDataxContext readerContext, Optional<TableMap> tableMap) throws IOException {

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
    public final Descriptor<DataxProcessor> getDescriptor() {
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

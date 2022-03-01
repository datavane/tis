/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.incr;

import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.datax.IDataXPluginMeta;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-29 10:50
 **/
@Public
public abstract class TISSinkFactory implements Describable<TISSinkFactory>, KeyedPluginStore.IPluginKeyAware {
    private static final Logger logger = LoggerFactory.getLogger(TISSinkFactory.class);
    @TISExtension
    public static final HeteroEnum<TISSinkFactory> sinkFactory = new HeteroEnum<TISSinkFactory>(//
            TISSinkFactory.class, //
            "sinkFactory", //
            "Incr Sink Factory", //
            Selectable.Single, true);


    public static TISSinkFactory getIncrSinKFactory(String dataXName) {
        IPluginContext pluginContext = IPluginContext.namedContext(dataXName);
        List<TISSinkFactory> sinkFactories = sinkFactory.getPlugins(pluginContext, null);
        TISSinkFactory sinkFactory = null;
        logger.info("sinkFactories size:" + sinkFactories.size());
        for (TISSinkFactory factory : sinkFactories) {
            sinkFactory = factory;
            break;
        }
        Objects.requireNonNull(sinkFactory, "sinkFactories.size():" + sinkFactories.size());
        return sinkFactory;
    }

    protected transient String dataXName;

    @Override
    public void setKey(KeyedPluginStore.Key key) {
        this.dataXName = key.keyVal.getVal();
    }

    /**
     * Map< IDataxProcessor.TableAlias, <SinkFunction<DTO> >
     *
     * @param dataxProcessor
     * @return
     */
    public abstract <SinkFunc> Map<IDataxProcessor.TableAlias, SinkFunc> createSinkFunction(IDataxProcessor dataxProcessor);

    @Override
    public final Descriptor<TISSinkFactory> getDescriptor() {
        Descriptor<TISSinkFactory> descriptor = TIS.get().getDescriptor(this.getClass());
        Class<BaseSinkFunctionDescriptor> expectClazz = getExpectDescClass();
        if (!(expectClazz.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of " + expectClazz.getName());
        }
        return descriptor;
    }

    protected <TT extends BaseSinkFunctionDescriptor> Class<TT> getExpectDescClass() {
        return (Class<TT>) BaseSinkFunctionDescriptor.class;
    }


    public static abstract class BaseSinkFunctionDescriptor extends Descriptor<TISSinkFactory> {
        @Override
        public Map<String, Object> getExtractProps() {
            Map<String, Object> vals = Maps.newHashMap();
            IDataXPluginMeta.EndType targetType = this.getTargetType();
            vals.put(IDataXPluginMeta.END_TARGET_TYPE, targetType.getVal());
            return vals;
        }

        protected abstract IDataXPluginMeta.EndType getTargetType();
    }
}

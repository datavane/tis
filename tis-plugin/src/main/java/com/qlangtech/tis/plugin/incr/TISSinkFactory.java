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

package com.qlangtech.tis.plugin.incr;

import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataXPluginMeta;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.Selectable;

import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-29 10:50
 **/
public abstract class TISSinkFactory implements Describable<TISSinkFactory> {

    @TISExtension
    public static final HeteroEnum<TISSinkFactory> sinkFactory = new HeteroEnum<TISSinkFactory>(//
            TISSinkFactory.class, //
            "sinkFactory", //
            "Incr Sink Factory", //
            Selectable.Single);

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

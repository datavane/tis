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

package com.qlangtech.tis.extension.util;

import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.Option;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-05-16 09:48
 **/
public abstract class AbstractPropAssist<T extends Describable, FIELD> {

    private Descriptor<T> descriptor;
    private Map<String, /*** fieldname*/IPropertyType> props;

    protected AbstractPropAssist(Descriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

//    protected void addFieldDescriptor(String fieldName, FIELD configOption) {
//        addFieldDescriptor(fieldName, configOption, new OverwriteProps());
//    }

    private Map<String, IPropertyType> getProps() {
        if (props == null) {
            this.props = descriptor.getPropertyTypes(false);
        }
        return props;
    }

    protected Options createOptions() {
        return new Options(this);
    }

    public static class Options<T extends Describable, FIELD> {
        private final List<PropAssistFieldTriple<FIELD>> opts = Lists.newArrayList();


        private final AbstractPropAssist<T, FIELD> propsAssist;

        public Options(AbstractPropAssist<T, FIELD> propsAssist) {
            this.propsAssist = propsAssist;
        }

        public void add(String fieldName, FIELD configOption) {
            // propsAssist.addFieldDescriptor(fieldName, configOption, new OverwriteProps());
            this.add(fieldName, TISAssistProp.create(configOption));
        }

        public void add(String fieldName, FIELD configOption, OverwriteProps overwriteProps) {
            TISAssistProp<FIELD> assistProp = TISAssistProp.create(configOption);
            this.add(fieldName, assistProp.setOverwriteProp(overwriteProps), null);
        }

        private void addFieldDescriptor(String fieldName, FIELD configOption, OverwriteProps overwriteProps) {
            propsAssist.addFieldDescriptor(fieldName, configOption, overwriteProps);
        }

        public void add(String fieldName, TISAssistProp<FIELD> option, PropValFilter propValFilter) {
            if (StringUtils.isNotEmpty(fieldName)) {
                this.addFieldDescriptor(fieldName, option.configOption, option.overwriteProp);
            }
            this.opts.add(PropAssistFieldTriple.of(fieldName, option.configOption, propValFilter));
        }

        public void add(String fieldName, TISAssistProp option) {
            this.add(fieldName, option, null);
        }

        public void add(FIELD option, PropValFilter propValFilter) {
            this.add(null, TISAssistProp.create(option), propValFilter);
        }


        public void setTarget(BiConsumer<FIELD, Object> targetPropSetter, T instance) {
            PropertyType property = null;
            Function<Object, Object> propValFilter = null;
            Object val = null;
            for (PropAssistFieldTriple<FIELD> opt : opts) {
                propValFilter = opt.getPropValFilter();

                if (StringUtils.isEmpty(opt.getFieldName())) {
                    throw new IllegalStateException("fieldKey can not be empty");
                }
                property = Objects.requireNonNull((PropertyType) getProps().get(opt.getFieldName())
                        , "key:" + opt.getFieldName() + " relevant props can not be null");
                val = property.getVal(false, instance);

                if (val == null) {
                    continue;
                }
                if (propValFilter != null) {
                    val = propValFilter.apply(val);
                }
                targetPropSetter.accept(opt.getField(), val);
            }
        }


        public Map<String, IPropertyType> getProps() {
            return propsAssist.getProps();
        }
    }

    public static class TISAssistProp<FIELD> {
        private OverwriteProps overwriteProp = new OverwriteProps();
        private final FIELD configOption;

        boolean hasSetOverWrite = false;

        public static <FIELD> TISAssistProp<FIELD> create(FIELD configOption) {
            return new TISAssistProp(configOption);
        }

        private TISAssistProp(FIELD configOption) {
            this.configOption = configOption;
        }

        public TISAssistProp overwriteDft(Object dftVal) {
            return this.setOverwriteProp(OverwriteProps.dft(dftVal));
        }

        public TISAssistProp overwritePlaceholder(Object placeholder) {
            return this.setOverwriteProp(OverwriteProps.placeholder(placeholder));
        }

        public TISAssistProp overwriteLabel(String label) {
            return this.setOverwriteProp(OverwriteProps.label(label));
        }

        public TISAssistProp overwriteLabel(Function<String, String> labelRewrite) {
            return this.setOverwriteProp(OverwriteProps.label(labelRewrite));
        }

        public TISAssistProp setOverwriteProp(OverwriteProps overwriteProp) {
            if (hasSetOverWrite) {
                throw new IllegalStateException("overwriteProp has been setted ,can not be writen twice");
            }
            this.overwriteProp = overwriteProp;
            this.hasSetOverWrite = true;
            return this;
        }
    }

    protected abstract String getDescription(FIELD configOption);

    protected abstract Object getDefaultValue(FIELD configOption);

    protected abstract List<Option> getOptEnums(FIELD configOption);

    /**
     * Get the display name of the field.
     *
     * @param configOption
     * @return
     */
    protected abstract String getDisplayName(FIELD configOption);

    protected final void addFieldDescriptor(String fieldName, FIELD configOption, OverwriteProps overwriteProps) {
        String desc = getDescription(configOption);//.description();

        Object dftVal = overwriteProps.processDftVal(getDefaultValue(configOption));

        PropertyType propertyType = Objects.requireNonNull((PropertyType) getProps().get(fieldName)
                , "fieldName:" + fieldName + " relevant propertyType can not be null");
        dftVal = propertyType.serialize2FrontendOutput(dftVal);

        StringBuffer helperContent = new StringBuffer(desc);
        if (overwriteProps.appendHelper.isPresent()) {
            helperContent.append("\n\n").append(overwriteProps.appendHelper.get());
        }

        final List<Option> opts = getOptEnums(configOption);

        descriptor.addFieldDescriptor(fieldName, dftVal
                , overwriteProps.labelRewrite.apply(getDisplayName(configOption)), helperContent.toString()
                , overwriteProps.opts.isPresent() ? overwriteProps.opts : Optional.ofNullable(opts));
    }
}
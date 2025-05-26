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
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.manage.common.Option;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-05-16 09:48
 **/
public abstract class AbstractPropAssist<T extends Describable, FIELD> {

    private Descriptor<T> descriptor;

    protected AbstractPropAssist(Descriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

    protected void addFieldDescriptor(String fieldName, FIELD configOption) {
        addFieldDescriptor(fieldName, configOption, new OverwriteProps());
    }

    protected Options createFlinkOptions() {
        return new Options(this);
    }

    public static class Options<T extends Describable, FIELD> {
        private final List<PropAssistFieldTriple<T, FIELD>> opts = Lists.newArrayList();
        private Map<String, /*** fieldname*/PropertyType> props;

        private AbstractPropAssist<T, FIELD> propsAssist;

        public Options(AbstractPropAssist<T, FIELD> propsAssist) {
            this.propsAssist = propsAssist;
        }

        public void addFieldDescriptor(String fieldName, FIELD configOption) {
            propsAssist.addFieldDescriptor(fieldName, configOption);
        }

        public void addFieldDescriptor(String fieldName, FIELD configOption, OverwriteProps overwriteProps) {
            propsAssist.addFieldDescriptor(fieldName, configOption, overwriteProps);
        }

        public void setTarget(BiConsumer<FIELD, Object> targetPropSetter, T instance) {
            //  org.apache.flink.configuration.Configuration cfg = new org.apache.flink.configuration.Configuration();
            PropertyType property = null;
            Function<T, Object> propGetter = null;
            Object val = null;
            for (PropAssistFieldTriple<T, FIELD> opt : opts) {
                propGetter = opt.getPropGetter();
                if (propGetter != null) {
                    val = propGetter.apply(instance);
                } else {
                    if (StringUtils.isEmpty(opt.getFieldName())) {
                        throw new IllegalStateException("fieldKey can not be empty");
                    }
                    property = Objects.requireNonNull(getProps().get(opt.getFieldName())
                            , "key:" + opt.getFieldName() + " relevant props can not be null");
                    val = property.getVal(instance);
                }
                if (val == null) {
                    continue;
                }
                targetPropSetter.accept(opt.getField(), val);
            }
        }

        public void add(String fieldName, TISAssistProp option) {
            this.add(fieldName, option, null);
        }

        public void add(FIELD option, Function<T, Object> propGetter) {
            this.add(null, TISAssistProp.create(option), propGetter);
        }

        public void add(String fieldName, TISAssistProp<FIELD> option, Function<T, Object> propGetter) {
            if (StringUtils.isNotEmpty(fieldName)) {
                this.addFieldDescriptor(fieldName, option.configOption, option.overwriteProp);

            }
            this.opts.add(PropAssistFieldTriple.of(fieldName, option.configOption, propGetter));
        }

        public Map<String, PropertyType> getProps() {
            if (props == null) {
                this.props = propsAssist.descriptor.getPluginFormPropertyTypes().accept(new PluginFormProperties.IVisitor() {
                    @Override
                    public Map<String, PropertyType> visit(RootFormProperties props) {
                        return props.propertiesType;
                    }
                });
            }
            return props;
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
            // overwriteProp.setDftVal(dftVal);
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

        StringBuffer helperContent = new StringBuffer(desc);
        if (overwriteProps.appendHelper.isPresent()) {
            helperContent.append("\n\n").append(overwriteProps.appendHelper.get());
        }

        // Type targetClazz = configOption.type();
        final List<Option> opts = getOptEnums(configOption);
//        switch (targetClazz) {
//            case LIST: {
//                throw new IllegalStateException("unsupported type:" + targetClazz);
//            }
//            case BOOLEAN: {
//                opts = Lists.newArrayList(new Option("是", true), new Option("否", false));
//                break;
//            }
//            case CLASS:
//            case PASSWORD:
//            case INT:
//            case DOUBLE:
//            case LONG:
//            case SHORT:
//            case STRING:
//            default:
//                // throw new IllegalStateException("unsupported type:" + targetClazz);
//        }

//        if (configOption.recommender() instanceof EnumRecommender) {
//            EnumRecommender enums = (EnumRecommender) configOption.recommender();
//            List vals = enums.validValues(null, null);
//            opts = (List<Option>) vals.stream().map((e) -> new Option(String.valueOf(e))).collect(Collectors.toList());
//        }


//        if (targetClazz == Duration.class) {
//            if (dftVal != null) {
//                dftVal = ((Duration) dftVal).getSeconds();
//            }
//            helperContent.append("\n\n 单位：`秒`");
//        } else if (targetClazz == MemorySize.class) {
//            if (dftVal != null) {
//                dftVal = ((MemorySize) dftVal).getKibiBytes();
//            }
//            helperContent.append("\n\n 单位：`kb`");
//        } else if (targetClazz.isEnum()) {
//            List<Enum> enums = EnumUtils.getEnumList((Class<Enum>) targetClazz);
//            opts = enums.stream().map((e) -> new Option(e.name())).collect(Collectors.toList());
//        } else if (targetClazz == Boolean.class) {
//            opts = Lists.newArrayList(new Option("是", true), new Option("否", false));
//        }

        descriptor.addFieldDescriptor(fieldName, dftVal
                , overwriteProps.labelRewrite.apply(getDisplayName(configOption)), helperContent.toString()
                , overwriteProps.opts.isPresent() ? overwriteProps.opts : Optional.ofNullable(opts));
    }


//    public class FieldTriple<T extends Describable,FIELD> {
//        public static <T extends Describable,FIELD> FieldTriple<T,FIELD> of(String fieldName, FIELD field, Function<T, Object> propGetter) {
//            return new FieldTriple<>(fieldName, field, propGetter);
//        }
//        private final String fieldName;
//        private final FIELD field;
//        private final Function<T, Object> propGetter;
//
//        private FieldTriple(String fieldName, FIELD field, Function<T, Object> propGetter) {
//            this.fieldName = fieldName;
//            this.field = field;
//            this.propGetter = propGetter;
//        }
//
//        public String getFieldName() {
//            return fieldName;
//        }
//
//        public FIELD getField() {
//            return field;
//        }
//
//        public Function<T, Object> getPropGetter() {
//            return propGetter;
//        }
//    }
}
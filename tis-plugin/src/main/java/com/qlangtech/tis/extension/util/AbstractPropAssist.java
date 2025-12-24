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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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
                property = Objects.requireNonNull((PropertyType) getProps().get(opt.getFieldName()),
                        "key:" + opt.getFieldName() + " relevant props can not be null");
                val = property.getVal(false, instance);

                if (val == null) {
                    if (property.isInputRequired()) {
                        throw new IllegalStateException("property:" + property.displayName + " is required but now " + "is" + " null");
                    }
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
        OverwriteProps overwriteProp = new OverwriteProps();
        private final FIELD configOption;

        boolean hasSetOverWrite = false;

        public static <FIELD> TISAssistProp<FIELD> create(FIELD configOption) {
            return new TISAssistProp(configOption);
        }

        public FIELD getConfigOption() {
            return configOption;
        }

        /**
         * 重新定义enumOpts和默认值
         *
         * @param assistProp
         * @param enumOpts
         * @param dftVal
         */
        public static void set(TISAssistProp assistProp, List<Option> enumOpts, Object dftVal) {
            if (!assistProp.hasSetOverWrite) {
                OverwriteProps overwriteProp = new OverwriteProps();
                overwriteProp.setEnumOpts(enumOpts);
                overwriteProp.setDftVal(dftVal);
                assistProp.setOverwriteProp(overwriteProp);
            } else {
                assistProp.overwriteProp.setEnumOpts(enumOpts);
                assistProp.overwriteProp.setDftVal(dftVal);
            }
        }

        private TISAssistProp(FIELD configOption) {
            this.configOption = configOption;
        }

        public TISAssistProp overwriteDft(Object dftVal) {
            return this.setOverwriteProp(OverwriteProps.dft(dftVal));
        }

        public TISAssistProp overwriteBooleanEnums() {
            OverwriteProps booleanEnums = OverwriteProps.createBooleanEnums();
            overwriteProp.setEnumOpts(booleanEnums.opts.get());
            overwriteProp.dftValConvert = booleanEnums.dftValConvert;
            return this;
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

    protected abstract MarkdownHelperContent getDescription(FIELD configOption);

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
        MarkdownHelperContent desc = getDescription(configOption);//.description();

        Object dftVal = overwriteProps.processDftVal(getDefaultValue(configOption));

        PropertyType propertyType = Objects.requireNonNull((PropertyType) getProps().get(fieldName),
                "fieldName:" + fieldName + " relevant propertyType can not be null");
        dftVal = propertyType.serialize2FrontendOutput(dftVal);

        //        StringBuffer helperContent = new StringBuffer(desc);
        //        if (overwriteProps.appendHelper.isPresent()) {
        //            helperContent.append("\n").append(overwriteProps.appendHelper.get());
        //        }
        //
        //        ;

        final List<Option> opts = getOptEnums(configOption);

        descriptor.addFieldDescriptor(fieldName, dftVal,
                overwriteProps.labelRewrite.apply(getDisplayName(configOption)), desc.append(overwriteProps),
                overwriteProps.opts.isPresent() ? overwriteProps.opts : Optional.ofNullable(opts));
    }

    public static class MarkdownHelperContent {
        private final StringBuffer content;
        int lastLineEmptyCount = 0;
        boolean hasAddLine = false;
        private final PluginExtraProps.AsynPropHelp asynProp;

        public MarkdownHelperContent(PluginExtraProps.AsynPropHelp content) {
            this.content = new StringBuffer();
            this.appendContent(Objects.requireNonNull(content, "content can not be null").getDetailed());
            this.asynProp = content;
        }

        public MarkdownHelperContent(MarkdownHelperContent content) {
            this(new PluginExtraProps.AsynPropHelp(content.getContent()));
        }

        /**
         *
         * @param content
         */
        private MarkdownHelperContent appendContent(String content) {

            try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (StringUtils.isEmpty(line)) {
                        lastLineEmptyCount++;
                    } else {
                        lastLineEmptyCount = 0;
                    }
                    if (lastLineEmptyCount > 1) {
                        continue;
                    }
                    if (hasAddLine) {
                        this.content.append("\n");
                    }
                    this.content.append(line);
                    hasAddLine = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }


        public MarkdownHelperContent append(OverwriteProps overwriteProps) {
            if (overwriteProps.appendHelper.isPresent()) {
                //  content.append("\n").append(overwriteProps.appendHelper.get());
                this.appendContent("\n\n" + overwriteProps.appendHelper.get());
            }
            return this;
        }

        /**
         * 确保每次添加内容，上下content 之间都添加一个空行（因为是markdown文本内容，不然不会换行）
         *
         * @param content
         * @return
         */
        public MarkdownHelperContent append(MarkdownHelperContent content) {
            if (content.isNotEmpty()) {
                // this.content.append("\n").append(content.content);
                this.appendContent("\n\n" + content.content.toString());
            }
            return this;
        }

        public StringBuffer getContent() {
            return this.content;
        }


        public StringBuffer getContentForAI() {
            StringBuffer shortContent = null;
            if ((shortContent = this.asynProp.getAbstracted()) != null) {
                return shortContent;
            }
            return getContent();
        }

        public boolean isNotEmpty() {
            return StringUtils.isNotEmpty(this.content.toString());
        }
    }
}
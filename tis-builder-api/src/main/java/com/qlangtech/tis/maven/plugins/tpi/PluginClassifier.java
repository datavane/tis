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

package com.qlangtech.tis.maven.plugins.tpi;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-06-20 17:16
 **/
public class PluginClassifier {
    public static final String PACAKGE_TPI_EXTENSION_NAME = "tpi";
    private static final String DIMENSION_SPLIT = ";";
    private final String classifier;

    private Map<String, String> dimension;

    public static final NoneClassifier NONE_CLASSIFIER = new NoneClassifier();

    public static boolean isNoneClassifier(PluginClassifier classifier) {
        return classifier instanceof NoneClassifier;
    }

    private static final class NoneClassifier extends PluginClassifier {
        public NoneClassifier() {
            super(StringUtils.EMPTY);
        }
    }

    public static final Comparator<PluginClassifier> DESCENDING = new Comparator<PluginClassifier>() {
        public int compare(PluginClassifier o1, PluginClassifier o2) {
            return o1.classifier.compareTo(o2.classifier);
        }
    };

    public PluginClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getTPIPluginName(String tpiName, String extension) {
        return tpiName + "_" + StringUtils.replace(classifier, DIMENSION_SPLIT, "_") + extension;
    }


    public static void validate(String requiredFrom, String dependencyForPlugin, Optional<PluginClassifier> dptCandidate, Optional<PluginClassifier> ba) {

//        Optional<PluginClassifier> dptCandidate = dptCandidate.getClassifier();
//        Optional<PluginClassifier> ba = b.getClassifier();

        if (dptCandidate.isPresent() ^ ba.isPresent()) {
            if (dptCandidate.isPresent()) {
                throw new IllegalStateException("\nrequired from " + requiredFrom + " for " + dependencyForPlugin
                        + " of Candidate classifier[" + dptCandidate.get().getClassifier() + "] present status:"
                        + dptCandidate.isPresent() + " must same with Owner classifier prestent status:" + ba.isPresent());
            }
        }
    }


    public String getClassifier() {
        return this.classifier;
    }

    private Map<String, String> getDimension() {

        if (dimension == null) {
            dimension = new HashMap<>();
            for (String dim : StringUtils.split(this.classifier, DIMENSION_SPLIT)) {
                String[] tuple = StringUtils.split(dim, "_");
                if (tuple.length != 2) {
                    throw new IllegalStateException("the format of classifier is illegal:" + this.classifier);
                }
                dimension.put(tuple[0], tuple[1]);
            }
            if (dimension.isEmpty()) {
                throw new IllegalStateException("dimension can not be empty,classifier:" + this.classifier);
            }
            dimension = Collections.unmodifiableMap(dimension);
        }
        return dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginClassifier that = (PluginClassifier) o;
        return StringUtils.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return classifier.hashCode();
    }

    /**
     * @param candidateClassifier
     * @return
     */
    public boolean match(String requiredFrom, PluginClassifier candidateClassifier) {
        Map<String, String> dimension = this.getDimension();

        for (Map.Entry<String, String> candidateDim : candidateClassifier.getDimension().entrySet()) {
            String dimVal = dimension.get(candidateDim.getKey());
            if (StringUtils.isEmpty(dimVal)) {
                throw new IllegalStateException("requiredFrom:" + requiredFrom + " candidate dim:" + candidateDim.getKey()
                        + "[" + candidateClassifier.classifier + "] is not contain in [" + this.classifier + "]");
            }
            if (!StringUtils.equals(dimVal, candidateDim.getValue())) {
                return false;
            }
        }
        return true;
    }
}

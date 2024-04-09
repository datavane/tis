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

package com.qlangtech.tis.datax.job;

import com.qlangtech.tis.plugin.k8s.K8sImage;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-03-11 13:33
 **/
public class ServiceResName<T> extends SubJobResName<T> {
    //    private static final String HOST_SUFFIX = "_SERVICE_HOST";
    private static final String PORT_SUFFIX = "_SERVICE_PORT";

    public ServiceResName(String name, SubJobExec<T> subJobExec) {
        super(name, subJobExec);
    }

    //    public String getHostEvnName() {
//        return replaceAndUpperCase(getName()) + HOST_SUFFIX;
//    }
//
    public String getPortEvnName() {
        return replaceAndUpperCase(getName()) + PORT_SUFFIX;
    }

    //
    public String getHostPortReplacement(K8sImage image) {
        return getName() + "." + image.getNamespace() + ":" + toVarReplacement(getPortEvnName());
        // return toVarReplacement(getHostEvnName()) + ":" + toVarReplacement(getPortEvnName());
    }

    private String toVarReplacement(String val) {
        return "$(" + Objects.requireNonNull(val, "val can not be null") + ")";
    }

    private String replaceAndUpperCase(String val) {
        return StringUtils.upperCase(StringUtils.replace(val, "-", "_"));
    }

    @Override
    protected String getResourceType() {
        return "service";
    }
}

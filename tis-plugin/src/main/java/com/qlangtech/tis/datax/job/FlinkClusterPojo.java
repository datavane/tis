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

import com.qlangtech.tis.datax.job.ServerLaunchToken.FlinkClusterType;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-01-15 16:47
 **/
public class FlinkClusterPojo {

//    public final static String JSON_KEY_WEB_INTERFACE_URL = "webInterfaceURL";
//    public final static String JSON_KEY_CLUSTER_ID = "clusterId";
//    public final static String JSON_KEY_APP_NAME = DataxUtils.DATAX_NAME;
//    public final static String JSON_KEY_CLUSTER_TYPE = "clusterType";
//    public final static String JSON_KEY_K8S_NAMESPACE = "k8s_namespace";
//    public final static String JSON_KEY_K8S_BASE_PATH = "k8s_base_path";
//    public final static String JSON_KEY_K8S_ID = "k8s_id";

    private String webInterfaceURL;
    private String clusterId;
    private String dataXName;
    private String k8sNamespace;
    private String k8sBasePath;
    private String k8sId;
    private FlinkClusterType clusterType;
    private long createTime;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setClusterType(FlinkClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public FlinkClusterType getClusterType() {
        return clusterType;
    }

    public String getWebInterfaceURL() {
        return webInterfaceURL;
    }

    public void setWebInterfaceURL(String webInterfaceURL) {
        this.webInterfaceURL = webInterfaceURL;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getDataXName() {
        return dataXName;
    }

    public void setDataXName(String dataXName) {
        this.dataXName = dataXName;
    }

    public String getK8sNamespace() {
        return k8sNamespace;
    }

    public void setK8sNamespace(String k8sNamespace) {
        this.k8sNamespace = k8sNamespace;
    }

    public String getK8sBasePath() {
        return k8sBasePath;
    }

    public void setK8sBasePath(String k8sBasePath) {
        this.k8sBasePath = k8sBasePath;
    }

    public String getK8sId() {
        return k8sId;
    }

    public void setK8sId(String k8sId) {
        this.k8sId = k8sId;
    }
}

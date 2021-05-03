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
package com.qlangtech.tis.plugin.k8s;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.k8s.IK8sContext;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IdentityName;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 18:07
 */
public abstract class K8sImage implements Describable<K8sImage>, IdentityName {

    protected abstract String getK8SName();

    public abstract String getNamespace();

    public abstract String getImagePath();

    /**
     *ParamsConfig.createConfigInstance(): io.kubernetes.client.openapi.ApiClient
     *
     * @param
     * @return
     */
    public <T> T createApiClient() {
        ParamsConfig cfg = (ParamsConfig) ParamsConfig.getItem(this.getK8SName(), IK8sContext.class);
        return cfg.createConfigInstance();
    }

    @Override
    public final Descriptor<K8sImage> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }
}

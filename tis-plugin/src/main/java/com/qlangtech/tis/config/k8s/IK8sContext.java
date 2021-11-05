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
package com.qlangtech.tis.config.k8s;

import com.qlangtech.tis.plugin.IdentityName;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IK8sContext extends IdentityName {
    /**
     * k8s认证文本内容
     *
     * @return
     */
    // = "/Users/mozhenghua/.kube/config";
    public String getKubeConfigContent();

    /**
     * 连接host地址
     *
     * @return
     */
    // = "https://cs.2dfire.tech:443";
    public String getKubeBasePath();
}

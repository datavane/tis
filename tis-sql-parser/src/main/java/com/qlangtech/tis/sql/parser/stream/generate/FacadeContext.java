/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.sql.parser.stream.generate;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class FacadeContext {

    private String fullFacadeClassName;

    private String facadeInstanceName;

    private String facadeInterfaceName;

    public String getFullFacadeClassName() {
        return fullFacadeClassName;
    }

    public void setFullFacadeClassName(String fullFacadeClassName) {
        this.fullFacadeClassName = fullFacadeClassName;
    }

    public String getFacadeInstanceName() {
        return facadeInstanceName;
    }

    public String getFacadeInstanceSetterName() {
        return "set" + StringUtils.capitalize(this.getFacadeInstanceName());
    }

    public void setFacadeInstanceName(String facadeInstanceName) {
        this.facadeInstanceName = facadeInstanceName;
    }

    public String getFacadeInterfaceName() {
        return facadeInterfaceName;
    }

    public void setFacadeInterfaceName(String facadeInterfaceName) {
        this.facadeInterfaceName = facadeInterfaceName;
    }
}

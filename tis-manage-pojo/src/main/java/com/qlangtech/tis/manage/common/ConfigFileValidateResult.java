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
package com.qlangtech.tis.manage.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-11-28
 */
public class ConfigFileValidateResult {

    private boolean valid = true;

    public ConfigFileValidateResult() {
        super();
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private final StringBuffer validateResult = new StringBuffer();

    public void appendResult(String value) {
        this.validateResult.append(value);
    }

    public boolean isValid() {
        return valid;
    }

    public String getValidateResult() {
        return this.validateResult.toString();
    }
}

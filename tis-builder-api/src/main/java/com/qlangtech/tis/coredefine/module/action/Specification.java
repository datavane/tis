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
package com.qlangtech.tis.coredefine.module.action;

import org.apache.commons.lang.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规格
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Specification {

    private static final Pattern p = Pattern.compile("(\\d+)(\\w*)");

    public static Specification parse(String val) {
        Matcher m = p.matcher(val);
        if (!m.matches()) {
            throw new IllegalArgumentException("val:" + val + " is not match the pattern:" + p);
        }
        Specification s = new Specification();
        s.setVal(Integer.parseInt(m.group(1)));
        s.setUnit(m.group(2));
        return s;
    }

    private int val;

    private String unit;

    public int getVal() {
        return val;
    }

    public boolean isUnitEmpty() {
        return StringUtils.isEmpty(this.unit);
    }

    public void setVal(int val) {
        this.val = val;
    }

    public int normalizeMemory() {
        int result = 0;
        if ("M".equals(this.getUnit())) {
            result = this.getVal();
        } else if ("G".equals(this.getUnit())) {
            result = this.getVal() * 1024;
        } else {
            throw new IllegalStateException("invalid memory unit:" + this.getUnit());
        }
        return result;
    }

    public String literalVal() {
        return this.getVal() + this.getUnit();
    }

    public int normalizeCPU() {
        // d.setCpuRequest(Specification.parse("300m"));
        // d.setCpuLimit(Specification.parse("2"));
        int result = 0;
        if ("m".equals(this.getUnit())) {
            result = this.getVal();
        } else if (this.isUnitEmpty()) {
            result = this.getVal() * 1024;
        } else {
            throw new IllegalStateException("invalid cpu unit:" + this.getUnit());
        }
        return result;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String toString() {
        return this.val + this.unit;
    }
}

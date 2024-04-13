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
package com.qlangtech.tis.coredefine.module.action;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规格
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Specification {

    private static final Pattern p = Pattern.compile("(\\d+(\\.\\d)?)([mGM]?)");

    public static Specification parse(String val) {
        Matcher m = p.matcher(val);
        if (!m.matches()) {
            throw new IllegalArgumentException("val:" + val + " is not match the pattern:" + p);
        }
        Specification s = new Specification();
        s.setVal(Float.parseFloat(m.group(1)));
        s.setUnit(m.group(3));
        return s;
    }

    private float val;

    private String unit;

    public float getVal() {
        return val;
    }

    public boolean isUnitEmpty() {
        return StringUtils.isEmpty(this.unit);
    }

    public void setVal(float val) {
        this.val = val;
    }

    public int normalizeMemory() {
        return this.normalizeMemory(Optional.empty());
    }

    /**
     * 归一化内存规格，单位：兆
     *
     * @return
     */
    public int normalizeMemory(Optional<Integer> proportion) {
        float result = 0;
        if ("M".equals(this.getUnit())) {
            result = this.getVal();
        } else if ("G".equals(this.getUnit())) {
            result = this.getVal() * 1024;
        } else {
            throw new IllegalStateException("invalid memory unit:" + this.getUnit());
        }
        final float r = result;
        return proportion.map((p) -> {
            if (p < 1 || p > 100) {
                throw new IllegalArgumentException("proportion:" + p + " is invalid");
            }
            return (int) (r * p / 100);
        }).orElse((int) result);
    }

    public String literalVal() {
        return this.getVal() + this.getUnit();
    }

    public int normalizeCPU() {
        // d.setCpuRequest(Specification.parse("300m"));
        // d.setCpuLimit(Specification.parse("2"));
        float result = 0;
        if ("m".equals(this.getUnit())) {
            result = this.getVal();
        } else if (this.isUnitEmpty()) {
            result = this.getVal() * 1024;
        } else {
            throw new IllegalStateException("invalid cpu unit:" + this.getUnit());
        }
        return (int)result;
    }

    public boolean memoryBigThan(Specification spec) {
        Objects.requireNonNull(spec, "param spec can not be null");
        return this.normalizeMemory() > spec.normalizeMemory();
    }

    public boolean cpuBigThan(Specification spec) {
        Objects.requireNonNull(spec, "param spec can not be null");
        return this.normalizeCPU() > spec.normalizeCPU();
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

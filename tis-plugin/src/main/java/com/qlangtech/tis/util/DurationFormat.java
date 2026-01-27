/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.util;

import java.time.Duration;

/**
 * Duration display format enum
 */
public enum DurationFormat {
    /**
     * Chinese format: 120秒, 2分钟, 2小时
     */
    CHINESE(new UnitGetter() {
        @Override
        public String secUnit() {
            return "秒";
        }

        @Override
        public String minUnit() {
            return "分钟";
        }

        @Override
        public String hourUnit() {
            return "小时";
        }

        @Override
        public String secUnit(long value) {
            return secUnit();
        }

        @Override
        public String minUnit(long value) {
            return minUnit();
        }

        @Override
        public String hourUnit(long value) {
            return hourUnit();
        }
    }),
    /**
     * Short English format: 120s, 2min, 2h
     */
    SHORT_EN(new UnitGetter() {
        @Override
        public String secUnit() {
            return "s";
        }

        @Override
        public String minUnit() {
            return "min";
        }

        @Override
        public String hourUnit() {
            return "h";
        }

        @Override
        public String secUnit(long value) {
            return secUnit();
        }

        @Override
        public String minUnit(long value) {
            return minUnit();
        }

        @Override
        public String hourUnit(long value) {
            return hourUnit();
        }
    }),
    /**
     * Full English format: 120 seconds, 2 minutes, 2 hours
     */
    FULL_EN(new UnitGetter() {
        @Override
        public String secUnit() {
            return "second";
        }

        @Override
        public String minUnit() {
            return "minute";
        }

        @Override
        public String hourUnit() {
            return "hour";
        }

        @Override
        public String secUnit(long value) {
            return value == 1 ? "second" : "seconds";
        }

        @Override
        public String minUnit(long value) {
            return value == 1 ? "minute" : "minutes";
        }

        @Override
        public String hourUnit(long value) {
            return value == 1 ? "hour" : "hours";
        }
    });

    private final UnitGetter unitGetter;

    DurationFormat(UnitGetter unitGetter) {
        this.unitGetter = unitGetter;
    }

    /**
     * Unit text getter interface
     */
    interface UnitGetter {
        String secUnit();
        String minUnit();
        String hourUnit();

        String secUnit(long value);
        String minUnit(long value);
        String hourUnit(long value);
    }

    /**
     * Format Duration to human-readable string
     * @param duration the duration to format
     * @return formatted string like "2h", "30min", "120s"
     */
    public String format(Duration duration) {
        if (duration == null) {
            return getDefaultZeroValue();
        }

        long seconds = duration.getSeconds();

        // Convert to hours if divisible by 3600
        if (seconds % 3600 == 0 && seconds >= 3600) {
            long hours = seconds / 3600;
            return formatTimeUnit(hours, "hour");
        }

        // Convert to minutes if divisible by 60
        if (seconds % 60 == 0 && seconds >= 60) {
            long minutes = seconds / 60;
            return formatTimeUnit(minutes, "minute");
        }

        // Otherwise show as seconds
        return formatTimeUnit(seconds, "second");
    }

    /**
     * Get default zero value based on format
     */
    private String getDefaultZeroValue() {
        return "0" + unitGetter.secUnit();
    }

    /**
     * Format time unit with value based on format
     * @param value the numeric value
     * @param unit the unit type: "hour", "minute", or "second"
     */
    private String formatTimeUnit(long value, String unit) {
        String unitText;
        switch (unit) {
            case "hour":
                unitText = unitGetter.hourUnit(value);
                break;
            case "minute":
                unitText = unitGetter.minUnit(value);
                break;
            case "second":
            default:
                unitText = unitGetter.secUnit(value);
                break;
        }

        // FULL_EN format needs space before unit
        if (this == FULL_EN) {
            return value + " " + unitText;
        }
        return value + unitText;
    }

}

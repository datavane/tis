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
 * @date 2020/04/13
 */
public class TimeFormatter {

    private static TimeFormatter instance;

    private String hourSuffix = null;

    private String hoursSuffix = null;

    private String minuteSuffix = null;

    private String minutesSuffix = null;

    private String secondSuffix = null;

    private String secondsSuffix = null;

    private TimeFormatter() {
        this(" hour", " hours", " minute", " minutes", " second", " seconds");
    }

    private TimeFormatter(String hourSuffix, String hoursSuffix, String minuteSuffix, String minutesSuffix, String secondSuffix, String secondsSuffix) {
        this.hourSuffix = hourSuffix;
        this.hoursSuffix = hoursSuffix;
        this.minuteSuffix = minuteSuffix;
        this.minutesSuffix = minutesSuffix;
        this.secondSuffix = secondSuffix;
        this.secondsSuffix = secondsSuffix;
    }

    /**
     * Formats a seconds time value into a brief representation, such as <code>37 minutes</code>.
     * Unicode characters are used to represent 1/4, 1/2 and 3/4 fractions.
     *
     * @param seconds
     * the number of seconds time value.
     *
     * @return
     * a representation of the time.
     */
    public static String formatTime(long timeMillis) {
        if (instance == null) {
            synchronized (TimeFormatter.class) {
                if (instance == null) {
                    instance = new TimeFormatter();
                }
            }
        }
        return instance.formatTime(timeMillis / 1000, true);
    }

    /**
     * Formats a seconds time value into a brief representation, such as <code>37 minutes</code>.
     *
     * @param seconds
     * the number of seconds time value.
     * @param useUnicodeChars
     * if true, special unicode characters are used to represent 1/4, 1/2 and 3/4 fractions.
     * If false, the fractions are displayed in standard text.
     *
     * @return
     * a representation of the time.
     */
    public String formatTime(long seconds, boolean useUnicodeChars) {
        int hours = 0;
        if (seconds > 3600) {
            hours = (int) seconds / 3600;
            seconds = seconds - (hours * 3600L);
        }
        int mins = (int) seconds / 60;
        seconds = seconds - (mins * 60L);
        if (hours > 0) {
            if (mins > 45) {
                return (hours + 1) + hourSuffix;
            } else if (mins > 30) {
                if (useUnicodeChars) {
                    // Three quarters
                    return hours + "\u00BE" + hoursSuffix;
                } else {
                    return hours + " 3/4" + hoursSuffix;
                }
            } else if (mins > 15) {
                if (useUnicodeChars) {
                    // One half
                    return hours + "\u00BD" + hoursSuffix;
                } else {
                    return hours + " 1/2" + hoursSuffix;
                }
            } else if (mins > 0) {
                if (useUnicodeChars) {
                    // One quarter
                    return hours + "\u00BC" + hoursSuffix;
                } else {
                    return hours + " 1/4" + hoursSuffix;
                }
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "");
            }
        } else if (mins > 0) {
            if (seconds > 45) {
                return (mins + 1) + minutesSuffix;
            } else if (seconds > 30) {
                if (useUnicodeChars) {
                    // Three quarters
                    return mins + "\u00BE" + minutesSuffix;
                } else {
                    return mins + " 3/4" + minutesSuffix;
                }
            } else if (seconds > 15) {
                if (useUnicodeChars) {
                    // One half
                    return mins + "\u00BD" + minutesSuffix;
                } else {
                    return mins + " 1/2" + minutesSuffix;
                }
            } else if (seconds > 0) {
                if (useUnicodeChars) {
                    // One quarter
                    return mins + "\u00BC" + minutesSuffix;
                } else {
                    return mins + " 1/4" + minutesSuffix;
                }
            } else {
                return mins + (mins > 1 ? minutesSuffix : minuteSuffix);
            }
        } else {
            return seconds + (seconds != 1 ? secondsSuffix : secondSuffix);
        }
    }
    // public static void main(String[] args) throws Exception {
    // TimeFormatter formatter = new TimeFormatter();
    // long seconds = 2 * 60 * 60;
    // 
    // while (seconds >= 0) {
    // System.out.println(formatter.formatTime(seconds));
    // Thread.sleep(5);
    // seconds--;
    // }
    // 
    // }
}

/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.apache.log4j;

/*
 * <font color="#AA4444">Refrain from using this class directly, use the
 * {@link Level} class instead</font>.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Priority {

    transient int level;

    transient String levelStr;

    transient int syslogEquivalent;

    public static final int OFF_INT = Integer.MAX_VALUE;

    public static final int FATAL_INT = 50000;

    public static final int ERROR_INT = 40000;

    public static final int WARN_INT = 30000;

    public static final int INFO_INT = 20000;

    public static final int DEBUG_INT = 10000;

    // public final static int FINE_INT = DEBUG_INT;
    public static final int ALL_INT = Integer.MIN_VALUE;

    /**
     * @deprecated Use {@link Level#FATAL} instead.
     */
    public static final Priority FATAL = new Level(FATAL_INT, "FATAL", 0);

    /**
     * @deprecated Use {@link Level#ERROR} instead.
     */
    public static final Priority ERROR = new Level(ERROR_INT, "ERROR", 3);

    /**
     * @deprecated Use {@link Level#WARN} instead.
     */
    public static final Priority WARN = new Level(WARN_INT, "WARN", 4);

    /**
     * @deprecated Use {@link Level#INFO} instead.
     */
    public static final Priority INFO = new Level(INFO_INT, "INFO", 6);

    /**
     * @deprecated Use {@link Level#DEBUG} instead.
     */
    public static final Priority DEBUG = new Level(DEBUG_INT, "DEBUG", 7);

    /**
     * Default constructor for deserialization.
     */
    protected Priority() {
        level = DEBUG_INT;
        levelStr = "DEBUG";
        syslogEquivalent = 7;
    }

    /**
     * Instantiate a level object.
     */
    protected Priority(int level, String levelStr, int syslogEquivalent) {
        this.level = level;
        this.levelStr = levelStr;
        this.syslogEquivalent = syslogEquivalent;
    }

    /**
     * Two priorities are equal if their level fields are equal.
     *
     * @since 1.2
     */
    public boolean equals(Object o) {
        if (o instanceof Priority) {
            Priority r = (Priority) o;
            return (this.level == r.level);
        } else {
            return false;
        }
    }

    /**
     * Return the syslog equivalent of this priority as an integer.
     */
    public final int getSyslogEquivalent() {
        return syslogEquivalent;
    }

    /**
     * Returns <code>true</code> if this level has a higher or equal level than
     * the level passed as argument, <code>false</code> otherwise.
     *
     * <p>
     * You should think twice before overriding the default implementation of
     * <code>isGreaterOrEqual</code> method.
     */
    public boolean isGreaterOrEqual(Priority r) {
        return level >= r.level;
    }

    /**
     * Return all possible priorities as an array of Level objects in descending
     * order.
     *
     * @deprecated This method will be removed with no replacement.
     */
    public static Priority[] getAllPossiblePriorities() {
        return new Priority[] { Priority.FATAL, Priority.ERROR, Level.WARN, Priority.INFO, Priority.DEBUG };
    }

    /**
     * Returns the string representation of this priority.
     */
    public final String toString() {
        return levelStr;
    }

    /**
     * Returns the integer representation of this level.
     */
    public final int toInt() {
        return level;
    }

    /**
     * @deprecated Please use the {@link Level#toLevel(String)} method instead.
     */
    public static Priority toPriority(String sArg) {
        return Level.toLevel(sArg);
    }

    /**
     * @deprecated Please use the {@link Level#toLevel(int)} method instead.
     */
    public static Priority toPriority(int val) {
        return toPriority(val, Priority.DEBUG);
    }

    /**
     * @deprecated Please use the {@link Level#toLevel(int, Level)} method
     *             instead.
     */
    public static Priority toPriority(int val, Priority defaultPriority) {
        return Level.toLevel(val, (Level) defaultPriority);
    }

    /**
     * @deprecated Please use the {@link Level#toLevel(String, Level)} method
     *             instead.
     */
    public static Priority toPriority(String sArg, Priority defaultPriority) {
        return Level.toLevel(sArg, (Level) defaultPriority);
    }
}

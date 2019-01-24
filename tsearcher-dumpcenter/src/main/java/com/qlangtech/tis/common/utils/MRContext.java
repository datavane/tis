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
package com.qlangtech.tis.common.utils;

import java.util.HashMap;

/*
 * @description
 * @since  2011-10-24 下午04:31:03
 * @version  1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MRContext extends HashMap<String, Object> {

    public static final String DEFAULT_FSURL = "fsUrl";

    public static final String DEFAULT_SERVICE = "serviceName";

    public static final String DEFAULT_EXCUTE_USER = "userName";

    public static final String DEFAULT_DUMP_TYPE = "dumpType";

    public static final String DEFAULT_TIMEPONIT = "timePoint";

    public static final String DEFAULT_CONFIG = "configuration";

    public static final String DEFAULT_INPUT = "input";

    public static final String DEFAULT_OUTPUT = "output";

    public static final String DEFAULT_GROUP = "group";

    public static final String SYN_INDEX_TYPE = "synIndexType";

    public static final String COMBINER_CLASS = "combinerClass";

    /**
     * @uml.property  name="rEDUCE_NUM"
     */
    public static final String REDUCE_NUM = "reduceNum";

    public MRContext() {
    }

    /**
     * @author   yingyuan.lyq
     */
    public enum SynIndexEnum {

        /**
         * @uml.property  name="dEFAULT_SYN_INDEX"
         * @uml.associationEnd
         */
        DEFAULT_SYN_INDEX("default.syn.index"),
        /**
         * @uml.property  name="rAM_MERGE_SYN_INDEX"
         * @uml.associationEnd
         */
        RAM_MERGE_SYN_INDEX("ram.merge.syn.index");

        /**
         * @uml.property  name="value"
         */
        private String value;

        SynIndexEnum(String value) {
            this.value = value;
        }

        /**
         * @return
         * @uml.property  name="value"
         */
        public String getValue() {
            return this.value;
        }
    }

    public String getSynIndexEnum() {
        return (String) this.get(SYN_INDEX_TYPE);
    }

    public void setSynIndexEnum(String synIndexEnum) {
        this.put(SYN_INDEX_TYPE, synIndexEnum);
    }

    public void setCombinerClassName(String className) {
        this.put(COMBINER_CLASS, className);
    }

    public String getCombinerClassName() {
        return (String) this.get(COMBINER_CLASS);
    }

    public String getfsUrl() {
        return (String) this.get(DEFAULT_FSURL);
    }

    public void setfsUrl(String fsUrl) {
        this.put(DEFAULT_FSURL, fsUrl);
    }

    public String getServiceName() {
        return (String) this.get(DEFAULT_SERVICE);
    }

    public void setServiceName(String serviceName) {
        this.put(DEFAULT_SERVICE, serviceName);
    }

    public String getExcuteUserName() {
        return (String) this.get(DEFAULT_EXCUTE_USER);
    }

    public void setExcuteUserName(String userName) {
        this.put(DEFAULT_EXCUTE_USER, userName);
    }

    public void setDumpType(String dumpType) {
        this.put(DEFAULT_DUMP_TYPE, dumpType);
    }

    public String getDumpType() {
        return (String) this.get(DEFAULT_DUMP_TYPE);
    }

    public String getTimePoint() {
        return (String) get(DEFAULT_TIMEPONIT);
    }

    public void setTimePoint(String timePoint) {
        this.put(DEFAULT_TIMEPONIT, timePoint);
    }

    public void setReduceNum(Integer reduceNum) {
        this.put(REDUCE_NUM, reduceNum);
    }

    /**
     * @return
     * @uml.property  name="rEDUCE_NUM"
     */
    public Integer getReduceNum() {
        return (Integer) get(REDUCE_NUM);
    }
}

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
package com.qlangtech.tis.common.data.processor;

import java.util.Map;

/*
 * DataProvider获取的数据进行处理的接口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface DataProcessor {

    /**
     * 该DataProcessor的职责描述
     * @return
     */
    String getDesc();

    /**
     * 数据处理方法,如果该行数据不符合要求，亦即不需要索引的数据,直接返回null，dump程序会自动忽略该数据
     *
     * @param map 代表一行数据，亦即一个Document数据源
     * @throws DataProcessException
     */
    ResultCode process(Map<String, String> map) throws DataProcessException;

    /**
     * @author  yingyuan.lyq
     */
    public class ResultCode {

        int code;

        String msg;

        /**
         * @uml.property  name="sUC"
         * @uml.associationEnd
         */
        public static final ResultCode SUC = new ResultCode(0, "处理成功");

        /**
         * @uml.property  name="fAI"
         * @uml.associationEnd
         */
        public static final ResultCode FAI = new ResultCode(-1, "处理失败");

        private ResultCode(int code, String msg) {
            super();
            this.code = code;
            this.msg = msg;
        }

        public ResultCode(String msg) {
            this(-2, msg);
        }

        public boolean isSuc() {
            return this.code == 0;
        }

        @Override
        public String toString() {
            return "code : " + code + "  msg : " + msg;
        }
    }
}

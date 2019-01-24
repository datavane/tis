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
package com.qlangtech.tis.common.data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * ds1:2,3,5-7, 9 ;ds2: 1,4,11,20
 * 默认的分库分表规则解析器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultTableDescriptionParser implements TableDescriptionParser {

    protected Log logger = LogFactory.getLog(DefaultTableDescriptionParser.class);

    protected static DecimalFormat format = new DecimalFormat("_0000");

    protected String preprocess(String raw) {
        return raw.replaceAll(" ", "");
    }

    protected void generateZone(String begin, String end, List<String> store) throws SubtableDescParseException {
        try {
            int head = Integer.parseInt(begin);
            int tail = Integer.parseInt(end);
            for (int i = head; i <= tail; i++) {
                store.add(format.format(i));
            }
        } catch (NumberFormatException nfe) {
            logger.error("生成区间时数字转化错误", nfe);
            throw new SubtableDescParseException("生成区间时数字转化错误", nfe);
        }
    }

    @Override
    public Map<String, List<String>> parse(String raw) throws SubtableDescParseException {
        String base = this.preprocess(raw);
        Map<String, List<String>> store = new LinkedHashMap<String, List<String>>();
        StringBuilder sb = new StringBuilder();
        boolean hasZone = false;
        String dsName = null;
        String begin = null;
        String end = null;
        for (char c : base.toCharArray()) {
            switch(c) {
                case ':':
                    dsName = sb.toString();
                    store.put(dsName, new ArrayList<String>());
                    sb = new StringBuilder();
                    break;
                case '-':
                    if (sb.length() == 0 || hasZone) {
                        logger.error("出现-时必须是区间，并且在当前区间处理结束前不能处理新的区间");
                        throw new SubtableDescParseException("出现-时必须是区间，并且在当前区间处理结束前不能处理新的区间");
                    }
                    begin = sb.toString();
                    sb = new StringBuilder();
                    hasZone = true;
                    break;
                case ';':
                case ',':
                    if (dsName == null) {
                        // 当出现$符号时，即单表情况，直接跳过
                        break;
                    }
                    if (hasZone) {
                        end = sb.toString();
                        this.generateZone(begin, end, store.get(dsName));
                        hasZone = false;
                    } else {
                        store.get(dsName).add(format.format(Long.parseLong(sb.toString())));
                    }
                    sb = new StringBuilder();
                    break;
                case '$':
                    sb = new StringBuilder();
                    // $表示单表情况，把dsName去掉，以免误用
                    if (store.get(dsName).size() != 0) {
                        logger.error("出现$符号，不应含有后缀");
                        throw new SubtableDescParseException("出现$符号，不应含有后缀");
                    }
                    dsName = null;
                    break;
                default:
                    sb.append(c);
            }
        }
        if (sb.length() != 0) {
            if (hasZone) {
                end = sb.toString();
                this.generateZone(begin, end, store.get(dsName));
            } else {
                store.get(dsName).add(sb.toString());
            }
            sb = new StringBuilder();
        }
        return store;
    }

    public static void main(String[] args) throws Exception {
        TableDescriptionParser parser = new DefaultTableDescriptionParser();
        String raw = "ds1:2,3,5-7, 9 ;ds2: 1,4,11,20, 21-25;ds3:$;ds4:30-32";
        long begin = System.nanoTime();
        System.out.println(parser.parse(raw));
        System.out.println("total spend time: " + (System.nanoTime() - begin) / 1000000.0 + "ms");
    }
}

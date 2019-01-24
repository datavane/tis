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
package com.qlangtech.tis.solrextend.fieldtype.pinyin;

import java.util.HashMap;
import java.util.Map;

/*
 * 将一段全拼的拼音按照单个字来切分
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PinyinUtils {

    static String[] sm = { "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "w", "x", "y", "z", "sh", "zh", "ch" };

    private static Map<String, PinyinMapping> map = new HashMap<>();

    static PinyinMapping root;

    static {
        PinyinMapping obj = new PinyinMapping();
        PinyinMapping n = new PinyinMapping();
        n.getMapping().put("g", obj);
        PinyinMapping a = new PinyinMapping();
        a.getMapping().put("i", obj);
        a.getMapping().put("o", obj);
        a.getMapping().put("n", n);
        map.put("a", a);
        PinyinMapping e = new PinyinMapping();
        e.getMapping().put("i", obj);
        e.getMapping().put("r", obj);
        e.getMapping().put("n", n);
        map.put("e", e);
        PinyinMapping o = new PinyinMapping();
        o.getMapping().put("u", obj);
        o.getMapping().put("n", n);
        map.put("o", o);
        PinyinMapping u = new PinyinMapping();
        u.getMapping().put("e", obj);
        u.getMapping().put("o", obj);
        u.getMapping().put("i", obj);
        PinyinMapping ua = new PinyinMapping();
        ua.getMapping().put("i", obj);
        ua.getMapping().put("n", n);
        u.getMapping().put("a", ua);
        u.getMapping().put("n", n);
        map.put("u", u);
        PinyinMapping i = new PinyinMapping();
        PinyinMapping io = new PinyinMapping();
        io.getMapping().put("n", n);
        PinyinMapping ia = new PinyinMapping();
        ia.getMapping().put("n", n);
        ia.getMapping().put("o", obj);
        i.getMapping().put("a", ia);
        i.getMapping().put("o", io);
        i.getMapping().put("e", obj);
        i.getMapping().put("u", obj);
        i.getMapping().put("n", n);
        map.put("i", i);
        root = new PinyinMapping();
        root.setMapping(map);
    }

    public static String[] split(String pinyin) {
        String result = "";
        for (int index = 0; index < pinyin.length(); ) {
            if (((pinyin.charAt(index) == 's') || (pinyin.charAt(index) == 'z') || (pinyin.charAt(index) == 'c')) && (index < pinyin.length() - 1) && (pinyin.charAt(index + 1) == 'h')) {
                result = result + " " + pinyin.charAt(index) + 'h' + " ";
                index += 2;
            } else {
                boolean flag = false;
                for (String string : sm) {
                    if (string.equals(String.valueOf(pinyin.charAt(index)))) {
                        result = result + " " + pinyin.charAt(index);
                        flag = true;
                        index++;
                        break;
                    }
                }
                if (!flag) {
                    result = result + " ";
                }
            }
            String tmp = "";
            PinyinMapping tempMapping = root;
            while (index < pinyin.length()) {
                char character = pinyin.charAt(index);
                tempMapping = (PinyinMapping) tempMapping.getMapping().get(character);
                tmp = tmp + pinyin.charAt(index);
                index++;
                if (tempMapping == null) {
                    break;
                }
                if ((tempMapping == null) || (index >= pinyin.length()) || (tempMapping.getMapping().get(pinyin.charAt(index)) == null)) {
                    break;
                }
            }
            result = result + tmp;
        }
        return result.trim().split(" ");
    }

    public static void main(String[] args) {
        String[] part = split("yuxiangrousi");
        for (String string : part) {
            System.out.println(string + " ");
        }
        System.out.println();
    }

    static class PinyinMapping {

        private PinyinTreeNode mapping;

        // = new PinyinTreeNode(
        // (Map<String, PinyinMapping>) Collections.emptyMap());
        PinyinTreeNode getMapping() {
            return mapping;
        }

        /**
         * @param mapping
         */
        public PinyinMapping() {
            super();
            this.mapping = new PinyinTreeNode(new HashMap<String, PinyinMapping>());
        }

        void setMapping(Map<String, PinyinMapping> mapping) {
            this.mapping = new PinyinTreeNode(mapping);
        }
    }

    static class PinyinTreeNode {

        private final Map<String, PinyinMapping> mapping;

        /**
         * @param mapping
         */
        public PinyinTreeNode(Map<String, PinyinMapping> mapping) {
            super();
            this.mapping = mapping;
        }

        public void put(String key, PinyinMapping mapping) {
            this.mapping.put(key, mapping);
        }

        public PinyinMapping get(String key) {
            return mapping.get(key);
        }

        public PinyinMapping get(char key) {
            return mapping.get(String.valueOf(key));
        }
    }
}

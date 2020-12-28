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
package com.qlangtech.tis.indexbuilder.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SimpleStringTokenizer {
//
//    private static final Log log = LogFactory.getLog(SimpleStringTokenizer.class);
//
//    StringTokenizer st = null;
//
//    String delim;
//
//    int length = -1;
//
//    /**
//     * @param str
//     * 		要切分的内容
//     * @param delim
//     * 		分隔符
//     */
//    public SimpleStringTokenizer(String str, String delim) {
//        st = new StringTokenizer(str, delim, true);
//        this.delim = delim;
//    }
//
//    /**
//     * @param str
//     * 		要切分的内容
//     * @param delim
//     * 		分隔符
//     * @param length
//     * 		默认分隔结果的个数,如果实际分割结果小于该长度,后面的都会被填成空
//     */
//    public SimpleStringTokenizer(String str, String delim, int length) {
//        st = new StringTokenizer(str, delim, true);
//        this.delim = delim;
//        this.length = length;
//    }

//    /**
//     * 原来为空的字段都填上空字符,然后存放在list里面返回
//     * @return
//     */
//    public static List<String> getAllElements(String src, String delimiter) {
//        List<String> values = new ArrayList<String>();
//        boolean dataflag = false;
//        StringTokenizer st = new StringTokenizer(src, delimiter, true);
//        while (st.hasMoreTokens()) {
//            String word = st.nextToken();
//            if (!delimiter.equals(word)) {
//                values.add(word);
//                dataflag = true;
//            } else {
//                if (dataflag)
//                    dataflag = false;
//                else {
//                    values.add("");
//                }
//            }
//        }
//        if (!dataflag) {
//            values.add("");
//        }
//        return values;
//    }

    /**
     * 原来为空的字段都填上空字符,然后存放数组里面返回
     * @return
     */
    public static String[] split(String src, String delimiter) {
        return StringUtils.splitPreserveAllTokens(src, delimiter);
        // log.warn("----->"+src);
//        List<String> values = new ArrayList<String>();
//        boolean dataflag = false;
//        StringTokenizer st = new StringTokenizer(src, delimiter, true);
//        while (st.hasMoreTokens()) {
//            String word = st.nextToken();
//            // System.out.println("token="+word);
//            if (!delimiter.equals(word)) {
//                values.add(word);
//                dataflag = true;
//            } else {
//                if (dataflag)
//                    dataflag = false;
//                else {
//                    values.add("");
//                }
//            }
//        }
//        if (!dataflag) {
//            values.add("");
//        }
//        /*if(length >= 1 && values.size() < length){
//			for(int i=0; i<length-values.size(); i++)
//				values.add("");
//		}*/
//        String[] a = new String[values.size()];
//        return values.toArray(a);
    }

    public static void main(String[] args) throws IOException {
        /*File file = new File("d:/part-00008--2-5");
		FileInputStream fi = new FileInputStream(file);
		//BufferedReader br = new BufferedReader(new InputStreamReader(fi,"UTF-8"));
		BufferedReader br = new BufferedReader(new InputStreamReader(fi,"UTF-8"));
		String line = "";
		while((line = br.readLine())!=null)
		{
			String a[]=line.split("\t");//SimpleStringTokenizer.split(src, "\t");
			System.out.println(a.length);
			if(a.length!=9)
			{
				System.out.println(a.length);
				for(int i=0;i<a.length;i++)
				{
					System.out.println(i+"--"+a[i]+"--");
				}
			}
		}
*/
        String line = "69969026487148633412450621789469905544901019320140623\t\t卧室\t现代\t大户型\t木纹砖0";

        String[] lines = StringUtils.splitPreserveAllTokens(line, "\t");
      //  String[] lines =    line.split("\t");
       // String[] lines = split(line, "\t");
        System.out.println(lines.length);
        for (int i = 0; i < lines.length; i++) {
            System.out.println(lines[i]);
        }
    }
}

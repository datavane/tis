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
package com.qlangtech.tis.hdfs.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
 * @description
 * @since 2011-8-9 08:08:23
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FormatTool {

    public static String formatInteger(int f) {
        return formatInteger(new Integer(f));
    }

    // format the float to string, seperate by ',' every 3 characters.
    // e.g. 12345 to 12,345 1234.5 to 1,234.5 1.2345E3 to 1,234.5
    // Note if the float given is a large one (more than a million), the change
    // would lost precision
    public static String format(Double f) {
        if (f == null) {
            return "0";
        }
        String sf = f.toString();
        // Logger.info("sf1:"+sf);
        // change 1.234E2 to 123.4, 3.0E5 to 300000
        // problem: once change, the precision would be lost
        int eindex = sf.indexOf("E");
        if (eindex > 0) {
            // Logger.info("In formatTool before change: "+sf);
            String sf_1 = sf.substring(0, eindex);
            String sf_length = sf.substring(eindex + 1);
            int length = Integer.parseInt(sf_length);
            int sf_1_index = sf_1.indexOf(".");
            // before .
            String sf_1_1 = sf_1.substring(0, sf_1_index);
            // after .
            String sf_1_2 = sf_1.substring(sf_1_index + 1);
            if (sf_1_2.length() < length) {
                // 3.0E5 sf_1_2=0, length = 5
                int count = length - sf_1_2.length();
                while (count > 0) {
                    sf_1_2 = sf_1_2 + "0";
                    count--;
                }
            }
            String sf_1_3 = sf_1_2.substring(0, length) + "." + sf_1_2.substring(length);
            sf = sf_1_1 + sf_1_3;
            if (sf.endsWith(".")) {
                sf = sf.substring(0, sf.length() - 1);
            }
        // Logger.info("In formatTool after change: "+sf);
        }
        int index = sf.indexOf(".");
        // Logger.info("sf2:"+sf);
        String intString = null;
        String floatString = null;
        if (index > 0) {
            // has float
            intString = sf.substring(0, index);
            floatString = sf.substring(index + 1);
            if (floatString.length() == 1) {
                floatString = floatString + "0";
            }
        // Logger.info("floatString:"+floatString);
        } else {
            // has not float part
            intString = sf;
            floatString = "00";
        }
        // format intString part
        int spaceCount = intString.length() - 1;
        int count = 2;
        while (spaceCount > 0) {
            spaceCount--;
            count--;
            if (count == 0) {
                intString = intString.substring(0, spaceCount) + "," + intString.substring(spaceCount);
                count = 3;
            }
        }
        if (intString.indexOf(",") == 0) {
            intString = intString.substring(1);
        }
        if (floatString != null) {
            // will be displayed
            if (floatString.length() > 2) {
                floatString = floatString.substring(0, 2);
            }
            intString = intString + "." + floatString;
        }
        return intString;
    }

    public static String format(int number) {
        return format(new Double(number));
    }

    public static String format(double number) {
        return format(new Double(number));
    }

    public static String format(BigDecimal number) {
        return formatBigDecimal(number);
    }

    public static String format(Integer number) {
        if (number == null) {
            return "0";
        }
        // return format(new Double(number.toString()));
        return formatInteger(number);
    }

    public static String format(Float number) {
        if (number == null) {
            return "0";
        }
        return format(new Float(number.floatValue()));
    }

    public static String format(float number) {
        return format(new Double(number));
    }

    /**
     * if the length of the input string is longer than the specified length,
     * substitute the longer part with "..."
     *
     * @param input,
     *            String
     * @param length
     * @return
     */
    public static String alterLongString(String input, int length) {
        if (input == null) {
            return "";
        }
        if (input.length() > length) {
            input = input.substring(0, length) + "...";
        }
        return input;
    }

    public class DeptPrintCalcu {

        public int pageRowsCapacity = 11;

        public int titleSpace = 2;

        public int subtitleSpace = 1;

        public int pageNoSpace = 1;

        public int tableInterSpace = 1;

        public int itemSpace = 1;

        public int preservedRow = 1;

        public int tableRows = 0;

        // add by liupeng 2008-04-08
        public int pageRowsCapacityPageNo1 = 5;

        // calculated fields
        public int pageItemRows = 0;

        public int totalDisplayRow = 0;

        public int totalPage = 0;

        // calculate the configuration value
        public void initCalcu() {
            this.preservedRow = this.subtitleSpace;
            this.pageItemRows = this.pageRowsCapacity - this.titleSpace - this.pageNoSpace - this.preservedRow;
        }

        public DeptPrintCalcu() {
        }

        public DeptPrintCalcu(int pageRowsCapacity, int titleSpace, int subtitleSpace, int pageNoSpace, int tableInterSpace, int itemSpace) {
            super();
            this.pageRowsCapacity = pageRowsCapacity;
            this.titleSpace = titleSpace;
            this.subtitleSpace = subtitleSpace;
            this.pageNoSpace = pageNoSpace;
            this.tableInterSpace = tableInterSpace;
            this.itemSpace = itemSpace;
            this.preservedRow = this.subtitleSpace;
            this.pageRowsCapacity = pageRowsCapacity;
        }

        // *************** add by liupeng 2008-04-11
        public DeptPrintCalcu(int pageRowsCapacity, int pageRowsCapacityPageNo1, int titleSpace, int subtitleSpace, int pageNoSpace, int tableInterSpace, int itemSpace) {
            super();
            this.pageRowsCapacity = pageRowsCapacity;
            this.titleSpace = titleSpace;
            this.subtitleSpace = subtitleSpace;
            this.pageNoSpace = pageNoSpace;
            this.tableInterSpace = tableInterSpace;
            this.itemSpace = itemSpace;
            this.preservedRow = this.subtitleSpace;
            this.pageRowsCapacity = pageRowsCapacity;
            this.pageRowsCapacityPageNo1 = pageRowsCapacityPageNo1;
        }

        // *************** add by liupeng 2008-04-11
        /**
         * just change the capacity to contain number of rows in one page
         *
         * @param rows
         */
        public void adjustCapacityByRows(int rows) {
            this.pageRowsCapacity = this.titleSpace + this.itemSpace * rows + this.preservedRow + this.pageNoSpace;
        }

        // *************** add by liupeng 2008-04-11
        // public void adjustCapacityByRowsPageNo1(int rows){
        // this.pageRowsCapacityPageNo1 =
        // this.titleSpace+this.itemSpace*rows+this.preservedRow+this.pageNoSpace;
        // }
        // *************** add by liupeng 2008-04-11
        /**
         * calculate output of tables
         *
         * @param tablerows,
         *            List<Integer>
         * @return HashMap(pageNo(Integer):List<startRowNo(Integer),endRowNo(
         *         Integer ), hasSubTitle(Integer)>) Note: pageNo,Integer: start
         *         from 1 startRowNo,Integer: start from 0 endRowNo, Integer
         *         hasSubTitle, Integer: 0--false; 1--true if more than one
         *         table are printed on the same page, the page list should be
         *         <startRowNo,endRowNo, hasSubTitle(1),
         *         hasNextTable(1),startRowNo,endRowNo,hasSubTitle>
         */
        public HashMap printCalculate(List tablerows) {
            HashMap pageMap = new HashMap();
            if ((tablerows == null) || (tablerows.size() == 0)) {
                return pageMap;
            }
            if (tablerows.size() == 1) {
                pageMap = this.deptPrintCalculate(((Integer) tablerows.get(0)).intValue());
            }
            if (tablerows.size() > 1) {
                pageMap = this.deptPrintCalculate(((Integer) tablerows.get(0)).intValue());
                for (int i = 1; i < tablerows.size(); i++) {
                    pageMap = this.printNextTableCalculate(pageMap, ((Integer) tablerows.get(i)).intValue(), i, tablerows);
                }
                return pageMap;
            }
            return pageMap;
        }

        public HashMap deptPrintCalculate(int tableRows) {
            this.tableRows = tableRows;
            return this.deptPrintCalculate();
        }

        /**
         * calculate the page show item
         *
         * @return HashMap(pageNo(Integer):List<startRowNo(Integer),endRowNo(
         *         Integer ), hasSubTitle(Integer)>) Note: pageNo,Integer: start
         *         from 1 startRowNo,Integer: start from 0 endRowNo, Integer
         *         hasSubTitle, Integer: 0--false; 1--true first consider only
         *         one table
         */
        public HashMap deptPrintCalculate() {
            this.initCalcu();
            // calculate the configuration value
            this.pageItemRows = this.pageRowsCapacity - this.titleSpace - this.pageNoSpace - this.preservedRow;
            this.totalDisplayRow = this.tableRows + this.subtitleSpace;
            this.totalPage = this.tableRows / this.pageItemRows + 1;
            // calculate the page configuration
            HashMap pageMap = new HashMap();
            if (this.tableRows == 0) {
                return pageMap;
            }
            int pageCount = 0;
            int leftTableRows = this.tableRows;
            int startTableRow = 0;
            int endTableRow = 0;
            List pageConfigList = new ArrayList();
            while (leftTableRows > 0) {
                // ***************edit by liupeng 2008-04-11
                if (pageCount == 0) {
                    startTableRow = 0;
                    endTableRow = this.pageRowsCapacityPageNo1;
                    leftTableRows = leftTableRows - this.pageRowsCapacityPageNo1 - 1;
                } else {
                    startTableRow = pageCount * this.pageItemRows - (this.pageItemRows - this.pageRowsCapacityPageNo1 - 1);
                    endTableRow = startTableRow + this.pageItemRows - 1;
                    leftTableRows -= this.pageItemRows;
                }
                // ***************edit by liupeng 2008-04-11
                pageCount++;
                pageConfigList = new ArrayList();
                pageConfigList.add(new Integer(startTableRow));
                pageConfigList.add(new Integer(endTableRow));
                pageConfigList.add(new Integer(0));
                pageMap.put(new Integer(pageCount), pageConfigList);
                if (leftTableRows <= 0) {
                    pageConfigList.remove(1);
                    pageConfigList.remove(1);
                    pageConfigList.add(new Integer(this.tableRows - 1));
                    pageConfigList.add(new Integer(1));
                    break;
                }
            }
            return pageMap;
        }

        /**
         * precondition: has at least one table info or has to call initCalcu to
         * initialize configuration Note: the previous table must not be empty
         * calculate the second table on the basis of the previous one
         *
         * @param tableMap,
         *            HashMap(pageNo(Integer):List<startRowNo(Integer),endRowNo
         *            (Integer), hasSubTitle(Integer)>)
         * @param tableRows,
         *            next tablerows
         * @return tableMap
         *         HashMap(pageNo(Integer):List<startRowNo(Integer),endRowNo
         *         (Integer),
         *         hasSubTitle(Integer),hasNextTable(Integer),startRowNo
         *         ,endRowNo, hasSubTitle>)
         */
        public HashMap printNextTableCalculate(HashMap pageMap, int tableRows, int i, List tablerows) {
            this.initCalcu();
            // get the last page info
            List pageInfo = (List) pageMap.get(new Integer(pageMap.size()));
            int preStartRowNo = ((Integer) pageInfo.get(0)).intValue();
            int preEndRowNo = ((Integer) pageInfo.get(1)).intValue();
            int prelines = preEndRowNo - preStartRowNo + 1;
            // judge if next table can be printed on the same page
            // edit by liupeng 0416
            int requiredLines = prelines + this.subtitleSpace + this.tableInterSpace + this.titleSpace;
            int tablerowsNum0 = ((Integer) tablerows.get(0)).intValue();
            int tablerowsNum1 = 0;
            int tableLines1 = 0;
            if (tablerows.size() > 1) {
                tablerowsNum1 = ((Integer) tablerows.get(1)).intValue();
                tableLines1 = tablerowsNum1 + +this.subtitleSpace + this.tableInterSpace + this.titleSpace;
            }
            if (tablerows.size() > 2) {
            // int tablerowsNum2 = ((Integer) tablerows.get(2)).intValue();
            // int tableLines2 = tablerowsNum2 + +this.subtitleSpace +
            // this.tableInterSpace + this.titleSpace;
            }
            int table0Spare = (tablerowsNum0 - this.pageRowsCapacityPageNo1 - 1) % this.pageItemRows;
            if (i == 1) {
                requiredLines = prelines + this.subtitleSpace + this.tableInterSpace + this.titleSpace;
                if (pageMap.size() == 1) {
                    requiredLines = prelines + this.subtitleSpace + this.tableInterSpace + this.titleSpace;
                    // int firstPageTable = this.pageItemRows -
                    // requiredLines;
                    /**
                     * ********************************************************
                     * 閿熸枻鎷烽敓鏂ゆ嫹閿熸彮绛规嫹闆嶉敓閰碉拷鎷烽敓鑴氭唻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷峰嵏閿燂拷if(
                     * firstPageTable<=tableLines1){ requiredLines =
                     * tableLines1+
                     * this.pageItemRows+prelines+this.subtitleSpace+
                     * this.tableInterSpace+this.titleSpace; }
                     * ********************************************************
                     */
                    requiredLines = tableLines1 + this.pageItemRows + prelines + this.subtitleSpace + this.tableInterSpace + this.titleSpace;
                }
            } else {
                if (pageMap.size() != 1 && table0Spare == 0) {
                    // requiredLines = tableLines1-prelines;
                    requiredLines = 0;
                } else if (tablerowsNum0 <= this.pageRowsCapacityPageNo1 + 1) {
                    requiredLines = 0;
                } else {
                    int conAndpoLines = table0Spare + this.subtitleSpace + this.tableInterSpace + this.titleSpace;
                    if (conAndpoLines <= this.pageItemRows) {
                        requiredLines = tableLines1 + this.pageItemRows + prelines + this.subtitleSpace + this.tableInterSpace + this.titleSpace;
                    }
                }
            }
            // edit by liupeng 0416
            int leftRowsCurPage = 0;
            int startRowNo = 0;
            int endRowNo = 0;
            int hasSubTitle = 0;
            int printedRows = 0;
            // print on the previous page
            if (requiredLines + 1 <= this.pageItemRows) {
                // set hasNextTable flag
                pageInfo.add(new Integer(1));
                leftRowsCurPage = this.pageItemRows - requiredLines;
                startRowNo = 0;
                if (leftRowsCurPage >= tableRows) {
                    endRowNo = tableRows - 1;
                    hasSubTitle = 1;
                } else {
                    endRowNo = leftRowsCurPage - 1;
                    printedRows = leftRowsCurPage;
                }
                pageInfo.add(new Integer(startRowNo));
                pageInfo.add(new Integer(endRowNo));
                pageInfo.add(new Integer(hasSubTitle));
                pageMap.put(new Integer(pageMap.size()), pageInfo);
                // if print the whole second table, add the pageInfo and return
                if (hasSubTitle == 1) {
                    return pageMap;
                }
                startRowNo = printedRows;
            }
            // print left rows on next page
            int leftRows = tableRows - printedRows;
            int startPageCount = pageMap.size() + 1;
            int newPageCount = 0;
            int initialRowNo = startRowNo;
            while (leftRows > 0) {
                startRowNo = initialRowNo + newPageCount * this.pageItemRows;
                endRowNo = startRowNo + this.pageItemRows - 1;
                leftRows -= this.pageItemRows;
                hasSubTitle = 0;
                // meet the end of the second table
                if (leftRows <= 0) {
                    endRowNo = tableRows - 1;
                    hasSubTitle = 1;
                }
                pageInfo = new ArrayList();
                pageInfo.add(new Integer(startRowNo));
                pageInfo.add(new Integer(endRowNo));
                pageInfo.add(new Integer(hasSubTitle));
                pageMap.put(new Integer(startPageCount + newPageCount), pageInfo);
                newPageCount++;
                if (hasSubTitle == 1) {
                    break;
                }
            }
            return pageMap;
        }
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM");

    private static SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMM");

    private static SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy");

    private static SimpleDateFormat sdf6 = new SimpleDateFormat("yyMMdd");

    private static SimpleDateFormat sdf7 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String formatDate2Str(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf.format(date);
        }
    }

    public static String formatDate3Str3(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf3.format(date);
        }
    }

    public static String formatDate2Str2(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf2.format(date);
        }
    }

    public static String formatDate2Str8(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf8.format(date);
        }
    }

    public static Date formatDate2Date2(Date date) throws ParseException {
        if (date == null) {
            return null;
        } else {
            return sdf2.parse(sdf2.format(date));
        }
    }

    public static String formatDate4Str4(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf4.format(date);
        }
    }

    public static String formatDate5Str5(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf5.format(date);
        }
    }

    public static String formatDate6Str6(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf6.format(date);
        }
    }

    public static String formatDate7Str7(Date date) {
        if (date == null) {
            return "";
        } else {
            return sdf7.format(date);
        }
    }

    public static Date formatStr2Date(String dateStr) throws ParseException {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        } else {
            return sdf.parse(dateStr);
        }
    }

    public static Date formatStr4Date(String dateStr) throws ParseException {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        } else {
            return sdf4.parse(dateStr);
        }
    }

    public static Date formatStr3Date(String dateStr) throws ParseException {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        } else {
            return sdf3.parse(dateStr);
        }
    }

    public static Date formateSdf3DateToDate(Date date) throws ParseException {
        if (date == null) {
            return null;
        } else {
            return sdf3.parse(sdf3.format(date));
        }
    }

    public static Date formateSdfDateToDate(Date date) throws ParseException {
        if (date == null) {
            return null;
        } else {
            return sdf.parse(sdf.format(date));
        }
    }

    public static Date formatStr2Date2(String dateStr) throws ParseException {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        } else {
            return sdf2.parse(dateStr);
        }
    }

    public static Date formatStr5Date5(String dateStr) throws ParseException {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        } else {
            return sdf5.parse(dateStr);
        }
    }

    public static String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("#,##0.00");
        return df.format(value);
    }

    public static String formatInteger(Integer value) {
        if (value == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("#,##0");
        return df.format(value);
    }

    public static String formatString(Object o) {
        String ret = "";
        if (o == null) {
            return ret;
        } else {
            return o.toString();
        }
    }

    public static Integer[] changeStringToInteger(String arg) {
        String[] str = arg.split(",");
        Integer[] integer = new Integer[str.length];
        try {
            for (int i = 0; i < str.length; i++) {
                integer[i] = new Integer(str[i]);
            }
        } catch (Exception e) {
        }
        return integer;
    }

    public static String filter(ResultSet resultSet, int index) {
        String value = null;
        try {
            value = resultSet.getString(index);
        } catch (Throwable e) {
            return value;
        }
        return filter(value);
    }

    public static String filter(String input) {
        if (input == null) {
            return input;
        }
        StringBuffer filtered = new StringBuffer(input.length());
        char c;
        for (int i = 0; i <= input.length() - 1; i++) {
            c = input.charAt(i);
            switch(c) {
                case '\t':
                    break;
                case '\r':
                    break;
                case '\n':
                    break;
                default:
                    filtered.append(c);
            }
        }
        return (filtered.toString());
    }

    public static void main(String[] args) {
        String testString = "中文\n\rcxzc\n玩具";
        System.out.println("input=>" + filter(null));
    }
}

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
package com.qlangtech.tis.indexbuilder.doc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DateToLongProcessor {

    public static final Logger logger = LoggerFactory.getLogger(DateToLongProcessor.class);

    private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * @uml.property  name="formatFields"
     */
    String formatFields;

    List<String> formatFieldsList = new ArrayList<String>();

    /**
     * @return
     * @uml.property  name="formatFields"
     */
    public String getFormatFields() {
        return formatFields;
    }

    /**
     * @param formatFields
     * @uml.property  name="formatFields"
     */
    public synchronized void setFormatFields(String formatFields) {
        this.formatFields = formatFields;
        String[] fields = formatFields.split(",");
        for (String field : fields) {
            if (!formatFieldsList.contains(field))
                formatFieldsList.add(field);
        }
    }

    // @Override
    // public ResultCode process(Map<String, String> map)throws DataProcessException{
    // // TODO Auto-generated method stub
    // String currentField = null;
    // //SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // try
    // {
    // for(String field:formatFieldsList)
    // {
    // currentField = field;
    // String dataStr = map.get(field);
    // long dataLong = df.get().parse(dataStr).getTime();
    // map.put(field, String.valueOf(dataLong));
    // }
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // throw new DataProcessException("日期转换出错,field:"+currentField+",value:"+map.get(currentField),e);
    // }
    // /*String beginDate = map.get("begin_date");
    // String endDate = map.get("end_date");
    // try {
    // long lbeginDate = sdf.parse(beginDate).getTime();
    // long lendDate = sdf.parse(endDate).getTime();
    // map.put("begin_date", String.valueOf(lbeginDate));
    // map.put("end_date", String.valueOf(lendDate));
    // } catch (ParseException e) {
    // // TODO Auto-generated catch block
    // throw new DataProcessException("日期转换出错：begin_date:"+beginDate+"end_date:"+endDate+","+e);
    // }*/
    // return ResultCode.SUC;
    // }
    public static void main(String[] args) {
        String beginDate = "2012-08-23 09:34:54";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = sdf.parse(beginDate);
            long lbeginDate = date.getTime();
            System.out.println(lbeginDate);
            long l = Long.parseLong("1312689140039");
            date = new Date(l);
            System.out.println(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /* (non-Javadoc)
	 * @see com.taobao.terminator.common.data.processor.DataProcessor#getDesc()
	 */
    // @Override
    // public String getDesc() {
    // // TODO Auto-generated method stub
    // return null;
    // }
}

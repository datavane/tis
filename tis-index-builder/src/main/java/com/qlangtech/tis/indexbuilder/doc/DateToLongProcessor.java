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
package com.qlangtech.tis.indexbuilder.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
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
}

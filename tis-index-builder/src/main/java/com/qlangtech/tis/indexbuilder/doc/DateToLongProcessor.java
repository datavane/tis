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

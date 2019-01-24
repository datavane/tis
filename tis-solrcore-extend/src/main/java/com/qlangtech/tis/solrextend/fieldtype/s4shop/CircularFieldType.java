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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import org.apache.commons.lang.StringUtils;
import org.locationtech.spatial4j.distance.DistanceUtils;

/*
 * 空间搜索 圆形区域定义
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CircularFieldType extends BaseShapeFieldType {

    private static final String CIRCULAR_PREFIX = "BUFFER(POINT(";

    public static void main(String[] args) {
        double radius = 3000;
        System.out.println(DistanceUtils.dist2Degrees(((double) radius) / 1000d, DistanceUtils.EARTH_MEAN_RADIUS_KM));
    }

    @Override
    protected StringBuffer buildShapLiteria(Object val) {
        // delivery_range ：“longitude，latitude，半径” 要这样的格式
        String[] c = StringUtils.split(String.valueOf(val), ",");
        if (c.length != 3) {
            return null;
        }
        // "BUFFER(POINT(120.1596 30.2447)," + degree + ")"
        int radius = Integer.parseInt(c[2]);
        float longit = Float.parseFloat(c[0]);
        float lat = Float.parseFloat(c[1]);
        if (!(longit > -180 && longit < 180) || !(lat > -90 && lat < 90) || (radius < 1)) {
            return null;
        }
        double degree = DistanceUtils.dist2Degrees(((double) radius) / 1000d, DistanceUtils.EARTH_MEAN_RADIUS_KM);
        StringBuffer circular = new StringBuffer(CIRCULAR_PREFIX);
        return circular.append(longit).append(" ").append(lat).append("),").append(degree).append(")");
    }

    @Override
    protected boolean isShapeLiteria(Object val) {
        return String.valueOf(val).startsWith(CIRCULAR_PREFIX);
    }
}

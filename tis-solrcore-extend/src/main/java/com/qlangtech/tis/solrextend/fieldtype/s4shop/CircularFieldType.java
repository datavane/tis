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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import org.apache.commons.lang.StringUtils;
import org.locationtech.spatial4j.distance.DistanceUtils;

/**
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

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

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
package com.qlangtech.tis.realtime.transfer;

import java.io.File;
import java.util.Map;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import com.alibaba.fastjson.JSON;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月8日 下午2:29:52
 */
public class TestDTO extends TestCase {

    public void testDserialize() throws Exception {
        String path = "D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\tis-realtime-transfer\\msgdemo.txt";
        DTO dto = JSON.parseObject(FileUtils.readFileToString(new File(path)), DTO.class);
        Assert.assertNotNull(dto);
        System.out.println(dto.getDbName());
        System.out.println("after:");
        for (Map.Entry<String, String> entry : dto.getAfter().entrySet()) {
            System.out.println("key:" + entry.getKey() + ",value:" + entry.getValue());
        }
    }
}

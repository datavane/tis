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
package com.qlangtech.tis.order.center;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AssembleConfig {

    private static final String Disble_Incr_Index_Monitor = "disable.incr.index.monitor";

    private static AssembleConfig instance;

    // disable this node index incr validate checking
    private boolean disble_Incr_Index_Monitor = false;

    // disable this node incr log collection
    private boolean enable_assemble_role = true;

    private AssembleConfig() throws Exception {
        String dataDir = System.getProperty("data.dir");
        if (StringUtils.isEmpty(dataDir)) {
            throw new IllegalStateException("sys prop 'data.dir' is null");
        }
        File f = new File(dataDir, "assemble.properties");
        if (f.exists()) {
            try (InputStream input = FileUtils.openInputStream(f)) {
                Properties prop = new Properties();
                prop.load(input);
                if ("true".equalsIgnoreCase(prop.getProperty("disable.assemble.role"))) {
                    enable_assemble_role = false;
                }
                if ("true".equalsIgnoreCase(prop.getProperty(Disble_Incr_Index_Monitor))) {
                    disble_Incr_Index_Monitor = true;
                }
            }
        }
    }

    private static AssembleConfig getInstance() {
        try {
            if (instance == null) {
                synchronized (AssembleConfig.class) {
                    if (instance == null) {
                        instance = new AssembleConfig();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * 是否不要監控部分增量索引是否正常監控
     *
     * @return
     * @throws IOException
     */
    public static boolean isDisbleIncrIndexMonitor() throws IOException {
        return getInstance().disble_Incr_Index_Monitor;
    }

    /**
     * 需要启动去抢夺assemble的锁吗？
     *
     * @return
     * @throws IOException
     */
    public static boolean shallRobTheAssembleRoleLock() throws IOException {
        return getInstance().enable_assemble_role;
    // String dataDir = System.getProperty("data.dir");
    // if (StringUtils.isEmpty(dataDir)) {
    // throw new IllegalStateException("sys prop 'data.dir' is null");
    // }
    // File f = new File(dataDir, "assemble.properties");
    // if (f.exists()) {
    // try (InputStream input = FileUtils.openInputStream(f)) {
    // Properties prop = new Properties();
    // prop.load(input);
    // if ("true".equalsIgnoreCase(prop.getProperty("disable.assemble.role"))) {
    // return false;
    // }
    // }
    // }
    // 
    // return true;
    }
}

/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis;

import ch.qos.logback.classic.util.ContextInitializer;
import com.qlangtech.tis.web.start.TisApp;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-27 11:48
 */
public abstract class BaseTestCase extends TestCase {

    static {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback-assemble.xml");
        try {
            File logDir = new File("/tmp/logs/tis");
            File assembleLogdir = new File(logDir, "assemble");
            FileUtils.forceMkdir(logDir);
            FileUtils.cleanDirectory(logDir);
            System.setProperty("log.dir", logDir.getAbsolutePath());
            System.setProperty(TisApp.KEY_ASSEMBLE_TASK_DIR, assembleLogdir.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

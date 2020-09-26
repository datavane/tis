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
package com.qlangtech.tis.web.start;

import junit.framework.TestCase;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-04 11:44
 */
public class TestTisApp extends TestCase {

    public void testLaunchAssemble() throws Exception {
        // 
        // TisApp.setWebRootDir(new File("/Users/mozhenghua/j2ee_solution/project/tis-saturn2/tmp"));
        // TisApp.setWebRootDir(new File("/Users/mozhenghua/j2ee_solution/project/tis-saturn2/tmp"));
        System.setProperty(TisApp.KEY_WEB_ROOT_DIR, "../tmp");
        String[] args = new String[0];
        TisApp.main(args);
    }

    public void testGetResources() throws Exception {
        List<URL> urls = new ArrayList<>();
        File dir = new File("target/dependency");
        for (String c : dir.list()) {
            urls.add((new File(dir, c)).toURI().toURL());
        }
        System.out.println("urls size:" + urls.size());
        URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
        Enumeration<URL> resources = cl.getResources("");
        while (resources.hasMoreElements()) {
            System.out.println(resources.nextElement());
        }
    }
}

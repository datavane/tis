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

import com.qlangtech.tis.web.start.TisApp;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-18 11:44
 */
public class StartTISWeb extends TestCase {

    public void testStart() throws Exception {
        TisApp app = new TisApp("/collect", 8080, (context) -> {
            context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            context.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
        });
        System.out.println("start");
        app.start(new String[0]);
    }
}

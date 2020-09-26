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
import com.qlangtech.tis.web.start.TisApp;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-05 19:53
 */
public class ConsoleStart {

    public static void main(String[] args) throws Exception {
        TisApp app = new TisApp("/tjs", 8080, (context) -> {
            context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            context.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
        });
        System.out.println("start");
        app.start(args);
    }
}

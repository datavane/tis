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
package scala.tools.scala_maven_executions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Use a file and reflection to start a main class with arguments define in a
 * file. This class should run without other dependencies than jre. This class
 * is used as a workaround to the windows command line size limitation.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MainWithArgsInFile {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            String mainClassName = args[0];
            List<String> argsFromFile = new ArrayList<>();
            if (args.length > 0) {
                argsFromFile = MainHelper.readArgFile(new File(args[1]));
            }
            MainHelper.runMain(mainClassName, argsFromFile, null);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-10000);
        }
    }
}

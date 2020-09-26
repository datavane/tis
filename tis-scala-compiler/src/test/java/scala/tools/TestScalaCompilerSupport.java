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
package scala.tools;

import com.qlangtech.tis.sql.parser.DBNode;
import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import scala.tools.scala_maven_executions.LogProcessorUtils;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestScalaCompilerSupport extends TestCase {

    public void testStreamCodeCompile() throws Exception {
        // 测试增量脚本编译
        String collection = "search4totalpay";
        long timestamp = 20190820171040l;
        File sourceRoot = StreamContextConstant.getStreamScriptRootDir(collection, timestamp);
        System.out.println("source root:" + sourceRoot);
        List<DBNode> dbsMeta = null;
        try (InputStream input = FileUtils.openInputStream(new File(Config.getMetaCfgDir(), StreamContextConstant.getDbDependencyConfigFilePath(collection, timestamp)))) {
            dbsMeta = DBNode.load(input);
        }
        assertNotNull(dbsMeta);
        // File sourceRoot = null;
        LogProcessorUtils.LoggerListener loggerListener = new LogProcessorUtils.LoggerListener() {

            @Override
            public void receiveLog(LogProcessorUtils.Level level, String line) {
                System.out.println("=========================" + level + "," + line);
            }
        };
        assertFalse("compile must be success", streamScriptCompile(sourceRoot, Sets.newHashSet(dbsMeta), loggerListener));
    }

    private boolean streamScriptCompile(File sourceRoot, Set<DBNode> dependencyDBNodes, LogProcessorUtils.LoggerListener loggerListener) throws Exception {
        Set<String> dbDependenciesClasspath = DBNode.appendDBDependenciesClasspath(dependencyDBNodes);
        return ScalaCompilerSupport.streamScriptCompile(sourceRoot, dbDependenciesClasspath, loggerListener);
    }
}

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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class LogProcessorUtils {

    public enum Level {

        ERROR, WARNING, INFO
    }

    public interface LoggerListener {

        void receiveLog(Level level, String line);
    }

    public static class LevelState {

        public Level level = Level.INFO;

        public String untilContains = null;
    }

    public static LevelState levelStateOf(String line, LevelState previous) {
        LevelState back = new LevelState();
        String lineLowerCase = line.toLowerCase();
        if (lineLowerCase.contains("error")) {
            back.level = Level.ERROR;
            if (lineLowerCase.contains(".scala")) {
                back.untilContains = "^";
            }
        } else if (lineLowerCase.contains("warn")) {
            back.level = Level.WARNING;
            if (lineLowerCase.contains(".scala")) {
                back.untilContains = "^";
            }
        } else if (previous.untilContains != null) {
            if (!lineLowerCase.contains(previous.untilContains)) {
                back = previous;
            } else {
                back.level = previous.level;
                back.untilContains = null;
            }
        }
        return back;
    }
}

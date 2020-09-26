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

/**
 * This interface is used to create a call on a main method of a java class.
 * The important implementations are JavaCommand and ReflectionJavaCaller
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface JavaMainCaller {

    /**
     * Adds a JVM arg. Note: This is not available for in-process "forks"
     */
    void addJvmArgs(String... args);

    /**
     * Adds arguments for the process
     */
    void addArgs(String... args);

    /**
     * Adds option (basically two arguments)
     */
    void addOption(String key, String value);

    /**
     * Adds an option (key-file pair). This will pull the absolute path of the file
     */
    void addOption(String key, File value);

    /**
     * Adds the key if the value is true
     */
    void addOption(String key, boolean value);

    /**
     * request run to be redirected to maven/requester logger
     */
    void redirectToLog();

    // TODO: avoid to have several Thread to pipe stream
    // TODO: add support to inject startup command and shutdown command (on :quit)
    void run(boolean displayCmd) throws Exception;

    /**
     * Runs the JavaMain with all the built up arguments/options
     */
    boolean run(boolean displayCmd, boolean throwFailure) throws Exception;
    // /**
    // * run the command without stream redirection nor waiting for exit
    // *
    // * @param displayCmd
    // * @return the spawn Process (or null if no process was spawned)
    // * @throws Exception
    // */
    // SpawnMonitor spawn(boolean displayCmd) throws Exception;
}

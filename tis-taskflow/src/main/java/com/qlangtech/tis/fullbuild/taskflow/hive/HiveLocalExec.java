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
package com.qlangtech.tis.fullbuild.taskflow.hive;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveLocalExec {
    /**
     * @param args
     */
    // public static void main(String[] args) throws Exception {
    // // String line = "AcroRd32.exe /p /h " + file.getAbsolutePath();
    // // CommandLine cmdLine = new CommandLine("hive");
    // // cmdLine.addArgument("--database tis");
    // // cmdLine.addArgument("-e");
    // // cmdLine.addArgument(
    // // "select count(1) from totalpay where pt='20160224001002';",
    // // true);
    // 
    // System.out.println("start===================");
    // 
    // // CommandLine cmdLine = new CommandLine("/bin/sh");
    // // cmdLine.addArgument("./dumpcenter-daily.sh");
    // 
    // CommandLine cmdLine = new CommandLine("hive");
    // cmdLine.addArgument("--database");
    // cmdLine.addArgument("tis");
    // cmdLine.addArgument("-e");
    // cmdLine.addArgument(
    // "select count(1) from instance;\n select count(1) from totalpay;",
    // true);
    // 
    // System.out.println("getExecutable:" + cmdLine.getExecutable());
    // System.out.println(cmdLine.getArguments());
    // System.out.println("==============");
    // DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    // 
    // ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
    // DefaultExecutor executor = new DefaultExecutor();
    // executor.setWorkingDirectory(new File("/home/baisui/tis"));
    // 
    // executor.setStreamHandler(new PumpStreamHandler(System.out));
    // executor.setExitValue(1);
    // executor.setWatchdog(watchdog);
    // executor.execute(cmdLine, resultHandler);
    // 
    // // 等待5个小时
    // resultHandler.waitFor(5 * 60 * 60 * 1000);
    // System.out.println("exec over===================");
    // 
    // System.out.println("exitCode:" + resultHandler.getExitValue());
    // if (resultHandler.getException() != null) {
    // resultHandler.getException().printStackTrace();
    // }
    // }
}

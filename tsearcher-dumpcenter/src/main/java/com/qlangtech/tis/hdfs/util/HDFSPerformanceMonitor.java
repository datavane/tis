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
package com.qlangtech.tis.hdfs.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * @description
 * @since 2011-8-5 01:15:20
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HDFSPerformanceMonitor {

    protected static Log log = LogFactory.getLog(HDFSPerformanceMonitor.class);

    public static void printWriteResult(int writeThreadNums, long startTime) throws Exception {
        int resultCount = 0;
        int successCount = 0;
        int errorCount = 0;
        int responseTime = 0;
        int below10 = 0;
        int below100 = 0;
        int below1000 = 0;
        int above1000 = 0;
        int[] successResults = caculate("writer.success.results");
        int[] errorResults = caculate("writer.error.results");
        successCount = successResults[0];
        errorCount = errorResults[0];
        resultCount = successCount + errorCount;
        responseTime = successResults[1] + errorResults[1];
        below10 = successResults[2] + errorResults[2];
        below100 = successResults[3] + errorResults[3];
        below1000 = successResults[4] + errorResults[4];
        above1000 = successResults[5] + errorResults[5];
        // 
        int avgRT = responseTime / resultCount;
        StringBuilder bufferBuilder = new StringBuilder();
        bufferBuilder.append("==================Write Result==================\r\n");
        if (avgRT > 0) {
            log.warn(" Write TPS: " + (1000 / avgRT) * writeThreadNums);
        }
        bufferBuilder.append(" Success Write Result: (" + successCount + "/" + resultCount + ")\r\n");
        bufferBuilder.append(" Failed Ratio: " + (100 * errorCount / resultCount) + "%\r\n");
        bufferBuilder.append(" Avg Response Time: " + avgRT + " ms \r\n");
        bufferBuilder.append(" 0~10ms: " + below10 + "\r\n");
        bufferBuilder.append(" 10~100ms: " + below100 + "\r\n");
        bufferBuilder.append(" 100~1000ms: " + below1000 + "\r\n");
        bufferBuilder.append(" 1000ms+: " + above1000 + "\r\n");
        log.warn(bufferBuilder.toString());
        analyzeResults("writer.success.results", "writer.success.csv", startTime);
        analyzeResults("writer.error.results", "writer.error.csv", startTime);
    }

    private static void printReadResult(int readThreadNums, long startTime) {
        int resultCount = 0;
        int successCount = 0;
        int errorCount = 0;
        int responseTime = 0;
        int below10 = 0;
        int below100 = 0;
        int below1000 = 0;
        int above1000 = 0;
        try {
            int[] successResults = caculate("reader.success.results");
            int[] failResults = caculate("reader.fail.results");
            int[] errorResults = caculate("reader.error.results");
            successCount = successResults[0];
            int failCount = failResults[0];
            errorCount = errorResults[0];
            resultCount = successCount + errorCount + failCount;
            responseTime = successResults[1] + errorResults[1] + failResults[1];
            below10 = successResults[2] + errorResults[2] + failResults[2];
            below100 = successResults[3] + errorResults[3] + failResults[3];
            below1000 = successResults[4] + errorResults[4] + failResults[4];
            above1000 = successResults[5] + errorResults[5] + failResults[5];
            int avgRT = responseTime / resultCount;
            System.out.println("==================Read Result==================");
            if (avgRT > 0) {
                System.out.println(" Read TPS: " + (1000 / avgRT) * readThreadNums);
            }
            System.out.println(" Success Read Result: (" + successCount + "/" + resultCount + ")");
            System.out.println(" Failed Ratio: " + (100 * failCount / resultCount) + "%");
            System.out.println(" Error Ratio: " + (100 * errorCount / resultCount) + "%");
            System.out.println(" Avg Response Time: " + avgRT + " ms");
            System.out.println(" 0~10ms: " + below10);
            System.out.println(" 10~100ms: " + below100);
            System.out.println(" 100~1000ms: " + below1000);
            System.out.println(" 1000ms+: " + above1000);
            analyzeResults("reader.success.results", "reader.success.csv", startTime);
            analyzeResults("reader.fail.results", "reader.fail.csv", startTime);
            analyzeResults("reader.error.results", "reader.error.csv", startTime);
        } catch (Exception e) {
            ;
        }
    }

    /**
     * 将结果分析成CSV格式的文件,便于图形化展示
     * @param inputFileName
     * @param outputFileName
     * @param startTime
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void analyzeResults(String inputFileName, String outputFileName, long startTime) throws FileNotFoundException, IOException {
        long beginTime = startTime;
        BufferedReader fileReader = new BufferedReader(new FileReader(new File(inputFileName)));
        File outputFile = new File(outputFileName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));
        String line = null;
        // key: 执行时的秒数 value: 响应时间的集合
        Map<String, List<Integer>> results = new HashMap<String, List<Integer>>();
        while ((line = fileReader.readLine()) != null) {
            String[] lineInfos = line.split(",");
            long occurTime = Long.parseLong(lineInfos[0]);
            int rt = Integer.parseInt(lineInfos[1]);
            String range = String.valueOf(Integer.parseInt(String.valueOf((occurTime - beginTime) / 1000)) + 1);
            if (results.containsKey(range)) {
                results.get(range).add(rt);
            } else {
                List<Integer> rts = new ArrayList<Integer>();
                rts.add(rt);
                results.put(range, rts);
            }
        }
        fileReader.close();
        // 排序Map Key，输出结果
        String[] keys = results.keySet().toArray(new String[results.size()]);
        Arrays.sort(keys, new Comparator<String>() {

            public int compare(String current, String next) {
                return (Integer.parseInt(current) > Integer.parseInt(next)) ? 1 : -1;
            }
        });
        for (String key : keys) {
            // 次数
            int count = results.get(key).size();
            // 响应时间总和
            int rt = 0;
            List<Integer> rts = results.get(key);
            for (Integer perRT : rts) {
                rt += perRT;
            }
            // 平均响应时间
            int avgRT = 0;
            if (count > 0)
                avgRT = rt / count;
            fileWriter.write(key + "," + count + "," + rt + "," + avgRT + "\r\n");
        }
        fileWriter.close();
    }

    private static int[] caculate(String fileName) throws Exception {
        int resultCount = 0;
        int responseTime = 0;
        int below10 = 0;
        int below100 = 0;
        int below1000 = 0;
        int above1000 = 0;
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
        while ((line = reader.readLine()) != null) {
            if (line.indexOf(",") != -1) {
                String[] datas = line.split(",");
                // 总的导入次数
                resultCount++;
                // 消耗时间
                int currRT = Integer.parseInt(datas[1]);
                // 总耗时
                responseTime += currRT;
                if (currRT >= 0 && currRT < 10) {
                    // 10毫秒内
                    below10++;
                } else if (currRT >= 10 && currRT < 100) {
                    // 100毫秒内
                    below100++;
                } else if (currRT >= 100 && currRT < 1000) {
                    // 100-1000毫秒内
                    below1000++;
                } else {
                    // 100-1000毫秒内
                    above1000++;
                }
            }
        }
        reader.close();
        return new int[] { resultCount, responseTime, below10, below100, below1000, above1000 };
    }

    public static void main(String[] args) {
    }
}

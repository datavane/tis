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
package com.qlangtech.tis.common.config;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HostInfoParser {

    /**
     * 192.168.222.32:8080#1:1:1:1
     * reader : writer : merger : indexWriter
     *
     * @param hostInfo
     * @return
     */
    public static HostConfig toHostConfig(String hostInfo) {
        HostConfig hostConfig = new HostConfig();
        String[] iprole = hostInfo.split("#");
        if (iprole.length != 2) {
            throw new IllegalArgumentException("The hostInfo must be like this [192.168.222.32:8080#1:1:1]");
        }
        String[] ippart = iprole[0].split(":");
        String[] rolepart = iprole[1].split(":");
        if (ippart.length != 2 || rolepart.length != 3) {
            throw new IllegalArgumentException("The hostInfo must be like this [192.168.222.32:8080#1:1:1]");
        }
        hostConfig.setIp(ippart[0]);
        hostConfig.setPort(ippart[1]);
        hostConfig.setReader(rolepart[0].equals("1"));
        hostConfig.setWriter(rolepart[1].equals("1"));
        hostConfig.setMerger(rolepart[2].equals("1"));
        return hostConfig;
    }

    /**
     * 192.168.222.32:8080#1:1:1:1
     * reader : writer : merger
     *
     * @param hostInfo
     * @return
     */
    public static String toHostInfo(HostConfig hostConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(hostConfig.getIp());
        sb.append(":");
        sb.append(hostConfig.getPort());
        sb.append("#");
        sb.append(hostConfig.isReader() ? "1" : "0");
        sb.append(":");
        sb.append(hostConfig.isWriter() ? "1" : "0");
        sb.append(":");
        sb.append(hostConfig.isMerger() ? "1" : "0");
        return sb.toString();
    }
}

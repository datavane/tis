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

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HostConfig extends Properties {

    private static final long serialVersionUID = -4586146581307582203L;

    /**
     * @uml.property name="ip"
     */
    private String ip;

    public static final String DEFAULT_IS_READER = "true";

    public static final String DEFAULT_IS_MERGER = "true";

    public static final String DEFAULT_IS_INDEXWRITER = "true";

    public static final String DEFAULT_IS_WRITER = "false";

    public static final String DEFAULT_PORT = "false";

    public HostConfig() {
    }

    private static final Pattern NODE_PATTERN = Pattern.compile("(.+?):(\\d{4,5})");

    public static void main(String[] arg) {
        Matcher m = NODE_PATTERN.matcher("10.1.7.41:8983_solr");
        if (m.find()) {
            System.out.println(m.group(1));
            System.out.println(m.group(2));
        } else {
            System.out.println("has not find");
        }
    }

    /**
     * @param hostInfo
     *            格式为 192.168.222.32:8080#1:1:1 ==> ip:port #
     *            reader:writer:merger
     */
    public HostConfig(String hostInfo) {
        Matcher matcher = NODE_PATTERN.matcher(hostInfo);
        if (matcher.find()) {
            this.setIp(matcher.group(1));
            this.setPort(matcher.group(2));
            return;
        }
        throw new IllegalStateException("node name:" + hostInfo + " is not illegal");
    // 
    // String[] iprole = hostInfo.split("#");
    // if (iprole.length != 2) {
    // throw new IllegalArgumentException(
    // "The hostInfo must be like this [192.168.222.32:8080#1:1:1]");
    // }
    // String[] ippart = iprole[0].split(":");
    // String[] rolepart = iprole[1].split(":");
    // 
    // if (ippart.length != 2 || rolepart.length != 3) {
    // throw new IllegalArgumentException(
    // "The hostInfo must be like this [192.168.222.32:8080#1:1:1]");
    // }
    // 
    // this.setReader(rolepart[0].equals("1"));
    // this.setWriter(rolepart[1].equals("1"));
    // this.setMerger(rolepart[2].equals("1"));
    }

    /**
     * @param ip
     * @uml.property name="ip"
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return
     * @uml.property name="ip"
     */
    public String getIp() {
        return this.ip;
    }

    public boolean isWriter() {
        return Boolean.valueOf(this.getProperty("isWriter", DEFAULT_IS_WRITER));
    }

    public boolean isReader() {
        return Boolean.valueOf(this.getProperty("isReader", DEFAULT_IS_READER));
    }

    public boolean isMerger() {
        return Boolean.valueOf(this.getProperty("isMerger", DEFAULT_IS_MERGER));
    }

    public void setMerger(boolean isMerger) {
        this.setProperty("isMerger", String.valueOf(isMerger));
    }

    public void setReader(boolean isReader) {
        this.setProperty("isReader", String.valueOf(isReader));
    }

    public void setWriter(boolean isWriter) {
        this.setProperty("isWriter", String.valueOf(isWriter));
    }

    public String getPort() {
        return this.getProperty("port", DEFAULT_PORT);
    }

    public void setPort(String port) {
        this.setProperty("port", port);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("      ").append(this.getIp()).append(":").append(this.getPort());
        if (this.isMerger()) {
            sb.append(" merger ");
        }
        if (this.isReader()) {
            sb.append(" reader ");
        }
        if (this.isWriter()) {
            sb.append(" writer ");
        }
        return sb.toString();
    }
}

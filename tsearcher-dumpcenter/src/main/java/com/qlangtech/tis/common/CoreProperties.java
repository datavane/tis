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
package com.qlangtech.tis.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * 对应配置文件  core.properties,每个core都有一个相应的配置文件，用于说明该机器下的该core的角色
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CoreProperties extends Properties {

    private static final long serialVersionUID = -4020028009909240962L;

    public CoreProperties() {
        super();
    }

    public CoreProperties(Properties defaults) {
        super(defaults);
    }

    public CoreProperties(File proFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(proFile);
        this.load(inputStream);
    }

    public CoreProperties(InputStream inputStream) throws IOException {
        this.load(inputStream);
    }

    public boolean isMerger() {
        return Boolean.valueOf(this.getProperty("isMerger", "true").trim());
    }

    public boolean isWriter() {
        return Boolean.valueOf(this.getProperty("isWriter", "false").trim());
    }

    public boolean isReader() {
        return Boolean.valueOf(this.getProperty("isReader", "true").trim());
    }

    public void setMerger(boolean isMerger) {
        this.setProperty("isMerger", isMerger ? "true" : "false");
    }

    public void setReader(boolean isReader) {
        this.setProperty("isReader", isReader ? "true" : "false");
    }

    public void setWriter(boolean isWriter) {
        this.setProperty("isWriter", isWriter ? "true" : "false");
    }

    public void setIP(String ip) {
        this.setProperty("ip", ip);
    }

    public String getIP() {
        return this.getProperty("ip");
    }

    public void setPort(String port) {
        this.setProperty("port", port);
    }

    public String getPort() {
        return this.getProperty("port");
    }
}

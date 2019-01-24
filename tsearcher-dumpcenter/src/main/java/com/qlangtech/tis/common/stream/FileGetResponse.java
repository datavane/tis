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
package com.qlangtech.tis.common.stream;

import java.io.Serializable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FileGetResponse implements Serializable {

    private static final long serialVersionUID = 1659196824016862319L;

    public static int SUCCESS = 0;

    public static int FILE_NOT_EXIST = 1;

    public static int FILE_TYPE_NOT_EXIST = 2;

    public static int FILE_EXCEPTION = 3;

    /**
     * @uml.property  name="type"
     */
    private String type = null;

    /**
     * @uml.property  name="name"
     */
    private String name = null;

    /**
     * @uml.property  name="length"
     */
    private long length = 0;

    /**
     * @uml.property  name="code"
     */
    private int code = 0;

    public FileGetResponse(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * @return
     * @uml.property  name="code"
     */
    public int getCode() {
        return code;
    }

    /**
     * @return
     * @uml.property  name="length"
     */
    public long getLength() {
        return length;
    }

    /**
     * @return
     * @uml.property  name="name"
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     * @uml.property  name="type"
     */
    public String getType() {
        return type;
    }

    /**
     * @param code
     * @uml.property  name="code"
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @param length
     * @uml.property  name="length"
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @param name
     * @uml.property  name="name"
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param type
     * @uml.property  name="type"
     */
    public void setType(String type) {
        this.type = type;
    }
}

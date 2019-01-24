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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServiceType {

    public static final String ROLE_MERGER = "merger";

    public static final String ROLE_READER = "readr";

    public static final String ROLE_WRITER = "writer";

    public static final String ROLE_INDEX_WRITER = "indexWriter";

    /**
     * @uml.property  name="code"
     */
    private int code;

    /**
     * @uml.property  name="type"
     */
    private String type;

    private ServiceType() {
    }

    private ServiceType(int code, String role) {
        this.code = code;
        this.type = role;
    }

    /**
     * @uml.property  name="reader"
     * @uml.associationEnd
     */
    public static final ServiceType reader = new ServiceType(1, ROLE_READER);

    /**
     * @uml.property  name="writer"
     * @uml.associationEnd
     */
    public static final ServiceType writer = new ServiceType(2, ROLE_WRITER);

    /**
     * @uml.property  name="merger"
     * @uml.associationEnd
     */
    public static final ServiceType merger = new ServiceType(3, ROLE_MERGER);

    /**
     * @return
     * @uml.property  name="type"
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return
     * @uml.property  name="code"
     */
    public int getCode() {
        return this.code;
    }

    public String toString() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceType other = (ServiceType) obj;
        if (code != other.code)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}

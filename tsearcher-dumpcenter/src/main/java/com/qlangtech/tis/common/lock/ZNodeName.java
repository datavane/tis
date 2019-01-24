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
package com.qlangtech.tis.common.lock;

import org.apache.log4j.Logger;

/*
 * Represents an ephemeral znode name which has an ordered sequence number and can be sorted in order
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
class ZNodeName implements Comparable<ZNodeName> {

    /**
     * @uml.property  name="name"
     */
    private final String name;

    /**
     * @uml.property  name="prefix"
     */
    private String prefix;

    private int sequence = -1;

    private static final Logger LOG = Logger.getLogger(ZNodeName.class);

    public ZNodeName(String name) {
        if (name == null) {
            throw new NullPointerException("id cannot be null");
        }
        this.name = name;
        this.prefix = name;
        int idx = name.lastIndexOf('-');
        if (idx >= 0) {
            this.prefix = name.substring(0, idx);
            try {
                this.sequence = Integer.parseInt(name.substring(idx + 1));
            // If an exception occurred we misdetected a sequence suffix,
            // so return -1.
            } catch (NumberFormatException e) {
                LOG.info("Number format exception for " + idx, e);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.info("Array out of bounds for " + idx, e);
            }
        }
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ZNodeName sequence = (ZNodeName) o;
        if (!name.equals(sequence.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + 37;
    }

    public int compareTo(ZNodeName that) {
        int answer = this.prefix.compareTo(that.prefix);
        if (answer == 0) {
            int s1 = this.sequence;
            int s2 = that.sequence;
            if (s1 == -1 && s2 == -1) {
                return this.name.compareTo(that.name);
            }
            answer = s1 == -1 ? 1 : s2 == -1 ? -1 : s1 - s2;
        }
        return answer;
    }

    /**
     * Returns the name of the znode
     * @uml.property  name="name"
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the sequence number
     */
    public int getZNodeName() {
        return sequence;
    }

    /**
     * Returns the text prefix before the sequence number
     * @uml.property  name="prefix"
     */
    public String getPrefix() {
        return prefix;
    }
}

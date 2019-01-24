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
package com.qlangtech.tis.pubhook.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JsonUtil {

    private static final XStream xstream = new XStream(new JsonHierarchicalStreamDriver());

    private static final XStream deserialableSstream = new XStream(new JettisonMappedXmlDriver());

    static {
        xstream.omitField(Object.class, "class");
    // xstream.addImplicitCollection(Owner.class, "list");
    }

    // 对象序列化工具
    private static final XStream serialToolkit = new XStream();

    private static final String ENCODE = "utf8";

    public static void toString(Object obj, OutputStream output) {
        xstream.toXML(obj, output);
    }

    /**
     * 将对象序列化成字符串，而且这个字符串是中间没有任何间隙这样便于序列化内容在http的post中传输
     *
     * @param o
     * @return
     */
    public static String serialObj(Object o) {
        return new String(Hex.encodeHex(serialToolkit.toXML(o).getBytes(Charset.forName(ENCODE))));
    }

    @SuppressWarnings("all")
    public static <T> T deserialObj(String text, Charset charset) {
        try {
            return (T) serialToolkit.fromXML(new String(Hex.decodeHex(text.toCharArray()), charset));
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(Object o) {
        return xstream.toXML(o);
    }

    @SuppressWarnings("all")
    public static <T> T fromStream(InputStream input) {
        return (T) deserialableSstream.fromXML(input);
    }

    public static String serialize(Object o) {
        return deserialableSstream.toXML(o);
    }

    public static void copy2writer(Object o, Writer writer) {
        xstream.toXML(o, writer);
    }

    static class Owner {

        private List<Object> list;

        public List<Object> getList() {
            return list;
        }

        public void setList(List<Object> list) {
            this.list = list;
        }
    }

    public static void main(String[] arg) {
    // System.out.println(deserialObj(JsonUtil.serialObj("我是一个兵 &*(&(")));
    // List<Object> lsit = new ArrayList<Object>();
    // 
    // lsit.add(new Object());
    // 
    // Owner owner = new Owner();
    // owner.setList(lsit);
    }
}

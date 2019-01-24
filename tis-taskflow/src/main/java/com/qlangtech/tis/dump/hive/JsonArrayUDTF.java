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
package com.qlangtech.tis.dump.hive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JsonArrayUDTF extends GenericUDTF {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    static {
        // Allows for unescaped ASCII control characters in JSON values
        JSON_FACTORY.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        JSON_FACTORY.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper(JSON_FACTORY);

    private static final JavaType MAP_TYPE = TypeFactory.arrayType(Map.class);

    public static void main(String[] args) throws Exception {
        Map<String, Object>[] arrays = (Map<String, Object>[]) MAPPER.readValue("[{a:1,b:2},]", MAP_TYPE);
        for (Map<String, Object> r : arrays) {
            for (Map.Entry<String, Object> entry : r.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            System.out.println("======================================");
        }
    }

    private Map<String, Object>[] arrays;

    @SuppressWarnings("all")
    @Override
    public StructObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        if (args.length != 1) {
            throw new UDFArgumentLengthException("UDTFSerial takes only one argument");
        }
        if (!args[0].getTypeName().equals("int")) {
            throw new UDFArgumentException("UDTFSerial only takes an integer as a parameter");
        }
        if (args[0].getCategory() != ObjectInspector.Category.PRIMITIVE || !args[0].getTypeName().equals(serdeConstants.STRING_TYPE_NAME)) {
            throw new UDFArgumentException("json_tuple()'s arguments have to be string type");
        }
        try {
            this.arrays = (Map<String, Object>[]) MAPPER.readValue(args[0].toString(), MAP_TYPE);
        } catch (Exception e) {
            throw new UDFArgumentException(e);
        }
        Set<String> keys = new HashSet<String>();
        for (Map<String, Object> r : arrays) {
            for (String key : r.keySet()) {
                keys.add(key.toLowerCase());
            }
        }
        ArrayList<String> fieldNames = new ArrayList<String>(keys.size());
        ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>(keys.size());
        for (String key : keys) {
            fieldNames.add(key);
            fieldOIs.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        }
        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }

    @Override
    public void process(Object[] args) throws HiveException {
    // this.forward(o);
    }

    @Override
    public void close() throws HiveException {
    }
}

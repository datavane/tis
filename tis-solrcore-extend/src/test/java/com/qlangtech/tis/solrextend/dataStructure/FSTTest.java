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
package com.qlangtech.tis.solrextend.dataStructure;

import junit.framework.TestCase;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 3/15/2017.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FSTTest extends TestCase {

    public void test() throws Exception {
        String[] inputValues = { "cat", "dog", "dogs" };
        long[] outputValues = { 5, 7, 12 };
        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        Builder<Long> builder = new Builder<>(FST.INPUT_TYPE.BYTE1, outputs);
        BytesRef scratchBytes;
        IntsRefBuilder scratchInts = new IntsRefBuilder();
        for (int i = 0; i < inputValues.length; i++) {
            scratchBytes = new BytesRef(inputValues[i]);
            builder.add(Util.toIntsRef(scratchBytes, scratchInts), outputValues[i]);
        }
        FST<Long> fst = builder.finish();
        Long value = Util.get(fst, new BytesRef("dog"));
        // 7
        System.out.println(value);
        System.out.println("------------------");
    // IntsRef key = Util.getByOutput(fst, 12);
    // BytesRefBuilder bytesRefBuilder = new BytesRefBuilder();
    // System.out.println(Util.toBytesRef(key, bytesRefBuilder).utf8ToString()); // dogs
    // System.out.println("------------------");
    // 
    // 
    // BytesRefFSTEnum<Long> iterator = new BytesRefFSTEnum<>(fst);
    // while (iterator.next() != null) {
    // BytesRefFSTEnum.InputOutput<Long> mapEntry = iterator.current();
    // System.out.println(mapEntry.input.utf8ToString());
    // System.out.println(mapEntry.output);
    // }
    // System.out.println("------------------");
    }
}

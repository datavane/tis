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
import java.io.File;
import java.net.URI;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestLocalCopy2Remote extends TestCase {

    public void testCopy() throws Exception {
        FileSystem fs = TISHdfsUtils.getFileSystem();
        File dir = new File("D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\oozie\\examples\\src\\main\\apps\\hive2");
        URI source = null;
        Path d = null;
        Path dest = new Path("hdfs://10.1.7.25:8020/user/oozie/hive2");
        for (String f : dir.list()) {
            source = (new File(dir, f)).toURI();
            d = new Path(dest, f);
            fs.copyFromLocalFile(new Path(source), d);
        // libs.add(d);
        // logger.info("local:" + source + " have been copy to hdfs");
        }
    }
}

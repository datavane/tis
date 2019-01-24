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
package com.qlangtech.tis.order.center;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTriggerFactory;
import com.qlangtech.tis.trigger.jst.impl.TriggerClassLoader;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RemoteBuildCenterUtils {

    public static final IRemoteJobTriggerFactory remoteJobTriggerFactory;

    public static final ExecutorService taskPool;

    private static final Logger logger = LoggerFactory.getLogger(RemoteBuildCenterUtils.class);

    static {
        try {
            File dir = new File(System.getProperty("dir-hdfs20", "/opt/data/jetty/dir-hdfs20"));
            if (!dir.exists() || !dir.isDirectory()) {
                throw new IllegalStateException("dir-hdfs20:" + dir.getAbsolutePath() + " is illegal.");
            }
            URL[] urls = new URL[dir.list().length];
            int i = 0;
            for (String f : dir.list()) {
                urls[i++] = ((new File(dir, f)).toURI().toURL());
            }
            final ClassLoader buildTriggerClassLoader = new TriggerClassLoader(urls, RemoteBuildCenterUtils.class.getClassLoader());
            Class<?> indexTriggerClass = buildTriggerClassLoader.loadClass("com.dfire.tis.fullbuild.indexbuild.impl.Hadoop020RemoteJobTriggerFactory");
            remoteJobTriggerFactory = (IRemoteJobTriggerFactory) indexTriggerClass.newInstance();
            taskPool = Executors.newCachedThreadPool(new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setContextClassLoader(buildTriggerClassLoader);
                    return thread;
                }
            });
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void main(String[] arg) {
    }
}

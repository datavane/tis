/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.manage.common.incr;

import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant.TISRes;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshot;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-03-13 16:16
 **/
public class UberJarUtil {
    private static final Logger logger = LoggerFactory.getLogger(UberJarUtil.class);

    public static File createStreamUberJar(TargetResName collection, long timestamp) throws Exception {
        File streamUberJar = null;
        Manifest manifest = null;
        try {
            streamUberJar = getStreamUberJarFile(collection).getFile();
            manifest = PluginAndCfgsSnapshot.createFlinkIncrJobManifestCfgAttrs(collection, timestamp);
        } catch (Exception e) {
            throw new RuntimeException("name:" + collection.getName() + ",timestamp:" + timestamp, e);
        }

        try (JarOutputStream jaroutput = new JarOutputStream(
                FileUtils.openOutputStream(streamUberJar, false)
                , Objects.requireNonNull(manifest, "manifest can not be null"))) {
            jaroutput.flush();
            return streamUberJar;
        }
    }

    public static TISRes getStreamUberJarFile(TargetResName collection) {
        TISRes streamScriptRootDir = StreamContextConstant.getStreamScriptRootDir(collection.getName(), false);
        final long timestamp = 0;
        String uberJarPath = streamScriptRootDir.getRelevantPath()
                + "/" + timestamp + "/" + StreamContextConstant.getIncrStreamJarName(collection.getName());
        File streamUberJar = StreamContextConstant.getIncrStreamJarFile(collection.getName(), 0);
        logger.info("streamUberJar path:{}", streamUberJar.getAbsolutePath());
        return new TISRes(streamUberJar, uberJarPath);
    }
}

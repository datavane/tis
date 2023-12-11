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

package com.qlangtech.tis.plugin;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/12/11
 */
public class PluginAndCfgsSnapshotUtils {

    public final static String KEY_PLUGIN_CFGS_METAS = "pluginCfgsMetas";

    public static void writeManifest2Jar(File manifestJar, Manifest manifestCfgAttrs) throws IOException {
        try (OutputStream outstream = FileUtils.openOutputStream(manifestJar, false)) {
            writeManifest2OutputStream(outstream, manifestCfgAttrs);
        }
    }


    public static void writeManifest2OutputStream(OutputStream outstream, Manifest manifestCfgAttrs) throws IOException {
        JarOutputStream jaroutput = new JarOutputStream(outstream, manifestCfgAttrs);
        jaroutput.putNextEntry(new ZipEntry(PluginAndCfgsSnapshot.getTaskEntryName()));
        jaroutput.flush();
    }
}

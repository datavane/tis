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

package com.qlangtech.tis.plugin.tdfs;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-08-04 08:03
 **/
public interface ITDFSSession extends AutoCloseable {

    String getRootPath();

    /**
     * @param @param  directoryPath
     * @param @return
     * @return boolean
     * @throws
     * @Title: isDirExist
     * @Description: 判断指定路径是否是目录
     */
    public abstract boolean isDirExist(String directoryPath);

    public void mkDirRecursive(String directoryPath);

    public abstract Set<String> getAllFilesInDir(String path, String fileName);

    /**
     * warn: 不支持文件夹删除, 比如 rm -rf
     */
    public abstract void deleteFiles(Set<String> filesToDelete);

    /**
     * @param @param  srcPaths 路径列表
     * @param @param  parentLevel 父目录的递归层数（首次为0）
     * @param @param  maxTraversalLevel 允许的最大递归层数
     * @param @return
     * @return HashSet<String>
     * @throws
     * @Title: getAllFiles
     * @Description: 获取指定路径列表下符合条件的所有文件的绝对路径
     */
    public HashSet<Res> getAllFiles(List<String> srcPaths, int parentLevel, int maxTraversalLevel);


    /**
     * @param @param  directoryPath
     * @param @param  parentLevel 父目录的递归层数（首次为0）
     * @param @param  maxTraversalLevel 允许的最大递归层数
     * @param @return
     * @return HashSet<String>
     * @throws
     * @Title: getListFiles
     * @Description: 递归获取指定路径下符合条件的所有文件绝对路径
     */
    public abstract HashSet<Res> getListFiles(String directoryPath, int parentLevel, int maxTraversalLevel);


    public default OutputStream getOutputStream(String filePath) {
        return this.getOutputStream(filePath, true);
    }

    public abstract OutputStream getOutputStream(String filePath, boolean append);

    /**
     * @param @param  filePath
     * @param @return
     * @return InputStream
     * @throws
     * @Title: getInputStream
     * @Description: 获取指定路径的输入流
     */
    public abstract InputStream getInputStream(String filePath);


    public class Res {
        public final String fullPath;
        public final String relevantPath;
        private static String PATH_SEPERATOR = "/";

        public static List<String> appendElement(List<String> relevantPaths, String fileName) {
            relevantPaths = Lists.newArrayList(relevantPaths);
            relevantPaths.add(StringUtils.remove(fileName, PATH_SEPERATOR));
            return relevantPaths;
        }

        public static String buildRelevantPath(List<String> relevantPaths, String fileName) {
            return appendElement(relevantPaths, fileName).stream().collect(Collectors.joining(PATH_SEPERATOR));
        }

        public Res(String fullPath, String relevantPath) {
            this.fullPath = fullPath;
            this.relevantPath = relevantPath;
        }
    }
}

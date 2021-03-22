/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.runtime.module.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.util.Memoizer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.commons.lang3.concurrent.Memoizer;


/**
 * 重新整理 项目中的jar包，可以使得整个Uber包可以做到最小
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-22 14:59
 */
public class SysInitializeArrangeJars {
  private static final List<String> subDirs = Lists.newArrayList("tis-assemble", "solr", "tjs", "tis-collect");
  static final Memoizer<String, List<File>> jars = new Memoizer<String, List<File>>() {
    @Override
    public List<File> compute(String key) {
      return Lists.newArrayList();
    }
  };

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalStateException("please set uberDir ");
    }
    File uberDir = new File(args[0]);
    if (!uberDir.exists()) {
      throw new IllegalStateException("uberDir is not exist:" + uberDir.getAbsolutePath());
    }
    File subModuleLibDir = null;
    for (String sbDir : subDirs) {
      subModuleLibDir = new File(uberDir, sbDir + "/lib");
      if (!subModuleLibDir.exists()) {
        throw new IllegalStateException("sub lib dir:" + subModuleLibDir.getAbsolutePath() + " is not exist");
      }
      for (String jarFileName : subModuleLibDir.list()) {
        jars.compute(jarFileName).add(new File(subModuleLibDir, jarFileName));
      }
    }
    File webStartDir = new File(uberDir, "web-start/lib");
    Set<String> existJarFiles = Sets.newHashSet(webStartDir.list());
    if (existJarFiles.size() < 1) {
      throw new IllegalStateException("webStartDir:" + webStartDir.getAbsolutePath() + " has any jar file");
    }

    for (Map.Entry<String, List<File>> subModuleJar : jars.getEntries()) {
      System.out.println("process file:" + subModuleJar.getKey());
      if (existJarFiles.contains(subModuleJar.getKey())) {
        subModuleJar.getValue().forEach((f) -> {
          forceDeleteOnExit(f);
        });
      } else {
        boolean first = true;
        for (File subJar : subModuleJar.getValue()) {
          if (first) {
            FileUtils.moveFile(subJar, new File(webStartDir, subModuleJar.getKey()));
            first = false;
          } else {
            forceDeleteOnExit(subJar);
          }
        }
      }
    }
  }

  private static void forceDeleteOnExit(File f) {
    try {
      FileUtils.forceDeleteOnExit(f);
    } catch (IOException e) {
      throw new IllegalStateException("path:" + f.getAbsolutePath(), e);
    }
  }


}

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

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.PluginManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * 将tis-plugin下的所有插件软连接到/opt/data/tis/plugins下
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-08-26 08:42
 **/
public class CreateSoftLink {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      throw new IllegalArgumentException("args length must be 1");
    }
    File pluginModuleDir = new File(args[0]);
    if (!pluginModuleDir.exists() || !pluginModuleDir.isDirectory()) {
      throw new IllegalArgumentException("pluginModuleDir is not illegal:" + pluginModuleDir.getAbsolutePath());
    }

    File targetPluginDir = TIS.pluginDirRoot;
    FileUtils.forceMkdir(targetPluginDir);
    FileUtils.cleanDirectory(targetPluginDir);
    Iterator<File> fileIt = FileUtils.iterateFiles(pluginModuleDir, new String[]{PluginManager.PACAKGE_TPI_EXTENSION_NAME}, true);
    File tpiFile = null;
    int fileCount = 0;
    while (fileIt.hasNext()) {
      tpiFile = fileIt.next();
      fileCount++;
      System.out.println("mk link:" + tpiFile.getAbsolutePath());
      Files.createSymbolicLink((new File(targetPluginDir, tpiFile.getName())).toPath(), tpiFile.toPath());
    }

    if (fileCount < 1) {
      throw new IllegalStateException("fileCount must more than 0");
    }
  }


}

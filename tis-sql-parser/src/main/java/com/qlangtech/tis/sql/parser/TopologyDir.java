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
package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-15 13:43
 */
public class TopologyDir {

    public final File dir;

    public final String relativePath;

    public TopologyDir(File dir, String topologyName) {
        this.dir = dir;
        this.relativePath = SqlTaskNode.NAME_DATAFLOW_DIR + "/" + topologyName;
    }

    public File synchronizeRemoteRes(String resName) {
        // CenterResource.copyFromRemote2Local(url, localFile);
        return CenterResource.copyFromRemote2Local(CenterResource.getPath(relativePath, resName), true);
        // return localFile;
    }

    public List<File> synchronizeSubRemoteRes() {

        File localSubFileDir = getLocalSubFileDir();

        Map<String, Boolean[]> localFileTag
                = localSubFileDir.exists()
                ? Arrays.stream(localSubFileDir.list((d, n) -> !n.endsWith(CenterResource.KEY_LAST_MODIFIED_EXTENDION)))
                .collect(Collectors.toMap((r) -> r, (r) -> new Boolean[]{Boolean.FALSE}))
                : Collections.emptyMap();

        Boolean[] localFileExistFlag = null;
        List<String> subFiles = CenterResource.getSubFiles(relativePath, false, true);
        List<File> subs = Lists.newArrayList();
        for (String f : subFiles) {
            /*****************************
             * 同步远端文件
             *****************************/
            subs.add(synchronizeRemoteRes(f));

            localFileExistFlag = localFileTag.get(f);
            if (localFileExistFlag != null) {
                // 标记本地文件对应的远端文件存在
                localFileExistFlag[0] = true;
            }
        }

        localFileTag.entrySet().forEach((entry) -> {
            if (!entry.getValue()[0]) {
                // 本地文件对应的远端文件不存在，则需要将本地文件删除掉
                FileUtils.deleteQuietly(new File(localSubFileDir, entry.getKey()));
                FileUtils.deleteQuietly(new File(localSubFileDir, entry.getKey() + CenterResource.KEY_LAST_MODIFIED_EXTENDION));
            }
        });

        return subs;
    }

    File getLocalSubFileDir() {
        return new File(Config.getMetaCfgDir(), relativePath);
    }

    public void delete() {
        try {
            FileUtils.forceDelete(this.dir);
        } catch (IOException e) {
            throw new RuntimeException("path:" + this.dir.getAbsolutePath(), e);
        }
    }
}

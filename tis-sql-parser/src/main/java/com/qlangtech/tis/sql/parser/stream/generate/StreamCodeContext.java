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
package com.qlangtech.tis.sql.parser.stream.generate;

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class StreamCodeContext {

    protected final String collectionName;

    protected final long timestamp;

    protected final File incrScriptDir;

    protected final boolean incrScriptDirCreated;

    public StreamCodeContext(String collectionName, long timestamp) {
        this.collectionName = collectionName;
        this.timestamp = timestamp;
        try {
            incrScriptDir = getScalaStreamScriptDir(this.collectionName, this.timestamp);
            this.incrScriptDirCreated = incrScriptDir.exists();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 存放增量脚本父目录
     *
     * @return
     */
    public File getIncrScriptDir() {
        return this.incrScriptDir;
    }

    public boolean isIncrScriptDirCreated() {
        return this.incrScriptDirCreated;
    }

    public static File getScalaStreamScriptDir(String collectionName, long timestamp) throws Exception {
        File dir = new File(StreamContextConstant.getStreamScriptRootDir(collectionName, timestamp)
                , "/src/main/scala/" + StringUtils.replace(Config.getGenerateParentPackage(), ".", "/") + "/" + collectionName);
        return dir;
    }
}

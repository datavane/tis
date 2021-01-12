/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.common.ConfigFileContext.ContentProcess;
import com.qlangtech.tis.pubhook.common.ConfigConstant;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-4-26
 */
public class ProcessConfigFile {

    private final ConfigFileContext.ContentProcess process;

    private final SnapshotDomain domain;

    public ProcessConfigFile(ContentProcess process, SnapshotDomain domain) {
        super();
        this.process = process;
        this.domain = domain;
    }

    public void execute() {
        try {
            for (PropteryGetter getter : ConfigFileReader.getConfigList()) {
//                if (ConfigConstant.FILE_JAR.equals(getter.getFileName())) {
//                    continue;
//                }
                // 没有属性得到
                if (getter.getUploadResource(domain) == null) {
                    continue;
                }
                String md5 = getter.getMd5CodeValue(this.domain);
                if (StringUtils.isBlank(md5)) {
                    continue;
                }
                try {
                    Thread.sleep(500);
                } catch (Throwable e) {
                }
                process.execute(getter, getter.getContent(this.domain));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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
package com.qlangtech.tis.manage.common;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.common.ConfigFileContext.ContentProcess;
import com.qlangtech.tis.pubhook.common.ConfigConstant;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
                if (ConfigConstant.FILE_JAR.equals(getter.getFileName())) {
                    continue;
                }
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

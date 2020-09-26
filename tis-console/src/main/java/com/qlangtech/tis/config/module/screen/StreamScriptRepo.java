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
package com.qlangtech.tis.config.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.DefaultFilter;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.HdfsAction;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 http://localhost:8080/tjs/config/stream_script_repo.action?path=/streamscript/search4totalpay/20190820171040/mq_meta/config.yaml
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class StreamScriptRepo extends BasicScreen {

    private static final Logger logger = LoggerFactory.getLogger(StreamScriptRepo.class);

    @Override
    @Func(value = PermissionConstant.PERMISSION_PLUGIN_GET_CONFIG, sideEffect = false)
    public void execute(Context context) throws Exception {
        File rootDir = Config.getDataDir();
        String path = this.getString("path");
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("illegal argument 'path'");
        }
        File targetFile = new File(rootDir, path);
        if (!targetFile.exists()) {
            throw new IllegalStateException("target file not exist:" + targetFile.getAbsolutePath());
        }
        boolean getMeta = Boolean.parseBoolean(this.getRequest().getHeader(ConfigFileContext.StreamProcess.HEADER_KEY_GET_FILE_META));
        logger.info("path:{},getChildren:{},local file exist:{},getMeta:{}", path, !targetFile.isFile(), targetFile.exists(), getMeta);
        if (targetFile.isFile()) {
            // 是否取文件meta信息
            HttpServletResponse response = HdfsAction.getDownloadResponse(targetFile, !getMeta);
            if (!getMeta) {
                try (InputStream input = FileUtils.openInputStream(targetFile)) {
                    IOUtils.copyLarge(input, response.getOutputStream());
                }
            }
        } else {
            HttpServletResponse response = (HttpServletResponse) DefaultFilter.getRespone();
            response.addHeader(ConfigFileContext.KEY_HEAD_FILE_DOWNLOAD, String.valueOf(false));
            List<String> subs = new ArrayList<>();
            File sub = null;
            for (String d : targetFile.list((d, fn) -> !StringUtils.endsWith(fn, CenterResource.KEY_LAST_MODIFIED_EXTENDION))) {
                sub = new File(targetFile, d);
                subs.add(d + ":" + (sub.isDirectory() ? "d" : "f"));
            }
            response.addHeader(ConfigFileContext.KEY_HEAD_FILES, subs.stream().collect(Collectors.joining(",")));
        // this.setBizResult(context, targetFile.list());
        }
    }
}

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
// */
package com.qlangtech.tis.runtime.module.action;

import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.DefaultFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class HdfsAction {


    public static HttpServletResponse getDownloadResponse(String pathName) {
        HttpServletResponse response = (HttpServletResponse) DefaultFilter.getRespone();
        response.setContentType("application/text");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + pathName + "\"");
        return response;
    }

    public static HttpServletResponse getDownloadResponse(File file, boolean hasContent) {
        HttpServletResponse response = (HttpServletResponse) DefaultFilter.getRespone();
        response.setContentType("application/text");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.addHeader(ConfigFileContext.KEY_HEAD_LAST_UPDATE, String.valueOf(file.lastModified()));
        response.addHeader(ConfigFileContext.KEY_HEAD_FILE_SIZE, String.valueOf(file.length()));
        response.addHeader(ConfigFileContext.KEY_HEAD_FILE_DOWNLOAD, String.valueOf(hasContent));
        return response;
    }

    public static HttpServletResponse getDownloadResponse(File file) {
        return getDownloadResponse(file, true);
    }
}

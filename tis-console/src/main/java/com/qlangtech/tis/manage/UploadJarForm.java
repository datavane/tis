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
package com.qlangtech.tis.manage;

import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class UploadJarForm {

    private File uploadfile;

    private File schema;

    private File config;

    private File spring;

    private File coreprop;

    private File datasource;

    public File getUploadfile() {
        return uploadfile;
    }

    public void setUploadfile(File uploadfile) {
        this.uploadfile = uploadfile;
    }

    public File getSchema() {
        return schema;
    }

    public void setSchema(File schema) {
        this.schema = schema;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public File getSpring() {
        return spring;
    }

    public void setSpring(File spring) {
        this.spring = spring;
    }

    public File getCoreprop() {
        return coreprop;
    }

    public void setCoreprop(File coreprop) {
        this.coreprop = coreprop;
    }

    public File getDatasource() {
        return datasource;
    }

    public void setDatasource(File datasource) {
        this.datasource = datasource;
    }
}

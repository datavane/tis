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
package com.qlangtech.tis.offline.pojo;

import org.json.JSONObject;

/**
 * git里文件两个版本的diff
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GitFileDiff {

    private String oldPath;

    private String newPath;

    private int aMode;

    private int bMode;

    private String diff;

    private boolean newFile;

    private boolean renamedFile;

    private boolean deletedFile;

    public GitFileDiff() {
    }

    public GitFileDiff(JSONObject jsonObject) {
        this.oldPath = jsonObject.getString("old_path");
        this.newPath = jsonObject.getString("new_path");
        this.aMode = jsonObject.getInt("a_mode");
        this.bMode = jsonObject.getInt("b_mode");
        this.diff = jsonObject.getString("diff");
        this.newFile = jsonObject.getBoolean("new_file");
        this.renamedFile = jsonObject.getBoolean("renamed_file");
        this.deletedFile = jsonObject.getBoolean("deleted_file");
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public int getaMode() {
        return aMode;
    }

    public void setaMode(int aMode) {
        this.aMode = aMode;
    }

    public int getbMode() {
        return bMode;
    }

    public void setbMode(int bMode) {
        this.bMode = bMode;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public boolean isNewFile() {
        return newFile;
    }

    public void setNewFile(boolean newFile) {
        this.newFile = newFile;
    }

    public boolean isRenamedFile() {
        return renamedFile;
    }

    public void setRenamedFile(boolean renamedFile) {
        this.renamedFile = renamedFile;
    }

    public boolean isDeletedFile() {
        return deletedFile;
    }

    public void setDeletedFile(boolean deletedFile) {
        this.deletedFile = deletedFile;
    }
}

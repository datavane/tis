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
package com.qlangtech.tis.db;

import com.qlangtech.tis.db.parser.domain.DBConfig;
import com.qlangtech.tis.git.GitUtils;
import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.offline.pojo.TISDb;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface IDBConfigAccess {

    void createDatabase(TISDb db, String commitLog);

    void updateDatabase(TISDb db, String commitLog);

    void processDBConfig(TISDb db, String commitLog, boolean isNew, boolean facade);

    void processDBConfig(TISDb db, String path, String commitLog, boolean isNew, boolean facade);

    String getDBConfigPath(String dbname, DbScope dbscope);

    //  void createTableDaily(TISTable table, String commitLog);

    // TISTable getTableConfig(String dbName, String tableName);

    void createDatasourceFileOnline(TISDb db, String commitLog);

    // private void updateFile(String path, String branch, String content,
    // String commitLog, int projectId) {
    // String urlString = "http://git.2dfire-inc.com/api/v4/projects/" +
    // projectId + "/repository/files";
    // List<PostParam> params = new ArrayList<>();
    // params.add(new PostParam("file_path", path));
    // params.add(new PostParam("branch_name", branch));
    // params.add(new PostParam("encoding", "base64"));
    // params.add(new PostParam("content",
    // Base64.getEncoder().encodeToString(content.getBytes(Charset.forName("utf8")))));
    // params.add(new PostParam("commit_message", commitLog));
    // 
    // String result = HttpUtils.put(urlString, params, new
    // GitPostStreamProcess<String>() {
    // @Override
    // public String p(int status, InputStream stream, String md5) {
    // try {
    // return IOUtils.toString(stream, "utf8");
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    // // System.out.println(result);
    // }
    void updateDatasourceFileOnline(String path, String content, String commitLog);

    void deleteDb(String name, GitUtils.GitUser user);

    void deleteDbOnline(String name, GitUtils.GitUser user);

    void deleteTableDaily(String dbName, String tableLogicName, GitUtils.GitUser user);

    void deleteTableOnline(String dbName, String tableLogicName, GitUtils.GitUser user);

   // DBConfig getDbLinkMetaData(String dbName, DbScope dbScope);

    List<String> listDbConfigPath(String dbname);

    boolean containFacadeDbTypeSubpath(String dbname);

    DBConfig getDbConfig(String dbName, DbScope dbScope);
}

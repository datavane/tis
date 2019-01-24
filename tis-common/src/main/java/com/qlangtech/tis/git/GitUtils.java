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
package com.qlangtech.tis.git;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.qlangtech.tis.git.GitUtils.IncrMonitorIndexs;
import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GitUtils {

    private static final ConfigFileContext.Header PRIVATE_TOKEN = new ConfigFileContext.Header("PRIVATE-TOKEN", "XqxWfcskmh9TskxGpEac");

    private static final ConfigFileContext.Header DELETE_METHOD = new ConfigFileContext.Header("method", "DELETE");

    public static final int WORKFLOW_GIT_PROJECT_ID = 1372;

    public static final int DATASOURCE_PROJECT_ID = 1375;

    public static final int DATASOURCE_PROJECT_ID_ONLINE = 1374;

    private GitUtils() {
    }

    private static final GitUtils SINGLEN = new GitUtils();

    public static GitUtils $() {
        return SINGLEN;
    }

    public static void main(String[] args) throws Exception {
    // String starem = $().getHiveJoinTaskConfig("search4totalpay",
    // RunEnvironment.ONLINE);
    // 
    // System.out.println(starem);
    // getChildren();
    // getProjects();
    }

    @SuppressWarnings("all")
    public IncrMonitorIndexs getIncrMonitorIndexs(RunEnvironment runtime) {
        IncrMonitorIndexs result = new IncrMonitorIndexs();
        return result;
    // InputStream input = null;
    // JSONTokener tokener = null;
    // JSONObject config = null;
    // JSONArray includes = null;
    // try {
    // String filePath = "mars/incr/incr-monitor-config.json";
    // URL url = new URL("http://git.2dfire-inc.com/api/v3/projects/" + WORKFLOW_GIT_PROJECT_ID
    // + "/repository/files?file_path=" + filePath + "&ref="
    // + (runtime == RunEnvironment.DAILY ? "develop" : "master"));
    // input = getFileContent(url);
    // tokener = new JSONTokener(IOUtils.toString(input, "utf8"));
    // config = new JSONObject(tokener);
    // includes = config.getJSONArray("includes");
    // 
    // for (int i = 0; i < includes.length(); i++) {
    // result.addInclude(includes.getString(i));
    // }
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // IOUtils.closeQuietly(input);
    // }
    // return result;
    }

    public static class IncrMonitorIndexs {

        public final Set<String> includes = Sets.newHashSet();

        public void addInclude(String indexName) {
            this.includes.add(indexName);
        }

        public void addIncludeAll(Collection<String> indexNames) {
            this.includes.addAll(indexNames);
        }
    }

    /**
     * http://git.2dfire-inc.com/dfire-searcher/tis-fullbuild-workflow/blob/
     * master/mars/hive/search4totalpay/join.xml
     *
     * @param collectionName
     * @param runtime
     * @return
     */
    @SuppressWarnings("all")
    public InputStream getHiveJoinTaskConfig(String collectionName, RunEnvironment runtime) {
        URL url = null;
        try {
            String filePath = getHiveJoinPath(collectionName);
            url = new URL("http://git.2dfire-inc.com/api/v3/projects/" + WORKFLOW_GIT_PROJECT_ID + "/repository/files?file_path=" + filePath + "&ref=" + (runtime == RunEnvironment.DAILY ? "develop" : "master"));
            return getFileContent(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("url:" + url, e);
        }
    // return HttpUtils.processContent(url, new GitStreamProcess<String>());
    // String urlString =
    // "http://git.2dfire-inc.com/api/v3/projects/"+WORKFLOW_GIT_PROJECT_ID+"/repository/files";
    // 
    // List<PostParam> params = new ArrayList<>();
    // params.add(new PostParam("file_path", "server/hello2.txt"));
    // params.add(new PostParam("branch_name", "master"));
    // params.add(new PostParam("encoding", "base64"));
    // params.add(new PostParam("content",
    // Base64.getEncoder().encodeToString("我爱北京天安门".getBytes(Charset.forName("utf8")))));
    // params.add(new PostParam("commit_message", "new added"));
    // 
    // HttpUtils.post(urlString, params, new GitPostStreamProcess<String>()
    // {
    // @Override
    // public String p(int status, InputStream stream, String md5) {
    // try {
    // return IOUtils.toString(stream, "utf8");
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    }

    public static String getHiveJoinPath(String collectionName) {
        return "mars/hive/" + collectionName + "/join.xml";
    }

    private InputStream getFileContent(URL url) {
        return HttpUtils.processContent(url, new GitStreamProcess<InputStream>() {

            @Override
            public InputStream p(int status, InputStream stream, String md5) {
                try {
                    JSONTokener tokener = new JSONTokener(IOUtils.toString(stream, "utf8"));
                    JSONObject o = new JSONObject(tokener);
                    return new ByteArrayInputStream(Base64.decodeBase64(o.getString("content")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            // try {
            // return new String(), "utf8");
            // } catch (Exception e) {
            // throw new RuntimeException(e);
            // }
            // 
            // try {
            // return new
            // String(Base64.getDecoder().decode(o.getString("content")),
            // "utf8");
            // } catch (Exception e) {
            // throw new RuntimeException(e);
            // }
            }
        });
    }

    private abstract static class GitStreamProcess<T> extends StreamProcess<T> {

        @Override
        public final List<ConfigFileContext.Header> getHeaders() {
            return createHeaders(super.getHeaders());
        }
    }

    private static List<ConfigFileContext.Header> createHeaders(List<ConfigFileContext.Header> orther) {
        List<ConfigFileContext.Header> heads = new ArrayList<>();
        heads.addAll(orther);
        heads.add(PRIVATE_TOKEN);
        return heads;
    }

    private static void createFile() {
    }
}

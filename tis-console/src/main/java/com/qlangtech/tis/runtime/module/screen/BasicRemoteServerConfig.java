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
package com.qlangtech.tis.runtime.module.screen;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.RemoteNotReachableException;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicRemoteServerConfig extends BasicManageScreen {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        String appName = this.getString("appname");
        Integer group = this.getInt("group");
        final String ip = this.getString("ip");
        // 动态变化的端口号 , coreNode启动时会发生变化
        int port = this.getInt("port") == null ? ConfigFileContext.getPort(this.getAppDomain().getRunEnvironment()) : this.getInt("port");
        String resName = this.getString("resname");
        Assert.assertNotNull(appName);
        Assert.assertNotNull(group);
        Assert.assertNotNull(ip);
        Assert.assertNotNull(resName);
        if (!isReachable(ip)) {
            context.put("filecontent", "服务器地址：" + ip + "连接不上，可能因为网络隔离");
            return;
        }
        try {
            byte[] fileContent = getFileContent(appName, group, ip, port, resName, this.getAppDomain().getRunEnvironment());
            context.put("filecontent", StringEscapeUtils.escapeHtml(new String(fileContent, Charset.forName(getEncode()))));
            this.forward("solr/remotecontent.vm");
        } catch (Exception e) {
            StringWriter reader = new StringWriter();
            PrintWriter errprint = new PrintWriter(reader);
            e.printStackTrace(errprint);
            this.addActionMessage(context, String.valueOf(reader.getBuffer()));
            errprint.close();
            return;
        }
    }

    // public static void traverseAllServers(String appName, int groupNum) {
    // LocatedCores locateCores = this.getClientProtocol().getCoreLocations(
    // appName);
    // for (LocatedCore group : locatedCores.getCores()) {
    // if(group.getCore().getCoreNums() != groupNum) {
    // continue;
    // }
    // for (CoreNodeExcessMap server : group.getLocs()) {
    // }
    // }
    // }
    public static byte[] getFileContent(String appName, Integer group, final String ip, int port, String resName, RunEnvironment runtime) throws Exception {
        Assert.assertNotNull(runtime);
        final String url = "http://" + StringUtils.trim(ip) + ":" + port + "/terminator-search/" + appName + "-" + group + "/admin/file/?file=" + resName;
        if (!isReachable(ip)) {
            throw new RemoteNotReachableException(group, ip);
        }
        final byte[] fileContent = ConfigFileContext.processContent(new URL(url), new StreamProcess<byte[]>() {

            public byte[] p(int status, InputStream stream, String md5) {
                try {
                    return IOUtils.toByteArray(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return fileContent;
    }

    private static boolean isReachable(final String ip) throws UnknownHostException, IOException {
        InetAddress remote = InetAddress.getByName(ip);
        return remote.isReachable(3000);
    }

    public static void main(String[] arg) throws Exception {
        InetAddress remote = InetAddress.getByName("10.232.12.21");
        System.out.println(remote.isReachable(3000));
    }
}

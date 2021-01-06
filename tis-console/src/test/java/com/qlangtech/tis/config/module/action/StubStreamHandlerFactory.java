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
package com.qlangtech.tis.config.module.action;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-05 17:39
 */
public class StubStreamHandlerFactory implements URLStreamHandlerFactory {
  public static URLStreamHandler streamHander;

  @Override
  public URLStreamHandler createURLStreamHandler(String protocol) {
    // System.out.println("=======================" + protocol);
    if ("http".equals(protocol)) {
      //return new StubHttpURLStreamHander();
      return streamHander;
    }
    return null;
  }

  public static class StubHttpURLStreamHander extends URLStreamHandler {
    @Override
    protected String toExternalForm(URL u) {
      return "mockURL"; //super.toExternalForm(u);
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
      return new StubHttpURLConnection(u);
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
      return new StubHttpURLConnection(u);
    }
  }

  //
  private static class StubHttpURLConnection extends HttpURLConnection {
    public StubHttpURLConnection(URL u) {
      super(u);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return super.getInputStream();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean usingProxy() {
      return false;
    }

    @Override
    public void connect() throws IOException {

    }
  }
}

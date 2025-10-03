/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.coredefine.module.action;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * 测试SSE连接在Jetty 9.4.31中的行为
 * 
 * @author 百岁 (baisui@qlangtech.com)
 */
public class TestSSEConnection {
    
    private Server server;
    private static final int TEST_PORT = 8899;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Before
    public void setUp() throws Exception {
        server = new Server(TEST_PORT);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        // 注册测试Servlet
        context.addServlet(new ServletHolder(new SSETestServlet()), "/sse/*");
        context.addServlet(new ServletHolder(new SSEWithAsyncServlet()), "/sse-async/*");
        
        server.setHandler(context);
        server.start();
    }
    
    @After
    public void tearDown() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
        }
        executorService.shutdown();
    }
    
    /**
     * 测试不使用AsyncContext的SSE连接
     */
    @Test
    public void testSSEWithoutAsyncContext() throws Exception {
        URL url = new URL("http://localhost:" + TEST_PORT + "/sse/test");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(5000);
        
        AtomicBoolean receivedData = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        // 异步读取响应
        executorService.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received (no async): " + line);
                    if (line.startsWith("data:")) {
                        receivedData.set(true);
                        latch.countDown();
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection closed unexpectedly: " + e.getMessage());
                latch.countDown();
            }
        });
        
        // 等待最多3秒
        boolean success = latch.await(3, TimeUnit.SECONDS);
        
        // 验证：如果没有使用AsyncContext，连接可能会过早关闭
        System.out.println("SSE without AsyncContext - Data received: " + receivedData.get());
    }
    
    /**
     * 测试使用AsyncContext的SSE连接
     */
    @Test
    public void testSSEWithAsyncContext() throws Exception {
        URL url = new URL("http://localhost:" + TEST_PORT + "/sse-async/test");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(5000);
        
        AtomicBoolean receivedAllData = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        // 异步读取响应
        executorService.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                int messageCount = 0;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received (with async): " + line);
                    if (line.startsWith("data:") && line.contains("message")) {
                        messageCount++;
                    }
                    if (line.contains("done")) {
                        receivedAllData.set(messageCount >= 3);
                        latch.countDown();
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading stream: " + e.getMessage());
                latch.countDown();
            }
        });
        
        // 等待最多5秒
        boolean success = latch.await(5, TimeUnit.SECONDS);
        
        // 验证：使用AsyncContext应该能接收到所有数据
        assertTrue("Should receive all data with AsyncContext", receivedAllData.get());
    }
    
    /**
     * 不使用AsyncContext的SSE Servlet（可能有问题）
     */
    private static class SSETestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("text/event-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Cache-Control", "no-cache");
            
            PrintWriter writer = resp.getWriter();
            
            // 发送初始数据
            writer.write("data: initial\n\n");
            writer.flush();
            
            // 异步发送更多数据（可能失败）
            executorService.execute(() -> {
                try {
                    Thread.sleep(1000);
                    writer.write("data: delayed message\n\n");
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    System.err.println("Failed to send delayed message: " + e.getMessage());
                }
            });
            
            // 方法返回，Servlet容器可能会关闭连接
        }
    }
    
    /**
     * 使用AsyncContext的SSE Servlet（推荐方式）
     */
    private static class SSEWithAsyncServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("text/event-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Cache-Control", "no-cache");
            
            // 启动异步处理
            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(10000); // 10秒超时
            
            PrintWriter writer = resp.getWriter();
            
            // 异步执行
            executorService.execute(() -> {
                try {
                    // 发送多条消息
                    for (int i = 1; i <= 3; i++) {
                        writer.write("data: message " + i + "\n\n");
                        writer.flush();
                        Thread.sleep(500);
                    }
                    
                    // 发送完成信号
                    writer.write("data: done\n\n");
                    writer.flush();
                    
                } catch (Exception e) {
                    System.err.println("Error in async processing: " + e.getMessage());
                } finally {
                    // 完成异步处理
                    asyncContext.complete();
                }
            });
            
            // 方法返回，但连接保持开放
        }
    }
}
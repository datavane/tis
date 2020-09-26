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
package com.qlangtech.tis.rpc.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-01 09:27
 */
public class IncrStatusServer {

    private final int port;

    // private final Server server;
    private ServerBuilder<?> serverBuilder;

    private Server server;

    private static final Logger logger = LoggerFactory.getLogger(IncrStatusServer.class);

    public IncrStatusServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port);
    }

    /**
     * Create a RouteGuide server using serverBuilder as a base and features as data.
     */
    public IncrStatusServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        this.serverBuilder = serverBuilder;
    // serverBuilder.addService(IncrStatusUmbilicalProtocolImpl.getInstance());
    }

    public void addService(BindableService bindSvc) {
        this.serverBuilder.addService(bindSvc);
    }

    // public void startLogging() {
    // IncrStatusUmbilicalProtocolImpl.getInstance().startLogging();
    // }
    /**
     * Start serving requests.
     */
    public void start() throws IOException {
        this.server = serverBuilder.build();
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    IncrStatusServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method.  This comment makes the linter happy.
     */
    public static void main(String[] args) throws Exception {
        IncrStatusServer server = new IncrStatusServer(8980);
        server.start();
        server.blockUntilShutdown();
    }
}

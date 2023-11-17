package com.qlangtech.tis.rpc.grpc.log.appender;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.33.0)",
    comments = "Source: logger-appender.proto")
public final class LogAppenderGrpc {

  private LogAppenderGrpc() {}

  public static final String SERVICE_NAME = "stream.LogAppender";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent,
      com.qlangtech.tis.rpc.grpc.log.common.Empty> getAppendMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Append",
      requestType = com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent.class,
      responseType = com.qlangtech.tis.rpc.grpc.log.common.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent,
      com.qlangtech.tis.rpc.grpc.log.common.Empty> getAppendMethod() {
    io.grpc.MethodDescriptor<com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent, com.qlangtech.tis.rpc.grpc.log.common.Empty> getAppendMethod;
    if ((getAppendMethod = LogAppenderGrpc.getAppendMethod) == null) {
      synchronized (LogAppenderGrpc.class) {
        if ((getAppendMethod = LogAppenderGrpc.getAppendMethod) == null) {
          LogAppenderGrpc.getAppendMethod = getAppendMethod =
              io.grpc.MethodDescriptor.<com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent, com.qlangtech.tis.rpc.grpc.log.common.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Append"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.qlangtech.tis.rpc.grpc.log.common.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LogAppenderMethodDescriptorSupplier("Append"))
              .build();
        }
      }
    }
    return getAppendMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LogAppenderStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogAppenderStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogAppenderStub>() {
        @java.lang.Override
        public LogAppenderStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogAppenderStub(channel, callOptions);
        }
      };
    return LogAppenderStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LogAppenderBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogAppenderBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogAppenderBlockingStub>() {
        @java.lang.Override
        public LogAppenderBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogAppenderBlockingStub(channel, callOptions);
        }
      };
    return LogAppenderBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LogAppenderFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogAppenderFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogAppenderFutureStub>() {
        @java.lang.Override
        public LogAppenderFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogAppenderFutureStub(channel, callOptions);
        }
      };
    return LogAppenderFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class LogAppenderImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 分布式环境下，写日志
     * </pre>
     */
    public void append(com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent request,
        io.grpc.stub.StreamObserver<com.qlangtech.tis.rpc.grpc.log.common.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getAppendMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getAppendMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent,
                com.qlangtech.tis.rpc.grpc.log.common.Empty>(
                  this, METHODID_APPEND)))
          .build();
    }
  }

  /**
   */
  public static final class LogAppenderStub extends io.grpc.stub.AbstractAsyncStub<LogAppenderStub> {
    private LogAppenderStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogAppenderStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogAppenderStub(channel, callOptions);
    }

    /**
     * <pre>
     * 分布式环境下，写日志
     * </pre>
     */
    public void append(com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent request,
        io.grpc.stub.StreamObserver<com.qlangtech.tis.rpc.grpc.log.common.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAppendMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class LogAppenderBlockingStub extends io.grpc.stub.AbstractBlockingStub<LogAppenderBlockingStub> {
    private LogAppenderBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogAppenderBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogAppenderBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 分布式环境下，写日志
     * </pre>
     */
    public com.qlangtech.tis.rpc.grpc.log.common.Empty append(com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent request) {
      return blockingUnaryCall(
          getChannel(), getAppendMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class LogAppenderFutureStub extends io.grpc.stub.AbstractFutureStub<LogAppenderFutureStub> {
    private LogAppenderFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogAppenderFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogAppenderFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 分布式环境下，写日志
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.qlangtech.tis.rpc.grpc.log.common.Empty> append(
        com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent request) {
      return futureUnaryCall(
          getChannel().newCall(getAppendMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_APPEND = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final LogAppenderImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(LogAppenderImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_APPEND:
          serviceImpl.append((com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent) request,
              (io.grpc.stub.StreamObserver<com.qlangtech.tis.rpc.grpc.log.common.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class LogAppenderBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LogAppenderBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.qlangtech.tis.rpc.grpc.log.appender.LogAppenderService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LogAppender");
    }
  }

  private static final class LogAppenderFileDescriptorSupplier
      extends LogAppenderBaseDescriptorSupplier {
    LogAppenderFileDescriptorSupplier() {}
  }

  private static final class LogAppenderMethodDescriptorSupplier
      extends LogAppenderBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    LogAppenderMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (LogAppenderGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LogAppenderFileDescriptorSupplier())
              .addMethod(getAppendMethod())
              .build();
        }
      }
    }
    return result;
  }
}

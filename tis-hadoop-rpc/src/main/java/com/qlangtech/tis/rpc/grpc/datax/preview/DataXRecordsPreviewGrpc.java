package com.qlangtech.tis.rpc.grpc.datax.preview;

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
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.33.0)",
        comments = "Source: preview-datax-records.proto")
public final class DataXRecordsPreviewGrpc {

    private DataXRecordsPreviewGrpc() {
    }

    public static final String SERVICE_NAME = "stream.DataXRecordsPreview";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria,
            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse> getPreviewRowsDataMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "previewRowsData",
            requestType = com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.class,
            responseType = com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria,
            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse> getPreviewRowsDataMethod() {
        io.grpc.MethodDescriptor<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria, com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse> getPreviewRowsDataMethod;
        if ((getPreviewRowsDataMethod = DataXRecordsPreviewGrpc.getPreviewRowsDataMethod) == null) {
            synchronized (DataXRecordsPreviewGrpc.class) {
                if ((getPreviewRowsDataMethod = DataXRecordsPreviewGrpc.getPreviewRowsDataMethod) == null) {
                    DataXRecordsPreviewGrpc.getPreviewRowsDataMethod = getPreviewRowsDataMethod =
                            io.grpc.MethodDescriptor.<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria, com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "previewRowsData"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new DataXRecordsPreviewMethodDescriptorSupplier("previewRowsData"))
                                    .build();
                }
            }
        }
        return getPreviewRowsDataMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static DataXRecordsPreviewStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<DataXRecordsPreviewStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<DataXRecordsPreviewStub>() {
                    @java.lang.Override
                    public DataXRecordsPreviewStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new DataXRecordsPreviewStub(channel, callOptions);
                    }
                };
        return DataXRecordsPreviewStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static DataXRecordsPreviewBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<DataXRecordsPreviewBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<DataXRecordsPreviewBlockingStub>() {
                    @java.lang.Override
                    public DataXRecordsPreviewBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new DataXRecordsPreviewBlockingStub(channel, callOptions);
                    }
                };
        return DataXRecordsPreviewBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static DataXRecordsPreviewFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<DataXRecordsPreviewFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<DataXRecordsPreviewFutureStub>() {
                    @java.lang.Override
                    public DataXRecordsPreviewFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new DataXRecordsPreviewFutureStub(channel, callOptions);
                    }
                };
        return DataXRecordsPreviewFutureStub.newStub(factory, channel);
    }

    /**
     *
     */
    public static abstract class DataXRecordsPreviewImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         * 同虚拟机下主进程查询执行进程执行结果通过此方法
         * </pre>
         */
        public void previewRowsData(com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria request,
                                    io.grpc.stub.StreamObserver<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse> responseObserver) {
            asyncUnimplementedUnaryCall(getPreviewRowsDataMethod(), responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getPreviewRowsDataMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria,
                                            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse>(
                                            this, METHODID_PREVIEW_ROWS_DATA)))
                    .build();
        }
    }

    /**
     *
     */
    public static final class DataXRecordsPreviewStub extends io.grpc.stub.AbstractAsyncStub<DataXRecordsPreviewStub> {
        private DataXRecordsPreviewStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected DataXRecordsPreviewStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new DataXRecordsPreviewStub(channel, callOptions);
        }

        /**
         * <pre>
         * 同虚拟机下主进程查询执行进程执行结果通过此方法
         * </pre>
         */
        public void previewRowsData(com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria request,
                                    io.grpc.stub.StreamObserver<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getPreviewRowsDataMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     *
     */
    public static final class DataXRecordsPreviewBlockingStub extends io.grpc.stub.AbstractBlockingStub<DataXRecordsPreviewBlockingStub> {
        private DataXRecordsPreviewBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected DataXRecordsPreviewBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new DataXRecordsPreviewBlockingStub(channel, callOptions);
        }

        /**
         * <pre>
         * 同虚拟机下主进程查询执行进程执行结果通过此方法
         * </pre>
         */
        public com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse
        previewRowsData(com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria request) {
            return blockingUnaryCall(
                    getChannel(), getPreviewRowsDataMethod(), getCallOptions(), request);
        }
    }

    /**
     *
     */
    public static final class DataXRecordsPreviewFutureStub extends io.grpc.stub.AbstractFutureStub<DataXRecordsPreviewFutureStub> {
        private DataXRecordsPreviewFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected DataXRecordsPreviewFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new DataXRecordsPreviewFutureStub(channel, callOptions);
        }

        /**
         * <pre>
         * 同虚拟机下主进程查询执行进程执行结果通过此方法
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse> previewRowsData(
                com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria request) {
            return futureUnaryCall(
                    getChannel().newCall(getPreviewRowsDataMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_PREVIEW_ROWS_DATA = 0;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final DataXRecordsPreviewImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(DataXRecordsPreviewImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_PREVIEW_ROWS_DATA:
                    serviceImpl.previewRowsData((com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria) request,
                            (io.grpc.stub.StreamObserver<com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataResponse>) responseObserver);
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

    private static abstract class DataXRecordsPreviewBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        DataXRecordsPreviewBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return com.qlangtech.tis.rpc.grpc.datax.preview.DataXRecordsPreviewService.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("DataXRecordsPreview");
        }
    }

    private static final class DataXRecordsPreviewFileDescriptorSupplier
            extends DataXRecordsPreviewBaseDescriptorSupplier {
        DataXRecordsPreviewFileDescriptorSupplier() {
        }
    }

    private static final class DataXRecordsPreviewMethodDescriptorSupplier
            extends DataXRecordsPreviewBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        DataXRecordsPreviewMethodDescriptorSupplier(String methodName) {
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
            synchronized (DataXRecordsPreviewGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new DataXRecordsPreviewFileDescriptorSupplier())
                            .addMethod(getPreviewRowsDataMethod())
                            .build();
                }
            }
        }
        return result;
    }
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common-msg.proto

package com.qlangtech.tis.rpc.grpc.log.common;

public interface JobLogOrBuilder extends
    // @@protoc_insertion_point(interface_extends:JobLog)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool waiting = 1;</code>
   */
  boolean getWaiting();

  /**
   * <code>uint32 mapper = 2;</code>
   */
  int getMapper();

  /**
   * <code>uint32 reducer = 3;</code>
   */
  int getReducer();
}

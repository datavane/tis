// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log-collector.proto

package com.qlangtech.tis.rpc.grpc.log.stream;

public final class LogCollectorProto {
  private LogCollectorProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PBuildPhaseStatusParam_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PBuildPhaseStatusParam_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PSynResTarget_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PSynResTarget_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PPhaseStatusCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PPhaseStatusCollection_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PDumpPhaseStatus_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PDumpPhaseStatus_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PDumpPhaseStatus_TablesDumpEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PDumpPhaseStatus_TablesDumpEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PJoinPhaseStatus_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PJoinPhaseStatus_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PJoinPhaseStatus_TaskStatusEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PJoinPhaseStatus_TaskStatusEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PBuildPhaseStatus_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PBuildPhaseStatus_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PBuildPhaseStatus_NodeBuildStatusEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PBuildPhaseStatus_NodeBuildStatusEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PIndexBackFlowPhaseStatus_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PIndexBackFlowPhaseStatus_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PIndexBackFlowPhaseStatus_NodesStatusEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PIndexBackFlowPhaseStatus_NodesStatusEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PMonotorTarget_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PMonotorTarget_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_stream_PExecuteState_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_stream_PExecuteState_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023log-collector.proto\022\006stream\032\020common-ms" +
      "g.proto\"(\n\026PBuildPhaseStatusParam\022\016\n\006tas" +
      "kid\030\001 \001(\004\"/\n\rPSynResTarget\022\014\n\004name\030\001 \001(\t" +
      "\022\020\n\010pipeline\030\002 \001(\010\"\366\001\n\026PPhaseStatusColle" +
      "ction\022+\n\tdumpPhase\030\001 \001(\0132\030.stream.PDumpP" +
      "haseStatus\022+\n\tjoinPhase\030\002 \001(\0132\030.stream.P" +
      "JoinPhaseStatus\022-\n\nbuildPhase\030\003 \001(\0132\031.st" +
      "ream.PBuildPhaseStatus\022C\n\030indexBackFlowP" +
      "haseStatus\030\004 \001(\0132!.stream.PIndexBackFlow" +
      "PhaseStatus\022\016\n\006taskId\030\005 \001(\r\"\225\001\n\020PDumpPha" +
      "seStatus\022<\n\ntablesDump\030\001 \003(\0132(.stream.PD" +
      "umpPhaseStatus.TablesDumpEntry\032C\n\017Tables" +
      "DumpEntry\022\013\n\003key\030\001 \001(\t\022\037\n\005value\030\002 \001(\0132\020." +
      "TableDumpStatus:\0028\001\"\224\001\n\020PJoinPhaseStatus" +
      "\022<\n\ntaskStatus\030\001 \003(\0132(.stream.PJoinPhase" +
      "Status.TaskStatusEntry\032B\n\017TaskStatusEntr" +
      "y\022\013\n\003key\030\001 \001(\t\022\036\n\005value\030\002 \001(\0132\017.JoinTask" +
      "Status:\0028\001\"\255\001\n\021PBuildPhaseStatus\022G\n\017node" +
      "BuildStatus\030\001 \003(\0132..stream.PBuildPhaseSt" +
      "atus.NodeBuildStatusEntry\032O\n\024NodeBuildSt" +
      "atusEntry\022\013\n\003key\030\001 \001(\t\022&\n\005value\030\002 \001(\0132\027." +
      "BuildSharedPhaseStatus:\0028\001\"\255\001\n\031PIndexBac" +
      "kFlowPhaseStatus\022G\n\013nodesStatus\030\001 \003(\01322." +
      "stream.PIndexBackFlowPhaseStatus.NodesSt" +
      "atusEntry\032G\n\020NodesStatusEntry\022\013\n\003key\030\001 \001" +
      "(\t\022\"\n\005value\030\002 \001(\0132\023.NodeBackflowStatus:\002" +
      "8\001\"d\n\016PMonotorTarget\022\022\n\ncollection\030\001 \001(\t" +
      "\022\016\n\006taskid\030\002 \001(\r\022.\n\007logtype\030\003 \001(\0162\035.stre" +
      "am.PExecuteState.LogType\"\213\003\n\rPExecuteSta" +
      "te\0220\n\010infoType\030\001 \001(\0162\036.stream.PExecuteSt" +
      "ate.InfoType\022.\n\007logType\030\002 \001(\0162\035.stream.P" +
      "ExecuteState.LogType\022\013\n\003msg\030\003 \001(\t\022\014\n\004fro" +
      "m\030\004 \001(\t\022\r\n\005jobId\030\005 \001(\004\022\016\n\006taskId\030\006 \001(\004\022\023" +
      "\n\013serviceName\030\007 \001(\t\022\021\n\texecState\030\010 \001(\t\022\014" +
      "\n\004time\030\t \001(\004\022\021\n\tcomponent\030\n \001(\t\"_\n\007LogTy" +
      "pe\022\035\n\031INCR_DEPLOY_STATUS_CHANGE\020\000\022\022\n\016MQ_" +
      "TAGS_STATUS\020\001\022\010\n\004FULL\020\002\022\010\n\004INCR\020\003\022\r\n\tINC" +
      "R_SEND\020\004\"4\n\010InfoType\022\010\n\004INFO\020\000\022\010\n\004WARN\020\001" +
      "\022\t\n\005ERROR\020\002\022\t\n\005FATAL\020\0032\300\002\n\014LogCollector\022" +
      "K\n\024RegisterMonitorEvent\022\026.stream.PMonoto" +
      "rTarget\032\025.stream.PExecuteState\"\000(\0010\001\022W\n\021" +
      "BuildPhraseStatus\022\036.stream.PBuildPhaseSt" +
      "atusParam\032\036.stream.PPhaseStatusCollectio" +
      "n\"\0000\001\0224\n\010InitTask\022\036.stream.PPhaseStatusC" +
      "ollection\032\006.Empty\"\000\022T\n\031LoadPhaseStatusFr" +
      "omLatest\022\025.stream.PSynResTarget\032\036.stream" +
      ".PPhaseStatusCollection\"\000BC\n%com.qlangte" +
      "ch.tis.rpc.grpc.log.streamB\021LogCollector" +
      "ProtoP\001\242\002\004HLWSb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.qlangtech.tis.rpc.grpc.log.common.LogCollectorProto.getDescriptor(),
        }, assigner);
    internal_static_stream_PBuildPhaseStatusParam_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_stream_PBuildPhaseStatusParam_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PBuildPhaseStatusParam_descriptor,
        new java.lang.String[] { "Taskid", });
    internal_static_stream_PSynResTarget_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_stream_PSynResTarget_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PSynResTarget_descriptor,
        new java.lang.String[] { "Name", "Pipeline", });
    internal_static_stream_PPhaseStatusCollection_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_stream_PPhaseStatusCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PPhaseStatusCollection_descriptor,
        new java.lang.String[] { "DumpPhase", "JoinPhase", "BuildPhase", "IndexBackFlowPhaseStatus", "TaskId", });
    internal_static_stream_PDumpPhaseStatus_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_stream_PDumpPhaseStatus_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PDumpPhaseStatus_descriptor,
        new java.lang.String[] { "TablesDump", });
    internal_static_stream_PDumpPhaseStatus_TablesDumpEntry_descriptor =
      internal_static_stream_PDumpPhaseStatus_descriptor.getNestedTypes().get(0);
    internal_static_stream_PDumpPhaseStatus_TablesDumpEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PDumpPhaseStatus_TablesDumpEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_stream_PJoinPhaseStatus_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_stream_PJoinPhaseStatus_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PJoinPhaseStatus_descriptor,
        new java.lang.String[] { "TaskStatus", });
    internal_static_stream_PJoinPhaseStatus_TaskStatusEntry_descriptor =
      internal_static_stream_PJoinPhaseStatus_descriptor.getNestedTypes().get(0);
    internal_static_stream_PJoinPhaseStatus_TaskStatusEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PJoinPhaseStatus_TaskStatusEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_stream_PBuildPhaseStatus_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_stream_PBuildPhaseStatus_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PBuildPhaseStatus_descriptor,
        new java.lang.String[] { "NodeBuildStatus", });
    internal_static_stream_PBuildPhaseStatus_NodeBuildStatusEntry_descriptor =
      internal_static_stream_PBuildPhaseStatus_descriptor.getNestedTypes().get(0);
    internal_static_stream_PBuildPhaseStatus_NodeBuildStatusEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PBuildPhaseStatus_NodeBuildStatusEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_stream_PIndexBackFlowPhaseStatus_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_stream_PIndexBackFlowPhaseStatus_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PIndexBackFlowPhaseStatus_descriptor,
        new java.lang.String[] { "NodesStatus", });
    internal_static_stream_PIndexBackFlowPhaseStatus_NodesStatusEntry_descriptor =
      internal_static_stream_PIndexBackFlowPhaseStatus_descriptor.getNestedTypes().get(0);
    internal_static_stream_PIndexBackFlowPhaseStatus_NodesStatusEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PIndexBackFlowPhaseStatus_NodesStatusEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_stream_PMonotorTarget_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_stream_PMonotorTarget_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PMonotorTarget_descriptor,
        new java.lang.String[] { "Collection", "Taskid", "Logtype", });
    internal_static_stream_PExecuteState_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_stream_PExecuteState_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_stream_PExecuteState_descriptor,
        new java.lang.String[] { "InfoType", "LogType", "Msg", "From", "JobId", "TaskId", "ServiceName", "ExecState", "Time", "Component", });
    com.qlangtech.tis.rpc.grpc.log.common.LogCollectorProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}

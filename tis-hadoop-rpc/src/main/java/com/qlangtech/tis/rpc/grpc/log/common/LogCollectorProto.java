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
package com.qlangtech.tis.rpc.grpc.log.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public final class LogCollectorProto {

    private LogCollectorProto() {
    }

    public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
    }

    static final com.google.protobuf.Descriptors.Descriptor internal_static_NodeBackflowStatus_descriptor;

    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_NodeBackflowStatus_fieldAccessorTable;

    static final com.google.protobuf.Descriptors.Descriptor internal_static_JoinTaskStatus_descriptor;

    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_JoinTaskStatus_fieldAccessorTable;

    static final com.google.protobuf.Descriptors.Descriptor internal_static_JoinTaskStatus_JobStatusEntry_descriptor;

    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_JoinTaskStatus_JobStatusEntry_fieldAccessorTable;

    static final com.google.protobuf.Descriptors.Descriptor internal_static_JobLog_descriptor;

    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_JobLog_fieldAccessorTable;

    static final com.google.protobuf.Descriptors.Descriptor internal_static_BuildSharedPhaseStatus_descriptor;

    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_BuildSharedPhaseStatus_fieldAccessorTable;

    static final com.google.protobuf.Descriptors.Descriptor internal_static_TableDumpStatus_descriptor;

    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_TableDumpStatus_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

    static {
        java.lang.String[] descriptorData = { "\n\020common-msg.proto\"y\n\022NodeBackflowStatus" + "\022\020\n\010nodeName\030\001 \001(\t\022\017\n\007allSize\030\002 \001(\004\022\016\n\006r" + "eaded\030\003 \001(\004\022\r\n\005faild\030\005 \001(\010\022\020\n\010complete\030\006" + " \001(\010\022\017\n\007waiting\030\007 \001(\010\"\306\001\n\016JoinTaskStatus" + "\022\024\n\014joinTaskName\030\001 \001(\t\0221\n\tjobStatus\030\002 \003(" + "\0132\036.JoinTaskStatus.JobStatusEntry\022\r\n\005fai" + "ld\030\005 \001(\010\022\020\n\010complete\030\006 \001(\010\022\017\n\007waiting\030\007 " + "\001(\010\0329\n\016JobStatusEntry\022\013\n\003key\030\001 \001(\r\022\026\n\005va" + "lue\030\002 \001(\0132\007.JobLog:\0028\001\":\n\006JobLog\022\017\n\007wait" + "ing\030\001 \001(\010\022\016\n\006mapper\030\002 \001(\r\022\017\n\007reducer\030\003 \001" + "(\r\"\231\001\n\026BuildSharedPhaseStatus\022\024\n\014allBuil" + "dSize\030\001 \001(\004\022\023\n\013buildReaded\030\002 \001(\004\022\016\n\006task" + "id\030\003 \001(\r\022\022\n\nsharedName\030\004 \001(\t\022\r\n\005faild\030\005 " + "\001(\010\022\020\n\010complete\030\006 \001(\010\022\017\n\007waiting\030\007 \001(\010\"\211" + "\001\n\017TableDumpStatus\022\021\n\ttableName\030\001 \001(\t\022\016\n" + "\006taskid\030\002 \001(\r\022\017\n\007allRows\030\003 \001(\r\022\020\n\010readRo" + "ws\030\004 \001(\r\022\r\n\005faild\030\005 \001(\010\022\020\n\010complete\030\006 \001(" + "\010\022\017\n\007waiting\030\007 \001(\010BC\n%com.qlangtech.tis." + "rpc.grpc.log.commonB\021LogCollectorProtoP\001" + "\242\002\004HLWSb\006proto3" };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {

            public com.google.protobuf.ExtensionRegistry assignDescriptors(com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                return null;
            }
        };
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] {}, assigner);
        internal_static_NodeBackflowStatus_descriptor = getDescriptor().getMessageTypes().get(0);
        internal_static_NodeBackflowStatus_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_NodeBackflowStatus_descriptor, new java.lang.String[] { "NodeName", "AllSize", "Readed", "Faild", "Complete", "Waiting" });
        internal_static_JoinTaskStatus_descriptor = getDescriptor().getMessageTypes().get(1);
        internal_static_JoinTaskStatus_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_JoinTaskStatus_descriptor, new java.lang.String[] { "JoinTaskName", "JobStatus", "Faild", "Complete", "Waiting" });
        internal_static_JoinTaskStatus_JobStatusEntry_descriptor = internal_static_JoinTaskStatus_descriptor.getNestedTypes().get(0);
        internal_static_JoinTaskStatus_JobStatusEntry_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_JoinTaskStatus_JobStatusEntry_descriptor, new java.lang.String[] { "Key", "Value" });
        internal_static_JobLog_descriptor = getDescriptor().getMessageTypes().get(2);
        internal_static_JobLog_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_JobLog_descriptor, new java.lang.String[] { "Waiting", "Mapper", "Reducer" });
        internal_static_BuildSharedPhaseStatus_descriptor = getDescriptor().getMessageTypes().get(3);
        internal_static_BuildSharedPhaseStatus_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_BuildSharedPhaseStatus_descriptor, new java.lang.String[] { "AllBuildSize", "BuildReaded", "Taskid", "SharedName", "Faild", "Complete", "Waiting" });
        internal_static_TableDumpStatus_descriptor = getDescriptor().getMessageTypes().get(4);
        internal_static_TableDumpStatus_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_TableDumpStatus_descriptor, new java.lang.String[] { "TableName", "Taskid", "AllRows", "ReadRows", "Faild", "Complete", "Waiting" });
    }
    // @@protoc_insertion_point(outer_class_scope)
}

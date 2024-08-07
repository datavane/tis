// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: preview-datax-records.proto

package com.qlangtech.tis.rpc.grpc.datax.preview;

/**
 * Protobuf type {@code stream.PreviewRowsDataCriteria}
 */
public  final class PreviewRowsDataCriteria extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:stream.PreviewRowsDataCriteria)
    PreviewRowsDataCriteriaOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PreviewRowsDataCriteria.newBuilder() to construct.
  private PreviewRowsDataCriteria(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PreviewRowsDataCriteria() {
    dataXName_ = "";
    tableName_ = "";
    orderByCols_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private PreviewRowsDataCriteria(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            dataXName_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            tableName_ = s;
            break;
          }
          case 24: {

            next_ = input.readBool();
            break;
          }
          case 34: {
            if (!((mutable_bitField0_ & 0x00000008) != 0)) {
              orderByCols_ = new java.util.ArrayList<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc>();
              mutable_bitField0_ |= 0x00000008;
            }
            orderByCols_.add(
                input.readMessage(com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.parser(), extensionRegistry));
            break;
          }
          case 40: {

            pageSize_ = input.readUInt32();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000008) != 0)) {
        orderByCols_ = java.util.Collections.unmodifiableList(orderByCols_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.qlangtech.tis.rpc.grpc.datax.preview.DataXRecordsPreviewService.internal_static_stream_PreviewRowsDataCriteria_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.qlangtech.tis.rpc.grpc.datax.preview.DataXRecordsPreviewService.internal_static_stream_PreviewRowsDataCriteria_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.class, com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.Builder.class);
  }

  private int bitField0_;
  public static final int DATAXNAME_FIELD_NUMBER = 1;
  private volatile java.lang.Object dataXName_;
  /**
   * <code>string dataXName = 1;</code>
   */
  public java.lang.String getDataXName() {
    java.lang.Object ref = dataXName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dataXName_ = s;
      return s;
    }
  }
  /**
   * <code>string dataXName = 1;</code>
   */
  public com.google.protobuf.ByteString
      getDataXNameBytes() {
    java.lang.Object ref = dataXName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      dataXName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TABLENAME_FIELD_NUMBER = 2;
  private volatile java.lang.Object tableName_;
  /**
   * <code>string tableName = 2;</code>
   */
  public java.lang.String getTableName() {
    java.lang.Object ref = tableName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      tableName_ = s;
      return s;
    }
  }
  /**
   * <code>string tableName = 2;</code>
   */
  public com.google.protobuf.ByteString
      getTableNameBytes() {
    java.lang.Object ref = tableName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      tableName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int NEXT_FIELD_NUMBER = 3;
  private boolean next_;
  /**
   * <code>bool next = 3;</code>
   */
  public boolean getNext() {
    return next_;
  }

  public static final int ORDERBYCOLS_FIELD_NUMBER = 4;
  private java.util.List<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc> orderByCols_;
  /**
   * <pre>
   * 主键值，如果是首次查询，可以不设置该值
   * </pre>
   *
   * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
   */
  public java.util.List<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc> getOrderByColsList() {
    return orderByCols_;
  }
  /**
   * <pre>
   * 主键值，如果是首次查询，可以不设置该值
   * </pre>
   *
   * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
   */
  public java.util.List<? extends com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder> 
      getOrderByColsOrBuilderList() {
    return orderByCols_;
  }
  /**
   * <pre>
   * 主键值，如果是首次查询，可以不设置该值
   * </pre>
   *
   * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
   */
  public int getOrderByColsCount() {
    return orderByCols_.size();
  }
  /**
   * <pre>
   * 主键值，如果是首次查询，可以不设置该值
   * </pre>
   *
   * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
   */
  public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc getOrderByCols(int index) {
    return orderByCols_.get(index);
  }
  /**
   * <pre>
   * 主键值，如果是首次查询，可以不设置该值
   * </pre>
   *
   * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
   */
  public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder getOrderByColsOrBuilder(
      int index) {
    return orderByCols_.get(index);
  }

  public static final int PAGESIZE_FIELD_NUMBER = 5;
  private int pageSize_;
  /**
   * <code>uint32 pageSize = 5;</code>
   */
  public int getPageSize() {
    return pageSize_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getDataXNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, dataXName_);
    }
    if (!getTableNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, tableName_);
    }
    if (next_ != false) {
      output.writeBool(3, next_);
    }
    for (int i = 0; i < orderByCols_.size(); i++) {
      output.writeMessage(4, orderByCols_.get(i));
    }
    if (pageSize_ != 0) {
      output.writeUInt32(5, pageSize_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getDataXNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, dataXName_);
    }
    if (!getTableNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, tableName_);
    }
    if (next_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(3, next_);
    }
    for (int i = 0; i < orderByCols_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, orderByCols_.get(i));
    }
    if (pageSize_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt32Size(5, pageSize_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria)) {
      return super.equals(obj);
    }
    com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria other = (com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria) obj;

    if (!getDataXName()
        .equals(other.getDataXName())) return false;
    if (!getTableName()
        .equals(other.getTableName())) return false;
    if (getNext()
        != other.getNext()) return false;
    if (!getOrderByColsList()
        .equals(other.getOrderByColsList())) return false;
    if (getPageSize()
        != other.getPageSize()) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + DATAXNAME_FIELD_NUMBER;
    hash = (53 * hash) + getDataXName().hashCode();
    hash = (37 * hash) + TABLENAME_FIELD_NUMBER;
    hash = (53 * hash) + getTableName().hashCode();
    hash = (37 * hash) + NEXT_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getNext());
    if (getOrderByColsCount() > 0) {
      hash = (37 * hash) + ORDERBYCOLS_FIELD_NUMBER;
      hash = (53 * hash) + getOrderByColsList().hashCode();
    }
    hash = (37 * hash) + PAGESIZE_FIELD_NUMBER;
    hash = (53 * hash) + getPageSize();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code stream.PreviewRowsDataCriteria}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:stream.PreviewRowsDataCriteria)
      com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteriaOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.qlangtech.tis.rpc.grpc.datax.preview.DataXRecordsPreviewService.internal_static_stream_PreviewRowsDataCriteria_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.qlangtech.tis.rpc.grpc.datax.preview.DataXRecordsPreviewService.internal_static_stream_PreviewRowsDataCriteria_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.class, com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.Builder.class);
    }

    // Construct using com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getOrderByColsFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      dataXName_ = "";

      tableName_ = "";

      next_ = false;

      if (orderByColsBuilder_ == null) {
        orderByCols_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000008);
      } else {
        orderByColsBuilder_.clear();
      }
      pageSize_ = 0;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.qlangtech.tis.rpc.grpc.datax.preview.DataXRecordsPreviewService.internal_static_stream_PreviewRowsDataCriteria_descriptor;
    }

    @java.lang.Override
    public com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria getDefaultInstanceForType() {
      return com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.getDefaultInstance();
    }

    @java.lang.Override
    public com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria build() {
      com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria buildPartial() {
      com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria result = new com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.dataXName_ = dataXName_;
      result.tableName_ = tableName_;
      result.next_ = next_;
      if (orderByColsBuilder_ == null) {
        if (((bitField0_ & 0x00000008) != 0)) {
          orderByCols_ = java.util.Collections.unmodifiableList(orderByCols_);
          bitField0_ = (bitField0_ & ~0x00000008);
        }
        result.orderByCols_ = orderByCols_;
      } else {
        result.orderByCols_ = orderByColsBuilder_.build();
      }
      result.pageSize_ = pageSize_;
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria) {
        return mergeFrom((com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria other) {
      if (other == com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria.getDefaultInstance()) return this;
      if (!other.getDataXName().isEmpty()) {
        dataXName_ = other.dataXName_;
        onChanged();
      }
      if (!other.getTableName().isEmpty()) {
        tableName_ = other.tableName_;
        onChanged();
      }
      if (other.getNext() != false) {
        setNext(other.getNext());
      }
      if (orderByColsBuilder_ == null) {
        if (!other.orderByCols_.isEmpty()) {
          if (orderByCols_.isEmpty()) {
            orderByCols_ = other.orderByCols_;
            bitField0_ = (bitField0_ & ~0x00000008);
          } else {
            ensureOrderByColsIsMutable();
            orderByCols_.addAll(other.orderByCols_);
          }
          onChanged();
        }
      } else {
        if (!other.orderByCols_.isEmpty()) {
          if (orderByColsBuilder_.isEmpty()) {
            orderByColsBuilder_.dispose();
            orderByColsBuilder_ = null;
            orderByCols_ = other.orderByCols_;
            bitField0_ = (bitField0_ & ~0x00000008);
            orderByColsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getOrderByColsFieldBuilder() : null;
          } else {
            orderByColsBuilder_.addAllMessages(other.orderByCols_);
          }
        }
      }
      if (other.getPageSize() != 0) {
        setPageSize(other.getPageSize());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.lang.Object dataXName_ = "";
    /**
     * <code>string dataXName = 1;</code>
     */
    public java.lang.String getDataXName() {
      java.lang.Object ref = dataXName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dataXName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string dataXName = 1;</code>
     */
    public com.google.protobuf.ByteString
        getDataXNameBytes() {
      java.lang.Object ref = dataXName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        dataXName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string dataXName = 1;</code>
     */
    public Builder setDataXName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      dataXName_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string dataXName = 1;</code>
     */
    public Builder clearDataXName() {
      
      dataXName_ = getDefaultInstance().getDataXName();
      onChanged();
      return this;
    }
    /**
     * <code>string dataXName = 1;</code>
     */
    public Builder setDataXNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      dataXName_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object tableName_ = "";
    /**
     * <code>string tableName = 2;</code>
     */
    public java.lang.String getTableName() {
      java.lang.Object ref = tableName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        tableName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string tableName = 2;</code>
     */
    public com.google.protobuf.ByteString
        getTableNameBytes() {
      java.lang.Object ref = tableName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tableName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string tableName = 2;</code>
     */
    public Builder setTableName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      tableName_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string tableName = 2;</code>
     */
    public Builder clearTableName() {
      
      tableName_ = getDefaultInstance().getTableName();
      onChanged();
      return this;
    }
    /**
     * <code>string tableName = 2;</code>
     */
    public Builder setTableNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      tableName_ = value;
      onChanged();
      return this;
    }

    private boolean next_ ;
    /**
     * <code>bool next = 3;</code>
     */
    public boolean getNext() {
      return next_;
    }
    /**
     * <code>bool next = 3;</code>
     */
    public Builder setNext(boolean value) {
      
      next_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bool next = 3;</code>
     */
    public Builder clearNext() {
      
      next_ = false;
      onChanged();
      return this;
    }

    private java.util.List<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc> orderByCols_ =
      java.util.Collections.emptyList();
    private void ensureOrderByColsIsMutable() {
      if (!((bitField0_ & 0x00000008) != 0)) {
        orderByCols_ = new java.util.ArrayList<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc>(orderByCols_);
        bitField0_ |= 0x00000008;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder> orderByColsBuilder_;

    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public java.util.List<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc> getOrderByColsList() {
      if (orderByColsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(orderByCols_);
      } else {
        return orderByColsBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public int getOrderByColsCount() {
      if (orderByColsBuilder_ == null) {
        return orderByCols_.size();
      } else {
        return orderByColsBuilder_.getCount();
      }
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc getOrderByCols(int index) {
      if (orderByColsBuilder_ == null) {
        return orderByCols_.get(index);
      } else {
        return orderByColsBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder setOrderByCols(
        int index, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc value) {
      if (orderByColsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOrderByColsIsMutable();
        orderByCols_.set(index, value);
        onChanged();
      } else {
        orderByColsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder setOrderByCols(
        int index, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder builderForValue) {
      if (orderByColsBuilder_ == null) {
        ensureOrderByColsIsMutable();
        orderByCols_.set(index, builderForValue.build());
        onChanged();
      } else {
        orderByColsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder addOrderByCols(com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc value) {
      if (orderByColsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOrderByColsIsMutable();
        orderByCols_.add(value);
        onChanged();
      } else {
        orderByColsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder addOrderByCols(
        int index, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc value) {
      if (orderByColsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOrderByColsIsMutable();
        orderByCols_.add(index, value);
        onChanged();
      } else {
        orderByColsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder addOrderByCols(
        com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder builderForValue) {
      if (orderByColsBuilder_ == null) {
        ensureOrderByColsIsMutable();
        orderByCols_.add(builderForValue.build());
        onChanged();
      } else {
        orderByColsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder addOrderByCols(
        int index, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder builderForValue) {
      if (orderByColsBuilder_ == null) {
        ensureOrderByColsIsMutable();
        orderByCols_.add(index, builderForValue.build());
        onChanged();
      } else {
        orderByColsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder addAllOrderByCols(
        java.lang.Iterable<? extends com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc> values) {
      if (orderByColsBuilder_ == null) {
        ensureOrderByColsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, orderByCols_);
        onChanged();
      } else {
        orderByColsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder clearOrderByCols() {
      if (orderByColsBuilder_ == null) {
        orderByCols_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
      } else {
        orderByColsBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public Builder removeOrderByCols(int index) {
      if (orderByColsBuilder_ == null) {
        ensureOrderByColsIsMutable();
        orderByCols_.remove(index);
        onChanged();
      } else {
        orderByColsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder getOrderByColsBuilder(
        int index) {
      return getOrderByColsFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder getOrderByColsOrBuilder(
        int index) {
      if (orderByColsBuilder_ == null) {
        return orderByCols_.get(index);  } else {
        return orderByColsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public java.util.List<? extends com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder> 
         getOrderByColsOrBuilderList() {
      if (orderByColsBuilder_ != null) {
        return orderByColsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(orderByCols_);
      }
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder addOrderByColsBuilder() {
      return getOrderByColsFieldBuilder().addBuilder(
          com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.getDefaultInstance());
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder addOrderByColsBuilder(
        int index) {
      return getOrderByColsFieldBuilder().addBuilder(
          index, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.getDefaultInstance());
    }
    /**
     * <pre>
     * 主键值，如果是首次查询，可以不设置该值
     * </pre>
     *
     * <code>repeated .stream.OffsetColValGrpc orderByCols = 4;</code>
     */
    public java.util.List<com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder> 
         getOrderByColsBuilderList() {
      return getOrderByColsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder> 
        getOrderByColsFieldBuilder() {
      if (orderByColsBuilder_ == null) {
        orderByColsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpc.Builder, com.qlangtech.tis.rpc.grpc.datax.preview.OffsetColValGrpcOrBuilder>(
                orderByCols_,
                ((bitField0_ & 0x00000008) != 0),
                getParentForChildren(),
                isClean());
        orderByCols_ = null;
      }
      return orderByColsBuilder_;
    }

    private int pageSize_ ;
    /**
     * <code>uint32 pageSize = 5;</code>
     */
    public int getPageSize() {
      return pageSize_;
    }
    /**
     * <code>uint32 pageSize = 5;</code>
     */
    public Builder setPageSize(int value) {
      
      pageSize_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>uint32 pageSize = 5;</code>
     */
    public Builder clearPageSize() {
      
      pageSize_ = 0;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:stream.PreviewRowsDataCriteria)
  }

  // @@protoc_insertion_point(class_scope:stream.PreviewRowsDataCriteria)
  private static final com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria();
  }

  public static com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PreviewRowsDataCriteria>
      PARSER = new com.google.protobuf.AbstractParser<PreviewRowsDataCriteria>() {
    @java.lang.Override
    public PreviewRowsDataCriteria parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new PreviewRowsDataCriteria(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<PreviewRowsDataCriteria> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PreviewRowsDataCriteria> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.qlangtech.tis.rpc.grpc.datax.preview.PreviewRowsDataCriteria getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


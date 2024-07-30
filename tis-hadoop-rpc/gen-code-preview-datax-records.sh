## 用于在tis-console中启动独立进程查看datax-pipeline同步数据预览功能
protoc --plugin=protoc-gen-grpc-java=/Users/mozhenghua/Downloads/protoc-gen-grpc-java-1.33.0-osx-x86_64.exe \
  --grpc-java_out="./src/main/java"   --java_out="./src/main/java" \
  --proto_path="./" ./preview-datax-records.proto

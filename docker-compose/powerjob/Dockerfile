#build by https://github.com/PowerJob/PowerJob/blob/master/powerjob-server/docker/Dockerfile
FROM powerjob/powerjob-server:4.3.6 as origin



FROM eclipse-temurin:8-jre-jammy
ENV TZ=Asia/Shanghai


ENV APP_NAME=powerjob-server
# 传递 SpringBoot 启动参数 和 JVM参数
ENV PARAMS=""
ENV JVMOPTIONS=""
# 将应用 jar 包拷入 docker
COPY --from=origin /powerjob-server.jar /powerjob-server.jar
# 暴露端口（HTTP + AKKA + VertX）
EXPOSE 7700 10086 10010
# 创建 docker 文件目录（盲猜这是用户目录）
RUN mkdir -p /root/powerjob-server
# 挂载数据卷，将文件直接输出到宿主机（注意，此处挂载的是匿名卷，即在宿主机位置随机）
# VOLUME /root/powerjob
# 启动应用
ENTRYPOINT ["sh","-c","java $JVMOPTIONS -jar /powerjob-server.jar $PARAMS"]



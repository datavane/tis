# s2i-java
#DOCKER_HOST=tcp://<host>:2375
FROM openjdk:8-jre
MAINTAINER baisui <baisui@2dfire.com>


ENTRYPOINT ["/bin/bash", "/usr/share/launch.sh"]


ARG TAT_FILE
ADD ${TAT_FILE} /usr/share/tis-console
ADD launch.sh /usr/share/launch.sh

RUN chown -R 1001:1001 /usr/share

USER 1001
EXPOSE 8080




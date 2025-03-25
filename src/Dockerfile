# 基础镜像
FROM openjdk:17
# 作者
LABEL maintainer="tmstack"
# 配置
ENV PARAMS=""
# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 添加应用
COPY target/mcp-server-useful-tools-0.0.1-SNAPSHOT.jar /mcp-server-useful-tools-0.0.1-SNAPSHOT.jar
## 在镜像运行为容器后执行的命令
ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=prod -XX:NewSize=200m -XX:MaxNewSize=200m -XX:InitialHeapSize=400m -XX:MaxHeapSize=400m -XX:SurvivorRatio=8 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log mcp-server-useful-tools-0.0.1-SNAPSHOT.jar $PARAMS"]

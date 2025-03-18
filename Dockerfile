FROM litongjava/jdk:8u411-stable-slim

RUN apt-get update && apt-get install -y ffmpeg libmp3lame0

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件到容器
COPY target/java-native-media-test-1.0.0.jar /app/

# 运行 JAR 文件
CMD ["java", "-jar", "java-native-media-test-1.0.0.jar"]
FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata ffmpeg && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="\
    -Xms768m \
    -Xmx768m \
    -XX:MetaspaceSize=128m \
    -XX:MaxMetaspaceSize=256m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:ParallelGCThreads=2 \
    -XX:ConcGCThreads=1 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Seoul"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

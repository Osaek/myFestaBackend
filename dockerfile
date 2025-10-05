FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata ffmpeg && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx256m -XX:MetaspaceSize=96m -XX:MaxMetaspaceSize=192m -XX:+UseG1GC -XX:+UseStringDeduplication -Xlog:gc*:gc.log:time"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

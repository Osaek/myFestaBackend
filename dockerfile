FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms128m -Xmx300m -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
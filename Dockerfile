FROM ghcr.io/graalvm/jdk-community:23
ARG JAR_FILE=build/libs/*.jar
ARG KAFKA_HOST=localhost
ARG REDIS_HOST=localhost:6379
ARG MONGO_HOST=localhost
ENV KAFKA_HOST=$KAFKA_HOST
ENV REDIS_HOST=$REDIS_HOST
ENV MONGO_HOST=$MONGO_HOST
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

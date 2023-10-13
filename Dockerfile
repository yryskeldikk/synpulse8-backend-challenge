FROM openjdk:17-jdk-slim

WORKDIR /app

COPY ./target/challenge-0.0.1-SNAPSHOT.jar .

ENTRYPOINT [ "java","-jar", "challenge-0.0.1-SNAPSHOT.jar" ]
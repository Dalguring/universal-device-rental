FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

ARG JAR_FILE=build/libs/app.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
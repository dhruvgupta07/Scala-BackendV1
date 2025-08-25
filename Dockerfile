FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/scala-2.12/Projectx-assembly-0.1.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

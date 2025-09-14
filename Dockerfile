FROM sbtscala/scala-sbt:eclipse-temurin-11.0.17_8_1.8.2_2.12.17 as build

WORKDIR /build
COPY . .
RUN sbt assembly

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /build/target/scala-2.12/Projectx-assembly-0.1.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

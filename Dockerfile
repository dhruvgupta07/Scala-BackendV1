FROM openjdk:11-jre-slim
WORKDIR /app
COPY . /app
RUN apt-get update && apt-get install -y curl && \
    curl -L -o sbt.deb https://github.com/sbt/sbt/releases/download/v1.8.2/sbt-1.8.2.deb && \
    dpkg -i sbt.deb && \
    rm sbt.deb && \
    sbt assembly

EXPOSE 8080
CMD ["java", "-jar", "target/scala-2.12/Projectx-assembly-0.1.0.jar"]

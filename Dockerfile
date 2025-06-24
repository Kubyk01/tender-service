FROM amazoncorretto:24

WORKDIR /app

COPY target/tender-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

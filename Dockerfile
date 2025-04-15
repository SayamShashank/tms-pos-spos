# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /geidea-tms

# Copy the JAR file from the target directory into the container
COPY target/ina-pay-tms-0.0.1-SNAPSHOT.jar ina-pay-tms-0.0.1-SNAPSHOT.jar
COPY src/main/resources/application.properties application.properties
COPY src/main/resources/keys keys
COPY src/main/resources/v6 v6
COPY src/main/resources/v8 v8
# Expose the port your Spring Boot application runs on (default: 8801)
EXPOSE 8801

# Run the application
ENTRYPOINT ["java", "-jar", "ina-pay-tms-0.0.1-SNAPSHOT.jar"]

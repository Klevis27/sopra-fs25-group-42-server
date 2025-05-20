FROM gradle:7.6-jdk17 as build
WORKDIR /app
COPY gradlew gradlew.bat /app/
COPY gradle /app/gradle
RUN chmod +x ./gradlew
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN ./gradlew clean build --no-daemon

FROM openjdk:17-slim
ENV SPRING_PROFILES_ACTIVE=prod

# Environment variables for connecting to MariaDB
ENV DATABASE_HOST=80.74.149.100
ENV DATABASE_PORT=3306
ENV DATABASE_USER=Klevis27
ENV DATABASE_PASSWORD=hh1#98Lv2
ENV DATABASE_NAME=sopra-fs25-group-42-db-production

ARG JWT_SECRET
ENV JWT_SECRET=$JWT_SECRET

USER 3301
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/soprafs25.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/soprafs25.jar"]

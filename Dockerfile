FROM gradle:6.8.3-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean && gradle build

FROM openjdk:8-jre-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/lib/* /app/lib/
COPY --from=build /home/gradle/src/build/libs/*.jar /app/labyrinth.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/labyrinth.jar"]

ARG VERSION=8u151

FROM openjdk:${VERSION}-jdk as BUILD

COPY . /src
WORKDIR /src
#RUN ./gradlew wrapper
#RUN ./gradlew clean build

FROM openjdk:${VERSION}-jre

COPY --from=BUILD /src/build/libs/planet-service-1.0-SNAPSHOT.jar /bin/runner/run.jar
WORKDIR /bin/runner

CMD ["java","-jar","run.jar"]
EXPOSE 8080
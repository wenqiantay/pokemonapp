FROM openjdk:22-jdk-oracle AS builder

ARG COMPILE_DIR=/complieddir

WORKDIR ${COMPILE_DIR}

COPY mvnw . 
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn
COPY src src

RUN ./mvnw package -Dmaven.test.skip=true

FROM openjdk:22-jdk-oracle

ARG WORK_DIR=/app

WORKDIR ${WORK_DIR}

COPY --from=builder /complieddir/target/pokemonapp-0.0.1-SNAPSHOT.jar pokemonapp.jar

ENV PORT=8080

EXPOSE ${PORT}

ENTRYPOINT java -jar pokemonapp.jar
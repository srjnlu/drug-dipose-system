FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*
COPY . .
ARG MODULE
RUN mvn -B -pl ${MODULE} -am package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
ARG MODULE
COPY --from=build /workspace/${MODULE}/target/${MODULE}-0.1.0.jar app.jar
USER 10001
EXPOSE 8081 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

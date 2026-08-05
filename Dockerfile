# syntax=docker/dockerfile:1

# Build: imagen oficial Maven + JDK 17 (no requiere mvnw dentro del contenedor)
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q -DskipTests package \
  && mkdir -p target/extracted \
  && java -Djarmode=tools -jar target/msvc-transactions-*.jar extract --launcher --destination target/extracted

# Runtime: solo JRE, imagen liviana
FROM eclipse-temurin:17-jre-jammy AS runtime
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/* \
  && groupadd --system spring \
  && useradd --system --gid spring --home-dir /app --create-home spring

WORKDIR /app
COPY --from=build /workspace/target/extracted/ ./

USER spring
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

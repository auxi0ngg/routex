FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user
RUN addgroup -S routex && adduser -S routex -G routex

# Copy JAR
COPY target/auth-service-1.0.0.jar app.jar

RUN chown routex:routex app.jar

USER routex

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S routex && adduser -S routex -G routex
COPY target/api-gateway-1.0.0.jar app.jar
RUN chown routex:routex app.jar
USER routex
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-XX:+UseG1GC","-jar","app.jar"]

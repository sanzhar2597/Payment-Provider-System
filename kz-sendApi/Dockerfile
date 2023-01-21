FROM registry.kz.eit.zone/microservice/base:1
EXPOSE 8082
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=*:8000,server=y,suspend=n
WORKDIR /opt
COPY target/*.jar /opt/app.jar
HEALTHCHECK  --interval=5m --timeout=3s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/session/healthCheak || exit 1
CMD ["java" , "-jar", "/opt/app.jar"]

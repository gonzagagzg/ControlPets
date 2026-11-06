FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/VETERINARIAITQ.war /usr/local/tomcat/webapps/VETERINARIAITQ.war
ENV CATALINA_OPTS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"
EXPOSE 8080
CMD ["catalina.sh", "run"]

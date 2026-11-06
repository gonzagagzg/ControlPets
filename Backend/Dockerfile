FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM tomcat:10.1-jdk17-temurin

# Eliminar aplicaciones predeterminadas
RUN rm -rf /usr/local/tomcat/webapps/*

# Desactivar el puerto de apagado 8005
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

# Instalar ControlPets
COPY --from=build /app/target/VETERINARIAITQ.war /usr/local/tomcat/webapps/VETERINARIAITQ.war

# Configurar memoria
ENV CATALINA_OPTS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"

EXPOSE 8080

CMD ["catalina.sh", "run"]


# Stage 1: Build the Java WAR
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Stage 2: Run NewsWeb using Tomcat
FROM tomcat:10.1-jdk21-temurin

# Deploy at /NewsWeb
COPY --from=build /app/target/NewsWeb.war \
    /usr/local/tomcat/webapps/NewsWeb.war

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

# Handle Windows CRLF line endings
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 10000

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

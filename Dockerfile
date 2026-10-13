
# Stage 1: Build the Java application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Stage 2: Run with Tomcat
FROM tomcat:10.1-jdk21-temurin

COPY --from=build /app/target/NewsWeb.war \
    /usr/local/tomcat/webapps/NewsWeb.war

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

# Fix Windows line endings and UTF-8 BOM
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && sed -i '1s/^\xEF\xBB\xBF//' /usr/local/bin/docker-entrypoint.sh \
    && chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 10000

# Run through the Linux shell explicitly
ENTRYPOINT ["/bin/sh", "/usr/local/bin/docker-entrypoint.sh"]

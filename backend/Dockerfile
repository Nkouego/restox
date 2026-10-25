# Etape1: Compiler l'application
FROM eclipse-temurin:21-jdk-noble AS build

WORKDIR /app

# Télécharger les dépendances Maven et compiler
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

#Etape2: Executer l'application 
FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build /app/target/*.jar app.jar

USER 10001

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
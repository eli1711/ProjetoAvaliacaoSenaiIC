# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e baixa dependências em cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante do código
COPY src ./src

# Gera o JAR final
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR gerado da etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Define a porta padrão
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java","-jar","app.jar"]

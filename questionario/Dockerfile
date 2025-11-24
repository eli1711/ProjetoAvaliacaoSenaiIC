# ============================
# STAGE 1 - Build com Maven
# ============================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o pom e baixa dependências em cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Gera o jar
RUN mvn clean package -DskipTests

# ============================
# STAGE 2 - Imagem final leve
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o JAR gerado do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Expor a porta da aplicação
EXPOSE 8080

# Variáveis padrão (podem ser sobrescritas no docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]

# Etapa de build
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Instalar dependências necessárias para as gems e cloc
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    pkg-config \
    libicu-dev \
    zlib1g-dev \
    libcurl4-openssl-dev \
    libssl-dev \
    ruby-dev \
    cloc \
    git 

# Instalar as gems github-linguist e rugged
RUN gem install github-linguist rugged

WORKDIR /home/knowledge-islands

# Criar a pasta temporária
RUN mkdir /home/knowledge-islands/temp
RUN mkdir /home/knowledge-islands/projects_files_logs
RUN mkdir /home/knowledge-islands/permanent_repositories

# Copiando os arquivos de configuração do Maven
COPY pom.xml . 
COPY src /home/knowledge-islands/src

# Variável de ambiente
ENV ENVIRONMENT=$ENVIRONMENT

# Fazendo o build do projeto
RUN mvn clean package -DskipTests

# Etapa de execução
FROM eclipse-temurin:17-jdk

# Instalar dependências necessárias para as gems e cloc
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    pkg-config \
    libicu-dev \
    zlib1g-dev \
    libcurl4-openssl-dev \
    libssl-dev \
    ruby-dev \
    cloc \
    git 

# Instalar as gems github-linguist e rugged
RUN gem install github-linguist rugged


WORKDIR /home/knowledge-islands

# Copiando o JAR gerado na etapa de build
COPY --from=build /home/knowledge-islands/target/git-analyzer-0.0.1-SNAPSHOT.jar /app/git-analyzer-0.0.1-SNAPSHOT.jar

# Definindo o ponto de entrada
CMD ["java", "-jar", "/app/git-analyzer-0.0.1-SNAPSHOT.jar"]

#Utilizamos la imagen de maven para crear el artefacto desplegable (jar) del proyecto
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /build

# Copiamos los archivos pom.xml del proyecto raíz y de los submódulos necesario
COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY service/pom.xml service/
COPY persistence/pom.xml persistence/

# Descargamos las dependencias para acelerar futuras compilaciones
RUN mvn dependency:go-offline -B

#Instalamos git para clonar el repositorio
RUN apk add git

#Clonamos el repositodiro data_microservice
RUN git clone https://github.com/JulianCastillo14/data_microservice.git

# Compilamos el proyecto y generamos el archivo .jar del módulo application, omitiendo los tests
RUN mvn clean package -DskipTests -pl application -am

# Usamos una imagen base más liviana con JDK 21 para ejecutar el .jar
FROM eclipse-temurin:21-jdk-jammy

# Instalamos ffmpeg (necesario para JavaCV)
RUN apt-get update && \
    apt-get install -y ffmpeg libavcodec-extra && \
    rm -rf /var/lib/apt/lists/*

# Establecemos el directorio de trabajo de la aplicación
WORKDIR /app

# Copiamos el .jar generado desde la imagen 'builder'
COPY --from=builder /build/application/target/*.jar app.jar

# Exponemos el puerto 8080, donde se ejecuta el microservicio
EXPOSE 8080

# Arrancamos el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
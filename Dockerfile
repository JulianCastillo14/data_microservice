#Utilizamos la imagen de maven para crear el artefacto desplegable (jar) del proyecto
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder

#Instalamos git para clonar el repositorio
RUN apt-get update \
  && apt-get install -y git \
  && rm -rf /var/lib/apt/lists/*

#Clonamos el repositodiro data_microservice
#RUN git clone https://github.com/JulianCastillo14/data_microservice.git
COPY . /data_microservice

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /data_microservice

# Compilamos el proyecto y generamos el archivo .jar del módulo application, omitiendo los tests
RUN mvn clean package -DskipTests -pl application -am

# Usamos una imagen base más liviana con JDK 21 para ejecutar el .jar
FROM eclipse-temurin:21-jdk-jammy

# Instalamos ffmpeg (necesario para JavaCV)
#RUN apt-get update && \
    #apt-get install -y ffmpeg libavcodec-extra && \
    #rm -rf /var/lib/apt/lists/*

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y ffmpeg \
    && rm -rf /var/lib/apt/lists/ \
    && which ffmpeg

# Establecemos el directorio de trabajo de la aplicación
WORKDIR /app
RUN mkdir -p /app/application
# Copiamos el .jar generado desde la imagen 'builder'
COPY --from=builder /data_microservice/application/target/*.jar app.jar

# Exponemos el puerto 8080, donde se ejecuta el microservicio
EXPOSE 8080

# Arrancamos el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
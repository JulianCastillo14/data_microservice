#Utilizamos la imagen de maven para crear el artefacto desplegable (jar) del proyecto
FROM maven:3.9.9-eclipse-temurin-17-alpine AS maven

#Instalamos git para clonar el repositorio
RUN apk add git

#Clonamos el repositodiro data_microservice
RUN git clone https://github.com/UIS-IoT-Smart-Campus/data_microservice

#Definimos el directorio de trabajo
WORKDIR /data_microservice

#Ejecutamos los comandos clean y package propios de maven para generar el jar
RUN mvn clean package 

#Utilizamos una imagen basada en alpine linux
FROM alpine

#Agregamos el openjdk17
RUN apk add openjdk17

#Copia el jar ejecutable de la imagen auxiliar alias maven
COPY  --from=maven /data_microservice/target/messages-0.0.1-SNAPSHOT.jar app.jar

#Arrancamos el microservicio
ENTRYPOINT [ "java","-jar","/app.jar" ]
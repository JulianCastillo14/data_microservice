
# Repositorio Microservicio de Datos/Mensajes IoT

En este repositorio se encuentra el código fuente del microservicio de almacenamiento de datos para la plataforma Smart Campus IoT UIS.

El microservicio está desarrollado en las siguientes tecnologías:

- Java 21
- Spring Boot 3.4.2
- InfluxDB 2.7 (docker influxdb:2.7)
- MongoDB (docker mongo latest)
- MinIO (docker quay.io/minio/minio)
- RabbitMQ 4.0-management (docker rabbitmq:4.0-management)
- EMQX 5.8 (docker emqx:5.8)

## Modulos del proyecto

| Módulo         | Propósito                                                                                         |
| -------------- |---------------------------------------------------------------------------------------------------|
| **application** | Exposición de endpoints REST, configuración de brokers EMQX Y RabbitMQ, manejo de CORS y Swagger. |
| **domain**      | Entidades de negocio y contratos de repositorio (interfaces).                                     |
| **service**     | Lógica de reencolado MQTT/AMQP y consultas a InfluxDB.                                            |
| **persistence** | Implementación para MongoDB, MinIO y InfluxDB.                                                    |

## Arquitectura 

![Diagrama de Arquitectura Onion](docs/images/Arquitectura_modulo_datos.jpg)

## API Document.

Para conocer los EndPoin de la API que expone el microservicio se instalo el paquete de Swagger que permite documenta fácilmente, para acceder se expone por la URL base del servicio + */swagger-ui.html#/*


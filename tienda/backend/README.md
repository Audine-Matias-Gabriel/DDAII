# Backend — Tienda

Java 17 + Spring Boot + Maven.

## Estructura prevista

```
src/main/java/com/tienda/{domain,repository,service,web,util,config,events,messaging,soap,ai,integration}
```

- `domain`: Producto, Cliente, Pedido, Tienda
- `repository`: DAO/Repository (primero en memoria, luego JPA + PostgreSQL)
- `service`: lógica de negocio tras interfaz (permite local vs remoto)
- `web`: REST + Swagger
- `util`: validación, logging, config

Actual: solo `com.ddaii.Main` Hello World. Falta migrar a Spring Boot (`spring-boot-starter-parent 3.x`: `web, data-jpa, validation, postgresql, springdoc-openapi`).

## Comandos

```bash
mvn test
mvn spring-boot:run
```

## Patrones / servicios (P1-P2)

Factory, Repository, Strategy, Observer/eventos, Facade. Servicios: Pedidos, Inventario, Clientes. RabbitMQ, SOAP puntual, API externa, IA (`AiService`).

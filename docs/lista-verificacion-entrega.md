# Lista de verificacion para entrega

## Estado de implementacion

El proyecto contiene los dos estilos de comunicacion pedidos:

1. Comunicacion sincrona HTTP:
   - `servicio-citas` expone `POST /api/citas`.
   - Antes de guardar la cita consulta por HTTP a `especialista-service` mediante `EspecialistaCliente`.
   - Tambien consulta/crea paciente mediante `PacienteCliente`.

2. Comunicacion asincrona RabbitMQ:
   - `especialista-service` publica el evento `ESPECIALISTA_CREADO` cuando se crea un especialista.
   - `servicio-citas` consume el evento y actualiza un espejo local de especialistas.
   - `GET /api/citas/async/especialistas` muestra los especialistas recibidos asincronicamente.
   - `POST /api/citas/async` agenda una cita usando el espejo local, sin consultar por HTTP a `especialista-service`.

## Cambios conservadores

- No se elimino ningun endpoint original.
- `POST /api/citas` sigue siendo el flujo principal sincrono.
- La asincronia queda apagada por defecto con `piedrazul.async.enabled=false`.
- El modo asincrono solo se activa usando el perfil `async`.

## Comandos de prueba recomendados

### Flujo sincrono

Levantar `especialista-service`:

```bash
cd especialista-service
./mvnw spring-boot:run
```

Levantar `servicio-citas`:

```bash
cd servicio-citas
./mvnw spring-boot:run
```

Crear especialista:

```http
POST http://localhost:8083/api/especialistas
```

Agendar cita sincrona:

```http
POST http://localhost:8081/api/citas
```

### Flujo asincrono

Levantar RabbitMQ:

```bash
docker run --rm -it -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Levantar `especialista-service` con perfil async:

```bash
cd especialista-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=async
```

Levantar `servicio-citas` con perfil async:

```bash
cd servicio-citas
./mvnw spring-boot:run -Dspring-boot.run.profiles=async
```

Crear especialista en `especialista-service` y luego revisar si llego al consumidor:

```http
GET http://localhost:8081/api/citas/async/especialistas
```

Agendar cita usando el espejo asincrono:

```http
POST http://localhost:8081/api/citas/async
```

## Nota

Para demostrar la parte asincrona es obligatorio tener RabbitMQ activo. Sin RabbitMQ, el proyecto puede usarse en modo normal porque la asincronia esta desactivada por defecto.

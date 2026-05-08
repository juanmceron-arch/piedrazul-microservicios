# Comunicacion sincrona y asincrona en Piedra Azul

Este proyecto conserva el flujo original y agrega una ruta asincrona equivalente al ejemplo de BookingGym.

## 1. Comunicacion sincrona HTTP

Ruta principal:

- `servicio-citas` expone `POST /api/citas`.
- Antes de guardar la cita, `CitaControlador` invoca por HTTP a:
  - `EspecialistaCliente` -> `GET http://localhost:8083/api/especialistas/{id}`.
  - `PacienteCliente` -> `GET/POST http://localhost:8082/api/pacientes`.
- Si el especialista no existe, la cita se rechaza.
- Si el horario ya esta ocupado, la cita se rechaza.

Esto es sincrono porque `servicio-citas` espera la respuesta del otro microservicio antes de terminar la solicitud.

## 2. Comunicacion asincrona con RabbitMQ

Ruta agregada sin reemplazar la anterior:

- `especialista-service` publica un evento `ESPECIALISTA_CREADO` al crear un especialista.
- `servicio-citas` consume ese evento y guarda una copia local en memoria llamada espejo de especialistas.
- `GET /api/citas/async/especialistas` permite ver los especialistas recibidos por eventos.
- `POST /api/citas/async` permite agendar usando el espejo local de especialistas, sin consultar por HTTP a `especialista-service`.

Esto es asincrono porque `especialista-service` no llama directamente a `servicio-citas`; solo publica el evento en RabbitMQ. `servicio-citas` lo consume cuando el broker se lo entrega.

## 3. Seguridad del cambio

Para no afectar el proyecto original:

- El endpoint original `POST /api/citas` no fue reemplazado.
- La comunicacion asincrona esta desactivada por defecto con `piedrazul.async.enabled=false`.
- Para probar el flujo asincrono, ejecutar ambos servicios con el perfil `async` y RabbitMQ disponible.

## 4. Prueba rapida del modo asincrono

1. Iniciar RabbitMQ, por ejemplo:

```bash
docker run --rm -it -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

2. Levantar `especialista-service` con perfil async:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=async
```

3. Levantar `servicio-citas` con perfil async:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=async
```

4. Crear un especialista:

```http
POST http://localhost:8083/api/especialistas
```

```json
{
  "id": "esp1",
  "nombre": "Juan Perez",
  "especialidad": "FISIOTERAPIA"
}
```

5. Verificar que llego al consumidor:

```http
GET http://localhost:8081/api/citas/async/especialistas
```

6. Agendar usando el espejo asincrono:

```http
POST http://localhost:8081/api/citas/async
```

```json
{
  "pacienteId": "pac1",
  "pacienteNombre": "Ana",
  "pacienteApellido": "Gomez",
  "pacienteTelefono": "3000000000",
  "pacienteGenero": "F",
  "especialistaId": "esp1",
  "especialistaNombre": "Juan Perez",
  "especialistaEspecialidad": "FISIOTERAPIA",
  "fecha": "2026-06-10",
  "hora": "09:00:00",
  "duracionMinutos": 30
}
```

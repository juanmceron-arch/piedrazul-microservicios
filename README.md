# piedrazul-microservicios

Proyecto de microservicios Piedra Azul.

## Comunicacion entre microservicios

Se documento y agrego soporte para los dos estilos vistos en BookingGym:

- Sincrono HTTP: `servicio-citas` valida especialistas y pacientes antes de guardar una cita.
- Asincrono RabbitMQ: `especialista-service` publica especialistas creados y `servicio-citas` mantiene un espejo local para agendar desde `/api/citas/async`.

Ver detalles en `docs/comunicacion-sync-async.md`.
Tambien se incluye una coleccion Postman en `postman/piedrazul-sync-async.postman_collection.json`.

## Entrega

Checklist y comandos de prueba en `docs/lista-verificacion-entrega.md`.

# Frontend Web clásico - Sistema Piedrazul

Frontend implementado con HTML, CSS y JavaScript puro. No usa Vite, React ni frameworks.

## Requisitos

Antes de abrir la interfaz, levante los microservicios:

- auth-service: http://localhost:8080
- especialista-service: http://localhost:8081
- appointment-service: http://localhost:8082

El backend fue ajustado para aceptar CORS desde:

- http://localhost:5500
- http://127.0.0.1:5500

## Ejecutar frontend

Desde esta carpeta:

```bash
python3 -m http.server 5500
```

Luego abra:

```txt
http://localhost:5500
```

## Flujo recomendado de prueba

1. Cree una cuenta de tipo `AGENDADOR` o `PACIENTE` desde la pantalla de registro.
2. Inicie sesión.
3. Registre un médico/terapeuta.
4. Configure sus días de atención.
5. Configure intervalo y ventana de agendamiento si lo necesita.
6. Agende una cita desde gestión manual o desde portal del paciente.
7. Consulte, exporte o cancele citas.

## Notas de integración

- Este frontend ya no usa datos demo ni datos mock.
- Si un microservicio está apagado, la pantalla mostrará el error real y no inventará registros locales.
- Las especialidades disponibles coinciden con el enum actual del backend: `MEDICINA_GENERAL`, `FISIOTERAPIA`, `PSICOLOGIA`.

# Piedrazul — Sistema de Gestión de Citas (Microservicios)

Plataforma para la gestión de citas de la **Red de Servicios Médicos Piedrazul**, construida con una arquitectura de microservicios sobre **Spring Boot**, autenticación centralizada con **Keycloak (OAuth2 / JWT)** y un frontend web servido con **Nginx**.

El sistema permite el registro y autenticación de usuarios (pacientes y agendadores), la administración de especialistas y su disponibilidad, y el ciclo completo de agendamiento de citas (agendar, reagendar, cancelar y cambiar de estado). El microservicio de citas se diseñó siguiendo los principios de **arquitectura hexagonal** y **Domain-Driven Design (DDD)**.

Proyecto desarrollado para la asignatura **Ingeniería de Software II** — Universidad del Cauca.

---

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Arquitectura hexagonal y DDD](#arquitectura-hexagonal-y-ddd)
- [Stack tecnológico](#stack-tecnológico)
- [Servicios y puertos](#servicios-y-puertos)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Patrones de diseño](#patrones-de-diseño)
- [Roles y seguridad](#roles-y-seguridad)
- [Endpoints de la API](#endpoints-de-la-api)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
- [Autores](#autores)

---

## Arquitectura

El sistema se compone de tres microservicios de negocio, un servidor de identidad (Keycloak) y un frontend web. Cada microservicio es autónomo, tiene su propia base de datos **SQLite** y se comunica con los demás vía **REST** propagando el token JWT.

```
                         ┌──────────────────────────┐
                         │       frontend-web        │
                         │   (Nginx · puerto 5500)   │
                         │   /api/* → reverse proxy   │
                         └────────────┬──────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
┌───────▼────────┐          ┌─────────▼─────────┐          ┌─────────▼──────────┐
│  auth-service  │          │ especialista-     │          │ appointment-       │
│   (8080)       │◄────────┐│ service (8081)    │◄────────┐│ service (8082)     │
│  Usuarios/JWT  │         ││ Especialistas/    │         ││ Citas (hexagonal)  │
└───────┬────────┘         │└───────────────────┘         │└────────────────────┘
        │                  │                              │
        │   appointment-service consume auth y especialista
        │   mediante clientes REST (PacienteClient,
        │   EspecialistaClient, DisponibilidadClient)
        │
┌───────▼─────────────────────────────────────────────────────────────┐
│                         Keycloak (8085)                               │
│              Emisor de tokens · Realm "piedrazul"                     │
└───────────────────────────────────────────────────────────────────────┘
```

Cada servicio actúa como **Resource Server OAuth2**: valida los JWT emitidos por Keycloak contra el conjunto de claves del realm `piedrazul`.

---

## Arquitectura hexagonal y DDD

El microservicio de citas (`appointment-service`) se diseñó siguiendo los principios de **arquitectura hexagonal (puertos y adaptadores)** y **Domain-Driven Design (DDD)**, con el fin de desacoplar la lógica de negocio de la infraestructura y de los mecanismos de persistencia y comunicación.

```
                      ┌───────────── Adaptadores de entrada ─────────────┐
                      │              CitaController (REST)                │
                      └───────────────────────┬──────────────────────────┘
                                              │  (puerto de entrada)
                      ┌───────────────────────▼──────────────────────────┐
                      │                    DOMINIO                        │
                      │   Cita, EstadoCita · Casos de uso (servicios):    │
                      │   Agendar · Reagendar · Cancelar · CambiarEstado  │
                      │   Consultar · HorarioSugerido                     │
                      └───────┬───────────────────────────────┬──────────┘
                  (puerto)    │                               │   (puertos)
            ┌─────────────────▼───────┐         ┌─────────────▼──────────────────┐
            │ Adaptador de persistencia │       │ Adaptadores de salida (clientes) │
            │ CitaRepository (JPA/SQLite)│      │ PacienteClient · EspecialistaClient│
            └──────────────────────────┘        │ DisponibilidadClient (REST)       │
                                                 └────────────────────────────────┘
```

- **Dominio (núcleo):** las entidades y reglas de negocio de las citas (`Cita`, `EstadoCita`) y los casos de uso (agendar, reagendar, cancelar, cambiar estado, consultar y sugerir horarios) son independientes de cualquier framework o tecnología de almacenamiento.
- **Puertos:** abstracciones que definen cómo el dominio se comunica con el exterior, como la interfaz de repositorio (`CitaRepository`) y los contratos de los clientes hacia otros servicios.
- **Adaptadores:** implementaciones concretas que conectan el dominio con el exterior — el adaptador de persistencia (Spring Data JPA sobre SQLite), los adaptadores de salida hacia otros microservicios (`PacienteClient`, `EspecialistaClient`, `DisponibilidadClient`) y los adaptadores de entrada (controladores REST).

Este enfoque permite que los cambios en la infraestructura (por ejemplo, cambiar la base de datos o el mecanismo de comunicación) o la incorporación de nuevas reglas de negocio (como agregar un nuevo tipo de profesional, p. ej. *Nutricionista*) se realicen modificando únicamente el dominio y sus adaptadores, **sin afectar a los demás microservicios**. Es la decisión arquitectónica que sustenta los escenarios de calidad de **modificabilidad** y **escalabilidad** del sistema.

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Framework backend | Spring Boot 3.3.5 (Web, Security, Data JPA) |
| Identidad / SSO | Keycloak 26.6.1 (OAuth2 · OpenID Connect · JWT) |
| Persistencia | SQLite (un archivo por servicio) + Hibernate `SQLiteDialect` |
| Comunicación entre servicios | REST sobre `RestTemplate` con propagación de JWT |
| Frontend web | HTML, CSS, JavaScript (vanilla) servido con Nginx |
| Frontend desktop | Java (módulo base, en desarrollo) |
| Orquestación | Docker · Docker Compose |
| Build | Maven (con Maven Wrapper `./mvnw`) |

> **Nota sobre persistencia:** `auth-service` y `appointment-service` usan **Spring Data JPA** sobre SQLite; `especialista-service` implementa un **DAO manual con JDBC** sobre SQLite.

---

## Servicios y puertos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| `frontend-web` | `5500` | Interfaz web (Nginx) y reverse proxy hacia los microservicios |
| `auth-service` | `8080` | Registro, login y consulta de usuarios/pacientes |
| `especialista-service` | `8081` | Gestión de especialistas y de su disponibilidad |
| `appointment-service` | `8082` | Agendamiento, reagendamiento, cancelación y estados de citas |
| `keycloak` | `8085` | Servidor de identidad (emisor de tokens) |

---

## Estructura del repositorio

```
piedrazul-microservicios/
├── auth-service/               # Microservicio de autenticación y usuarios
│   └── src/main/java/.../auth_service/
│       ├── controller/         # AuthController
│       ├── service/            # AuthServiceImpl + decoradores (Logging, Validation)
│       ├── factory/            # UsuarioFactory, PacienteFactory, AgendadorFactory, FactoryProducer
│       ├── repository/         # UsuarioRepository (JPA)
│       ├── config/             # Seguridad, CORS, ensamblado de decoradores
│       ├── model/              # Usuario, TipoUsuario, TipoGenero
│       └── DTO/                # RegisterRequest, LoginRequest, AuthResponse, PacienteResponse
│
├── especialista-service/       # Microservicio de especialistas y disponibilidad
│   └── src/main/java/.../especialista_service/
│       ├── controller/         # EspecialistaController, DisponibilidadController
│       ├── service/            # EspecialistaService, DisponibilidadService
│       ├── builder/            # DisponibilidadEspecialistaBuilder
│       ├── repository/         # Interfaces + implementaciones SQL (DAO manual)
│       ├── conexion/           # SQLConexionBD (conexión SQLite)
│       ├── model/              # Especialista, DisponibilidadEspecialista, FranjaHoraria
│       └── DTO/
│
├── appointment-service/        # Microservicio de citas (arquitectura hexagonal + DDD)
│   └── src/main/java/.../appointment_service/
│       ├── controller/         # CitaController (adaptador de entrada)
│       ├── service/            # Casos de uso: Agendar, Reagendar, Cancelar, CambiarEstado, Consultar, HorarioSugerido
│       ├── client/             # PacienteClient, EspecialistaClient, DisponibilidadClient (adaptadores de salida)
│       ├── repository/         # CitaRepository (adaptador de persistencia · JPA)
│       ├── config/             # Seguridad, CORS, RestTemplate con interceptor JWT
│       ├── model/              # Cita, EstadoCita, TipoGenero (dominio)
│       └── dto/
│
├── frontend-web/               # Cliente web (HTML/CSS/JS) servido por Nginx
├── frontend-desktop/           # Cliente de escritorio en Java (módulo base)
│
├── *.Dockerfile                # Un Dockerfile por servicio
├── docker-compose.yml          # Orquestación completa del sistema
└── nginx.conf                  # Configuración del reverse proxy
```

---

## Patrones de diseño

El proyecto implementa varios patrones de diseño de forma explícita:

| Patrón | Ubicación | Aplicación |
|--------|-----------|------------|
| **Factory Method** (+ Simple Factory) | `auth-service/factory` | `FactoryProducer` selecciona la fábrica adecuada (`PacienteFactory` / `AgendadorFactory`) según el rol y crea el `Usuario` correspondiente durante el registro. Encapsula la lógica de creación de los distintos tipos de usuario. |
| **Decorator** | `auth-service/service` | `ValidationAuthDecorator` y `LoggingAuthDecorator` envuelven `AuthServiceImpl` para añadir validación y registro de eventos sin modificar la lógica núcleo. Se ensamblan en `AuthConfig`. |
| **Builder** | `especialista-service/builder` | `DisponibilidadEspecialistaBuilder` construye objetos `DisponibilidadEspecialista` (días, horario, intervalo, semanas) de forma fluida y legible, evitando constructores extensos. |
| **Repository / DAO** | Todos los microservicios | Abstrae el acceso a datos: JPA en `auth` y `appointment` (`UsuarioRepository`, `CitaRepository`); DAO manual con JDBC en `especialista` (`EspecialistaRepositorio`, `DisponibilidadRepositorio`). |
| **DTO (Data Transfer Object)** | Todos los microservicios | Transporta datos entre capas y entre microservicios sin exponer las entidades del dominio (`RegisterRequest`, `AuthResponse`, `CrearEspecialistaDto`, `AgendarPacienteRequest`, etc.). |
| **Proxy Remoto / Service Client** | `appointment-service/client` | `EspecialistaClient`, `PacienteClient` y `DisponibilidadClient` actúan como representantes locales de servicios remotos, encapsulando la comunicación distribuida. |
| **Interceptor** | `appointment-service` (config REST) | Un interceptor del `RestTemplate` agrega y propaga el token JWT en cada petición saliente, centralizando la seguridad y evitando duplicar código en los clientes. |

---

## Roles y seguridad

La autorización se basa en roles de Keycloak validados con `@PreAuthorize`:

- **PACIENTE** — agenda sus propias citas, consulta especialistas y horarios.
- **AGENDADOR** — gestiona especialistas y disponibilidad, agenda en nombre de pacientes, cambia el estado de las citas y busca pacientes.

Todas las peticiones a los microservicios (excepto `register` y `login`) requieren un **token JWT** válido en el header `Authorization: Bearer <token>`.

---

## Endpoints de la API

### auth-service (`/auth`)
| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| `POST` | `/auth/register` | público | Registrar un usuario |
| `POST` | `/auth/login` | público | Iniciar sesión y obtener token |
| `GET`  | `/auth/pacientes/{id}` | PACIENTE, AGENDADOR | Obtener un paciente por id |
| `GET`  | `/auth/pacientes` | AGENDADOR | Buscar pacientes por documento |

### especialista-service
| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| `POST` | `/especialistas` | AGENDADOR | Crear un especialista |
| `GET`  | `/especialistas` | PACIENTE, AGENDADOR | Listar especialistas |
| `GET`  | `/especialistas/{id}` | PACIENTE, AGENDADOR | Obtener especialista por id |
| `POST` | `/disponibilidad/{especialistaId}` | AGENDADOR | Configurar disponibilidad |
| `GET`  | `/disponibilidad/{especialistaId}` | PACIENTE, AGENDADOR | Consultar disponibilidad |

### appointment-service (`/citas`)
| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| `POST` | `/citas/agendar/paciente` | PACIENTE | Agendar cita (paciente) |
| `POST` | `/citas/agendar/agendador` | AGENDADOR | Agendar cita en nombre de un paciente |
| `GET`  | `/citas` | PACIENTE, AGENDADOR | Listar citas |
| `PUT`  | `/citas/cancelar/{id}` | PACIENTE, AGENDADOR | Cancelar cita |
| `PUT`  | `/citas/{id}/estado?estado=` | AGENDADOR | Cambiar estado de la cita |
| `PUT`  | `/citas/reagendar/{id}` | PACIENTE, AGENDADOR | Reagendar cita |
| `GET`  | `/citas/horarios?especialistaId=&fecha=` | PACIENTE, AGENDADOR | Horarios sugeridos disponibles |

> A través del frontend, todas las rutas se exponen bajo el prefijo `/api/` (p. ej. `/api/auth/...`, `/api/especialistas/...`, `/api/appointments/...`) gracias al reverse proxy de Nginx.

---

## Cómo ejecutar el proyecto

### Requisitos previos
- [Docker](https://www.docker.com/) y Docker Compose
- (Opcional, para desarrollo local) Java 21 y Maven

### Levantar todo con Docker Compose

```bash
# Clonar el repositorio
git clone https://github.com/juanmceron-arch/piedrazul-microservicios.git
cd piedrazul-microservicios

# Construir y levantar todos los servicios
docker compose up --build
```

Una vez levantado:

| Recurso | URL |
|---------|-----|
| Frontend web | http://localhost:5500 |
| auth-service | http://localhost:8080 |
| especialista-service | http://localhost:8081 |
| appointment-service | http://localhost:8082 |
| Consola de Keycloak | http://localhost:8085 (admin / admin) |

### Configuración de Keycloak

El sistema espera un realm llamado **`piedrazul`** con los roles `PACIENTE` y `AGENDADOR`, y el cliente `piedrazul-auth-service` para la administración de usuarios. Crea el realm y los roles desde la consola de administración (http://localhost:8085) antes de registrar usuarios.

### Ejecución de un servicio en local (sin Docker)

```bash
cd auth-service
./mvnw spring-boot:run
```

---

## Autores

Proyecto académico desarrollado para la asignatura **Ingeniería de Software II**, Programa de **Ingeniería de Sistemas**, Facultad de Ingeniería Electrónica y Telecomunicaciones — **Universidad del Cauca**, Popayán (2026).

**Integrantes:**
- Nicole Burbano Solarte
- Juan Martín Cerón Chates
- John Alexander Ramirez
- Kevin Yesid Castaño

**Docentes:**
- Wilson Libardo Pantoja Yepez
- Roberto Encarnación Mosquera

**Repositorio:** https://github.com/juanmceron-arch/piedrazul-microservicios.git

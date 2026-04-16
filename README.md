# AstroPI - Sistema de Gestion de Incidencias y Peticiones

AstroPI es una aplicacion backend desarrollada con Spring Boot que proporciona una API REST para la gestion de incidencias y peticiones, inspirada en sistemas tipo HelpDesk o Jira.

El proyecto esta disenado para ser consumido por un frontend, por ejemplo React, ofreciendo una arquitectura escalable, segura y alineada con buenas practicas profesionales.

---

## Descripcion

AstroPI implementa un backend que permite:

- Autenticacion de usuarios mediante JWT.
- Registro basico de usuarios.
- Creacion y gestion de incidencias.
- Control de acceso por usuario y grupo.
- Generacion automatica de identificadores de ticket.
- Cambio de estado de incidencias.
- Proteccion de datos mediante uso de DTOs.
- Validacion de datos de entrada con Bean Validation.

El sistema esta preparado para integrarse con un frontend que proporcionara una interfaz visual para los usuarios finales.

---

## Arquitectura

El sistema sigue una arquitectura en capas:

- `Controller`: exposicion de endpoints REST.
- `Service`: logica de negocio.
- `Repository`: acceso a base de datos.
- `Model`: entidades JPA.
- `DTO`: transferencia segura de datos.
- `Security`: autenticacion y validacion JWT.
- `Config`: configuracion general de seguridad.

Estructura general:

```text
Frontend React
    |
Backend Spring Boot API
    |
Base de datos PostgreSQL
```

---

## Tecnologias

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Security
- JWT
- BCrypt
- Bean Validation
- Maven
- Git y GitHub
- Postman para pruebas
- DBeaver para gestion de base de datos

---

## Seguridad

La seguridad actual incluye:

- Autenticacion mediante JWT.
- Sistema stateless, sin sesiones de servidor.
- Contrasenas encriptadas con BCrypt.
- Control de acceso por roles.
- Endpoints de incidencias protegidos.
- Uso de DTOs para evitar exponer informacion sensible.
- Respuestas controladas para errores de autenticacion.

---

## Objetivo del proyecto

Construir un sistema completo de gestion de tickets que permita:

- Registro y autenticacion de usuarios.
- Creacion y gestion de incidencias.
- Creacion y gestion de peticiones.
- Control de acceso basado en grupos.
- Gestion avanzada por roles.
- Futuro panel de administracion para usuarios, grupos y permisos.

---

## Entidades principales

### Usuario

Campos principales:

- `id`
- `username`
- `password`
- `nombre`
- `apellidos`
- `email`
- `dni`
- `activo`
- `rol`
- `grupo`

Estado: implementado.

### Rol

Roles previstos:

- `USER`
- `ADMIN`
- `SUPER_ADMIN`

Estado: implementado a nivel de entidad y seguridad base.

### Grupo

Campos:

- `id`
- `nombre`

Estado: implementado. Falta gestion completa desde endpoints administrativos.

### Incidencia

Campos:

- `id`
- `codigoTicket`
- `titulo`
- `descripcion`
- `servicio`
- `categoria`
- `estado`
- `usuario`
- `grupo`
- `fechaCreacion`

Estado: implementado.

### Peticion

Funcionara de forma similar a incidencia, con codigo automatico:

```text
P-YYYYMMDD-0001
```

Estado: pendiente.

### Permiso

Sistema granular de permisos.

Estado: pendiente.

---

## Gestion de usuarios

### Autenticacion

El sistema permite login mediante:

- `username`
- `password`

Tras un login correcto, la API devuelve un token JWT.

### Registro

El registro basico permite enviar:

- `username`
- `nombre`
- `apellidos`
- `email`
- `dni`
- `password`

El usuario se guarda con password encriptada mediante BCrypt.

---

## Sistema de tickets

El sistema esta pensado para gestionar dos tipos de tickets:

- Incidencias.
- Peticiones.

Actualmente el modulo de incidencias esta implementado. El modulo de peticiones queda pendiente.

Cada ticket incluye:

- Codigo automatico.
- Titulo.
- Descripcion.
- Servicio.
- Categoria.
- Estado.
- Fecha de creacion automatica.
- Usuario creador.
- Grupo destino.

Formato de codigos:

```text
I-YYYYMMDD-0001
P-YYYYMMDD-0001
```

---

## Control de acceso

Cada usuario pertenece a un grupo.

Un usuario puede ver:

- Sus propias incidencias.
- Incidencias asociadas a su grupo.

Las incidencias solo se pueden consultar y modificar si el usuario autenticado tiene acceso a ellas.

---

## Funcionalidades implementadas

- Autenticacion con JWT.
- Endpoint `/auth/login`.
- Endpoint `/auth/register`.
- Endpoint `/auth/me`.
- Creacion de incidencias.
- Generacion automatica de codigo de ticket.
- Listado de incidencias por usuario.
- Listado de incidencias por usuario y grupo.
- Cambio de estado de incidencias.
- Estado `CERRADA`.
- Regla para evitar reapertura de incidencias cerradas.
- Validaciones con Bean Validation.
- Uso de DTOs para requests y responses.
- Manejo basico de errores de validacion.

---

## Endpoints principales

### Autenticacion

```http
POST /auth/login
POST /auth/register
GET /auth/me
```

### Incidencias

```http
POST /incidencias
GET /incidencias/mis-incidencias
GET /incidencias
PUT /incidencias/{id}/estado
```

---

## Estados de incidencia

Estados disponibles:

- `ABIERTA`
- `EN_PROCESO`
- `PARADA`
- `RESUELTA`
- `CERRADA`

Una incidencia en estado `CERRADA` no puede reabrirse.

---

## DTOs implementados

- `LoginRequest`
- `LoginResponse`
- `RegisterRequest`
- `UserResponse`
- `IncidenciaRequest`
- `IncidenciaResponse`
- `EstadoRequest`

---

## Ejemplo de uso

### Login

```http
POST /auth/login
```

```json
{
  "username": "admin2",
  "password": "password"
}
```

Respuesta:

```json
{
  "token": "jwt..."
}
```

### Crear incidencia

```http
POST /incidencias
```

Requiere token JWT.

```json
{
  "titulo": "Error al iniciar sesion",
  "descripcion": "El usuario no puede acceder a la aplicacion",
  "servicio": "Autenticacion",
  "categoria": "Bug",
  "grupoId": 1
}
```

Respuesta:

```json
{
  "id": 7,
  "codigoTicket": "I-20260416-0001",
  "titulo": "Error al iniciar sesion",
  "descripcion": "El usuario no puede acceder a la aplicacion",
  "servicio": "Autenticacion",
  "categoria": "Bug",
  "estado": "ABIERTA",
  "grupo": "Desarrollo",
  "usuario": "admin2",
  "fechaCreacion": "2026-04-16T18:55:55.643658"
}
```

### Cambiar estado

```http
PUT /incidencias/7/estado
```

Requiere token JWT.

```json
{
  "estado": "CERRADA"
}
```

---

## Validaciones

### IncidenciaRequest

Validaciones actuales:

- `titulo`: obligatorio, maximo 150 caracteres.
- `descripcion`: obligatoria, maximo 1000 caracteres.
- `servicio`: obligatorio, maximo 100 caracteres.
- `categoria`: obligatoria, maximo 100 caracteres.
- `grupoId`: obligatorio y positivo.

### EstadoRequest

Validaciones actuales:

- `estado`: obligatorio.
- Debe coincidir con un valor del enum `EstadoIncidencia`.

Ejemplo de error de validacion:

```json
{
  "error": "Datos invalidos",
  "campos": {
    "titulo": "El titulo es obligatorio"
  }
}
```

---

## PostgreSQL y DBeaver

La base de datos usada es PostgreSQL y se gestiona durante el desarrollo con DBeaver.

Configuracion actual:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/astroPI_DB
spring.datasource.username=postgres
spring.jpa.hibernate.ddl-auto=update
```

Nota importante:

Si se anade un nuevo valor al enum de estados, PostgreSQL puede mantener una constraint antigua en la tabla `incidencias`. Esto ocurrio al anadir el estado `CERRADA`.

Script disponible:

```text
docs/database/incidencias_estado_check.sql
```

Contenido:

```sql
ALTER TABLE incidencias
DROP CONSTRAINT IF EXISTS incidencias_estado_check;

ALTER TABLE incidencias
ADD CONSTRAINT incidencias_estado_check
CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'));
```

Este script se puede ejecutar desde el SQL Editor de DBeaver.

---

## Instalacion y ejecucion

Clonar el repositorio:

```bash
git clone https://github.com/tu-usuario/astropi.git
```

Configurar la base de datos en:

```text
src/main/resources/application.properties
```

Ejecutar la aplicacion:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

---

## Pruebas

Ejecutar tests:

```powershell
.\mvnw.cmd test
```

Actualmente hay pruebas para:

- Carga del contexto de Spring Boot.
- Deserializacion del estado `CERRADA` en `EstadoRequest`.

---

## Estado del proyecto

| Area | Estado |
| --- | --- |
| Backend seguridad | Implementado |
| JWT stateless | Implementado |
| Registro/Login | Implementado |
| Backend incidencias | Implementado |
| Validaciones incidencias | Implementado |
| Cambio de estado | Implementado |
| Backend usuarios | En progreso |
| Backend grupos | En progreso |
| Backend peticiones | Pendiente |
| Permisos granulares | Pendiente |
| Frontend React | Pendiente |

---

## Proximas mejoras

- Implementar modulo de peticiones.
- Crear filtros avanzados de incidencias por estado, fecha, servicio o categoria.
- Completar gestion de usuarios.
- Completar gestion de grupos.
- Crear sistema de permisos granular.
- Crear panel de administracion para `SUPER_ADMIN`.
- Preparar coleccion de Postman.
- Valorar Flyway o Liquibase para migraciones de base de datos.
- Desarrollar frontend en React.

---

## Buenas practicas del proyecto

Durante el desarrollo se busca mantener:

- Codigo limpio y bien estructurado.
- Separacion por capas.
- Uso de DTOs.
- Comentarios claros de nivel junior profesional.
- Flujo paso a paso.
- Pruebas con Postman.
- Control de base de datos con DBeaver.
- Commits claros y bien definidos en Git.

---

## Autor

Proyecto desarrollado por Daniel Montero Ruiz como parte de aprendizaje en desarrollo backend con Spring Boot.

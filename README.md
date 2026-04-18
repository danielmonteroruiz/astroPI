# AstroPI - Sistema de Gestion de Incidencias y Peticiones

AstroPI es una aplicacion backend desarrollada con Spring Boot que proporciona una API REST para la gestion de incidencias y peticiones, inspirada en sistemas tipo HelpDesk o Jira.

El proyecto esta disenado para ser consumido por un frontend, por ejemplo React, ofreciendo una arquitectura escalable, segura y alineada con buenas practicas profesionales.

---

## Descripcion

AstroPI implementa un backend que permite:

- Autenticacion de usuarios mediante JWT.
- Registro basico de usuarios.
- Creacion y gestion de incidencias.
- Creacion y gestion de peticiones.
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
- `SUPER_ADMIN`

Estado: implementado a nivel de entidad y seguridad base.

### Grupo

Campos:

- `id`
- `nombre`

Estado: implementado. Ya existe consulta de grupos y creacion desde admin. Falta edicion, eliminacion y asignaciones avanzadas.

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

Funciona de forma similar a incidencia, con codigo automatico:

```text
P-YYYYMMDD-0001
```

Estado: implementado en backend.

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

Reglas actuales:

- No se permite registrar un `username` duplicado.
- No se permite registrar un `dni` duplicado.
- No se permite registrar un `email` duplicado si se envia.
- Si `email` llega vacio, se guarda como `null`.

---

## Sistema de tickets

El sistema esta pensado para gestionar dos tipos de tickets:

- Incidencias.
- Peticiones.

Actualmente los modulos de incidencias y peticiones estan implementados en backend.

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

La fecha corresponde al dia de creacion, pero la numeracion no se reinicia cada dia. Cada tipo de ticket mantiene su propia secuencia continua:

```text
I-20260401-0003
I-20260402-0004

P-20260401-0003
P-20260402-0004
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
- Creacion de peticiones.
- Generacion automatica de codigo de peticion.
- Listado de peticiones por usuario.
- Listado de peticiones por usuario y grupo.
- Cambio de estado de peticiones.
- Consulta de grupos.
- Creacion de grupos desde admin.
- Listado de usuarios desde admin.
- Asignacion de grupo a usuarios desde admin.
- Asignacion de rol a usuarios desde admin.
- Activacion y desactivacion de usuarios desde admin.
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
GET /incidencias?estado=ABIERTA
GET /incidencias?servicio=Autenticacion
GET /incidencias?categoria=Bug
GET /incidencias?grupoId=1
GET /incidencias?fechaDesde=2026-04-01
GET /incidencias?fechaHasta=2026-04-18
PUT /incidencias/{id}/estado
```

### Peticiones

```http
POST /peticiones
GET /peticiones/mis-peticiones
GET /peticiones
GET /peticiones?estado=ABIERTA
GET /peticiones?servicio=Accesos
GET /peticiones?categoria=Alta%20de%20permisos
GET /peticiones?grupoId=1
GET /peticiones?fechaDesde=2026-04-01
GET /peticiones?fechaHasta=2026-04-18
PUT /peticiones/{id}/estado
```

### Grupos

```http
GET /grupos
POST /admin/grupos
PUT /admin/grupos/{id}
DELETE /admin/grupos/{id}
```

`GET /grupos` requiere usuario autenticado.

Los endpoints `/admin/grupos` requieren rol `SUPER_ADMIN`.

### Administracion de usuarios

```http
GET /admin/usuarios
GET /admin/roles
PUT /admin/usuarios/{id}
PUT /admin/usuarios/{id}/grupo
PUT /admin/usuarios/{id}/rol
PUT /admin/usuarios/{id}/activo
```

Requiere rol `SUPER_ADMIN`.

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
- `EstadoIncidenciaRequest`
- `PeticionRequest`
- `PeticionResponse`
- `EstadoPeticionRequest`
- `GrupoRequest`
- `GrupoResponse`
- `ActualizarUsuarioRequest`
- `AdminUsuarioResponse`
- `AsignarGrupoRequest`
- `AsignarRolRequest`
- `RolResponse`
- `UsuarioActivoRequest`

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

### Filtrar incidencias

```http
GET /incidencias?estado=ABIERTA
GET /incidencias?servicio=Autenticacion
GET /incidencias?categoria=Bug
GET /incidencias?grupoId=1
GET /incidencias?fechaDesde=2026-04-01
GET /incidencias?fechaHasta=2026-04-18
GET /incidencias?fechaDesde=2026-04-01&fechaHasta=2026-04-18
GET /incidencias?estado=ABIERTA&grupoId=1
```

Requiere token JWT.

Filtros disponibles:

- `estado`: valor del enum `EstadoIncidencia`.
- `servicio`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `categoria`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `grupoId`: id del grupo.
- `fechaDesde`: fecha minima de creacion en formato `yyyy-MM-dd`.
- `fechaHasta`: fecha maxima de creacion en formato `yyyy-MM-dd`.

Las incidencias devueltas siguen respetando el acceso del usuario autenticado: incidencias propias o de su grupo.

Si se envian ambas fechas, `fechaDesde` no puede ser posterior a `fechaHasta`.

### Crear peticion

```http
POST /peticiones
```

Requiere token JWT.

```json
{
  "titulo": "Solicitar nuevo acceso",
  "descripcion": "Necesito acceso al panel de reporting",
  "servicio": "Accesos",
  "categoria": "Alta de permisos",
  "grupoId": 1
}
```

Respuesta:

```json
{
  "id": 1,
  "codigoTicket": "P-20260416-0001",
  "titulo": "Solicitar nuevo acceso",
  "descripcion": "Necesito acceso al panel de reporting",
  "servicio": "Accesos",
  "categoria": "Alta de permisos",
  "estado": "ABIERTA",
  "grupo": "Desarrollo",
  "usuario": "admin2",
  "fechaCreacion": "2026-04-16T20:15:00"
}
```

### Filtrar peticiones

```http
GET /peticiones?estado=ABIERTA
GET /peticiones?servicio=Accesos
GET /peticiones?categoria=Alta%20de%20permisos
GET /peticiones?grupoId=1
GET /peticiones?fechaDesde=2026-04-01
GET /peticiones?fechaHasta=2026-04-18
GET /peticiones?fechaDesde=2026-04-01&fechaHasta=2026-04-18
GET /peticiones?estado=ABIERTA&grupoId=1
```

Requiere token JWT.

Filtros disponibles:

- `estado`: valor del enum `EstadoPeticion`.
- `servicio`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `categoria`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `grupoId`: id del grupo.
- `fechaDesde`: fecha minima de creacion en formato `yyyy-MM-dd`.
- `fechaHasta`: fecha maxima de creacion en formato `yyyy-MM-dd`.

Las peticiones devueltas siguen respetando el acceso del usuario autenticado: peticiones propias o de su grupo.

Si se envian ambas fechas, `fechaDesde` no puede ser posterior a `fechaHasta`.

### Listar grupos

```http
GET /grupos
```

Requiere token JWT.

Respuesta:

```json
[
  {
    "id": 1,
    "nombre": "Desarrollo"
  }
]
```

### Crear grupo

```http
POST /admin/grupos
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "nombre": "Soporte"
}
```

### Editar grupo

```http
PUT /admin/grupos/2
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "nombre": "Soporte Nivel 1"
}
```

Respuesta:

```json
{
  "id": 2,
  "nombre": "Soporte Nivel 1"
}
```

### Eliminar grupo

```http
DELETE /admin/grupos/2
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Si el grupo no tiene usuarios, incidencias ni peticiones asociadas, devuelve:

```http
204 No Content
```

Si el grupo tiene datos asociados, devuelve `409 Conflict` con un mensaje claro.

### Listar usuarios como administrador

```http
GET /admin/usuarios
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Respuesta:

```json
[
  {
    "id": 1,
    "username": "admin2",
    "nombre": "Admin",
    "apellidos": "Dos",
    "email": "admin2@astropi.com",
    "dni": "12345678A",
    "activo": true,
    "rol": "USER",
    "grupo": "Desarrollo"
  }
]
```

### Editar usuario como administrador

```http
PUT /admin/usuarios/5
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "username": "usuario1",
  "nombre": "Dani",
  "apellidos": "Montero",
  "email": "dani@astropi.com",
  "dni": "12345678A"
}
```

Reglas:

- `username`, `nombre`, `apellidos` y `dni` son obligatorios.
- `email` es opcional, pero si se envia debe tener formato valido.
- No se permite repetir `username`, `email` ni `dni` en otro usuario.
- Este endpoint no cambia password, rol, grupo ni estado activo.

Respuesta:

```json
{
  "id": 5,
  "username": "usuario1",
  "nombre": "Dani",
  "apellidos": "Montero",
  "email": "dani@astropi.com",
  "dni": "12345678A",
  "activo": true,
  "rol": "USER",
  "grupo": "Desarrollo"
}
```

### Listar roles como administrador

```http
GET /admin/roles
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Respuesta esperada:

```json
[
  {
    "id": 1,
    "nombre": "SUPER_ADMIN"
  },
  {
    "id": 2,
    "nombre": "USER"
  }
]
```

### Asignar grupo a usuario

```http
PUT /admin/usuarios/5/grupo
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "grupoId": 2
}
```

Respuesta:

```json
{
  "id": 5,
  "username": "usuario1",
  "nombre": "Dani",
  "apellidos": "Montero",
  "email": "usuario1@astropi.com",
  "dni": "12345678A",
  "activo": true,
  "rol": "USER",
  "grupo": "Soporte"
}
```

### Asignar rol a usuario

```http
PUT /admin/usuarios/5/rol
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "rolId": 2
}
```

Reglas de seguridad:

- Un `SUPER_ADMIN` no puede quitarse a si mismo el rol `SUPER_ADMIN`.
- No se permite dejar el sistema sin ningun `SUPER_ADMIN` activo.

Respuesta:

```json
{
  "id": 5,
  "username": "usuario1",
  "nombre": "Dani",
  "apellidos": "Montero",
  "email": "usuario1@astropi.com",
  "dni": "12345678A",
  "activo": true,
  "rol": "USER",
  "grupo": "Soporte"
}
```

### Activar o desactivar usuario

```http
PUT /admin/usuarios/5/activo
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "activo": false
}
```

Reglas de seguridad:

- Un `SUPER_ADMIN` no puede desactivar su propio usuario.
- No se permite desactivar al ultimo `SUPER_ADMIN` activo.

Respuesta:

```json
{
  "id": 5,
  "username": "usuario1",
  "nombre": "Dani",
  "apellidos": "Montero",
  "email": "usuario1@astropi.com",
  "dni": "12345678A",
  "activo": false,
  "rol": "USER",
  "grupo": "Soporte"
}
```

---

## Validaciones

### LoginRequest

Validaciones actuales:

- `username`: obligatorio, maximo 50 caracteres.
- `password`: obligatoria, entre 6 y 100 caracteres.

### RegisterRequest

Validaciones actuales:

- `username`: obligatorio, maximo 50 caracteres.
- `nombre`: obligatorio, maximo 100 caracteres.
- `apellidos`: obligatorio, maximo 150 caracteres.
- `email`: opcional, formato email valido si se envia, maximo 150 caracteres.
- `dni`: obligatorio, maximo 20 caracteres.
- `password`: obligatoria, entre 6 y 100 caracteres.

### IncidenciaRequest

Validaciones actuales:

- `titulo`: obligatorio, maximo 150 caracteres.
- `descripcion`: obligatoria, maximo 1000 caracteres.
- `servicio`: obligatorio, maximo 100 caracteres.
- `categoria`: obligatoria, maximo 100 caracteres.
- `grupoId`: obligatorio y positivo.

### EstadoIncidenciaRequest

Validaciones actuales:

- `estado`: obligatorio.
- Debe coincidir con un valor del enum `EstadoIncidencia`.

### PeticionRequest

Validaciones actuales:

- `titulo`: obligatorio, maximo 150 caracteres.
- `descripcion`: obligatoria, maximo 1000 caracteres.
- `servicio`: obligatorio, maximo 100 caracteres.
- `categoria`: obligatoria, maximo 100 caracteres.
- `grupoId`: obligatorio y positivo.

### EstadoPeticionRequest

Validaciones actuales:

- `estado`: obligatorio.
- Debe coincidir con un valor del enum `EstadoPeticion`.

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

Para peticiones existe tambien:

```text
docs/database/peticiones_estado_check.sql
```

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
- Deserializacion del estado `CERRADA` en `EstadoIncidenciaRequest`.
- Deserializacion del estado `CERRADA` en `EstadoPeticionRequest`.

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
| Backend admin usuarios | En progreso: listado, edicion, asignacion de grupo, asignacion de rol, activacion y autoproteccion implementados |
| Backend grupos | Implementado: listado, creacion, edicion y eliminacion segura |
| Backend peticiones | Implementado |
| Permisos granulares | Pendiente |
| Frontend React | Pendiente |

---

## Proximas mejoras

- Valorar paginacion en listados de incidencias y peticiones.
- Completar gestion de usuarios: eliminar si procede y revisar cambio de password.
- Revisar reglas avanzadas de grupos si el panel admin necesita mas restricciones.
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

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

El backend funciona como API REST y no usa motor de plantillas HTML.
La gestion de JWT usa la libreria `jjwt` moderna (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) sin dependencias legacy duplicadas.
Los tests usan Mockito configurado como `javaagent` en Maven Surefire para evitar avisos de self-attach en Java recientes.

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

Campos:

- `id`
- `nombre`
- `descripcion`

Estado: implementado a nivel de entidad, administracion y asignacion por rol.

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
- Filtros y paginacion en listados de incidencias.
- Cambio de estado de incidencias.
- Creacion de peticiones.
- Generacion automatica de codigo de peticion.
- Listado de peticiones por usuario.
- Listado de peticiones por usuario y grupo.
- Filtros y paginacion en listados de peticiones.
- Cambio de estado de peticiones.
- Consulta de grupos.
- Creacion de grupos desde admin.
- Listado de usuarios desde admin.
- Asignacion de grupo a usuarios desde admin.
- Asignacion de rol a usuarios desde admin.
- Activacion y desactivacion de usuarios desde admin.
- Estado `CERRADA`.
- Regla para evitar reapertura de incidencias cerradas.
- Creacion y listado de permisos granulares.
- Asignacion y retirada de permisos por rol.
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

`POST /auth/login` y `POST /auth/register` son publicos.

`GET /auth/me` requiere token JWT y sirve para comprobar el usuario autenticado.

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
GET /incidencias?page=0&size=10
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
GET /peticiones?page=0&size=10
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
GET /admin/permisos
POST /admin/permisos
DELETE /admin/permisos/{id}
PUT /admin/roles/{id}/permisos
DELETE /admin/roles/{id}/permisos/{permisoId}
PUT /admin/usuarios/{id}
PUT /admin/usuarios/{id}/grupo
PUT /admin/usuarios/{id}/rol
PUT /admin/usuarios/{id}/activo
PUT /admin/usuarios/{id}/password
DELETE /admin/usuarios/{id}
```

Requiere rol `SUPER_ADMIN`.

Tambien se permite acceso si el rol del usuario tiene el permiso granular correspondiente:

- `GESTIONAR_USUARIOS`: endpoints `/admin/usuarios/**`.
- `GESTIONAR_GRUPOS`: endpoints `/admin/grupos/**`.
- `GESTIONAR_PERMISOS`: endpoints `/admin/roles/**` y `/admin/permisos/**`.

El rol `SUPER_ADMIN` mantiene acceso total aunque no tenga permisos asignados.

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
- `CambiarPasswordUsuarioRequest`
- `PermisoRequest`
- `PermisoResponse`
- `AsignarPermisoRequest`
- `PagedResponse`
- `MensajeResponse`

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
GET /incidencias?estado=ABIERTA&page=0&size=10
```

Requiere token JWT.

Filtros disponibles:

- `estado`: valor del enum `EstadoIncidencia`.
- `servicio`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `categoria`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `grupoId`: id del grupo.
- `fechaDesde`: fecha minima de creacion en formato `yyyy-MM-dd`.
- `fechaHasta`: fecha maxima de creacion en formato `yyyy-MM-dd`.
- `page`: numero de pagina, empezando en 0.
- `size`: cantidad de elementos por pagina, entre 1 y 100.

Las incidencias devueltas siguen respetando el acceso del usuario autenticado: incidencias propias o de su grupo.

Si se envian ambas fechas, `fechaDesde` no puede ser posterior a `fechaHasta`.

Respuesta paginada:

```json
{
  "content": [
    {
      "id": 9,
      "codigoTicket": "I-20260418-0002",
      "titulo": "Prueba filtro incidencia abierta",
      "descripcion": "Incidencia creada para probar el filtro por estado ABIERTA",
      "servicio": "Autenticacion",
      "categoria": "Bug",
      "estado": "ABIERTA",
      "grupo": "Desarrollo",
      "usuario": "superadmin",
      "fechaCreacion": "2026-04-18T00:41:13.237061"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

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
GET /peticiones?estado=ABIERTA&page=0&size=10
```

Requiere token JWT.

Filtros disponibles:

- `estado`: valor del enum `EstadoPeticion`.
- `servicio`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `categoria`: coincidencia exacta sin distinguir mayusculas/minusculas.
- `grupoId`: id del grupo.
- `fechaDesde`: fecha minima de creacion en formato `yyyy-MM-dd`.
- `fechaHasta`: fecha maxima de creacion en formato `yyyy-MM-dd`.
- `page`: numero de pagina, empezando en 0.
- `size`: cantidad de elementos por pagina, entre 1 y 100.

Las peticiones devueltas siguen respetando el acceso del usuario autenticado: peticiones propias o de su grupo.

Si se envian ambas fechas, `fechaDesde` no puede ser posterior a `fechaHasta`.

Respuesta paginada:

```json
{
  "content": [
    {
      "id": 3,
      "codigoTicket": "P-20260418-0002",
      "titulo": "Solicitar nuevo acceso",
      "descripcion": "Necesito acceso al panel de reporting",
      "servicio": "Accesos",
      "categoria": "Alta de permisos",
      "estado": "ABIERTA",
      "grupo": "Desarrollo",
      "usuario": "superadmin",
      "fechaCreacion": "2026-04-18T00:46:11.265135"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

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

```json
{
  "mensaje": "Grupo borrado correctamente"
}
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
    "nombre": "SUPER_ADMIN",
    "permisos": [
      "GESTIONAR_USUARIOS"
    ]
  },
  {
    "id": 2,
    "nombre": "USER",
    "permisos": []
  }
]
```

### Listar permisos como administrador

```http
GET /admin/permisos
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Respuesta:

```json
[
  {
    "id": 1,
    "nombre": "GESTIONAR_USUARIOS",
    "descripcion": "Permite gestionar usuarios desde administracion"
  }
]
```

### Crear permiso como administrador

```http
POST /admin/permisos
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "nombre": "GESTIONAR_USUARIOS",
  "descripcion": "Permite gestionar usuarios desde administracion"
}
```

Reglas:

- `nombre` es obligatorio y no puede superar 100 caracteres.
- `descripcion` es opcional y no puede superar 255 caracteres.
- No se permite crear dos permisos con el mismo nombre.

### Eliminar permiso como administrador

```http
DELETE /admin/permisos/1
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Reglas:

- No se permite eliminar un permiso si esta asignado a algun rol.

Si el permiso se puede eliminar, devuelve:

```json
{
  "mensaje": "Permiso borrado correctamente"
}
```

### Asignar permiso a rol

```http
PUT /admin/roles/1/permisos
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "permisoId": 1
}
```

Respuesta:

```json
{
  "id": 1,
  "nombre": "SUPER_ADMIN",
  "permisos": [
    "GESTIONAR_USUARIOS"
  ]
}
```

### Quitar permiso a rol

```http
DELETE /admin/roles/1/permisos/1
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Respuesta:

```json
{
  "id": 1,
  "nombre": "SUPER_ADMIN",
  "permisos": []
}
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

### Cambiar password de usuario como administrador

```http
PUT /admin/usuarios/5/password
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

```json
{
  "password": "nuevaPassword123"
}
```

Reglas:

- `password` es obligatoria.
- Debe tener entre 6 y 100 caracteres.
- La password se guarda encriptada con BCrypt.
- La respuesta nunca devuelve la password.

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

### Eliminar usuario como administrador

```http
DELETE /admin/usuarios/5
```

Requiere token JWT de usuario con rol `SUPER_ADMIN`.

Reglas de seguridad:

- Un `SUPER_ADMIN` no puede eliminar su propio usuario.
- No se permite eliminar el ultimo `SUPER_ADMIN` activo.
- No se permite eliminar usuarios con incidencias asociadas.
- No se permite eliminar usuarios con peticiones asociadas.

Si el usuario se puede eliminar, devuelve:

```json
{
  "mensaje": "Usuario borrado correctamente"
}
```

Si el usuario tiene tickets asociados, devuelve `409 Conflict` con un mensaje claro.

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

### Parametros de URL

Si un parametro de URL no tiene el formato correcto, la API devuelve un error claro.

Ejemplos:

```http
GET /incidencias?estado=INVALIDO
GET /incidencias?fechaDesde=18-04-2026
GET /incidencias?page=abc
```

Respuesta para un estado no valido:

```json
{
  "error": "Parametro invalido",
  "mensaje": "Valor no permitido para estado. Valores permitidos: ABIERTA, EN_PROCESO, PARADA, RESUELTA, CERRADA"
}
```

Respuesta para una fecha no valida:

```json
{
  "error": "Parametro invalido",
  "mensaje": "Formato no valido para fechaDesde. Usa yyyy-MM-dd"
}
```

Respuesta para una pagina no numerica:

```json
{
  "error": "Parametro invalido",
  "mensaje": "El parametro page debe ser numerico"
}
```

---

## PostgreSQL y DBeaver

La base de datos usada es PostgreSQL y se gestiona durante el desarrollo con DBeaver.

Configuracion actual:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/astroPI_DB
spring.datasource.username=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

Nota importante:

Ahora el esquema se gestiona con Flyway. La migracion inicial esta en:

```text
src/main/resources/db/migration/V1__init_schema.sql
```

Datos base adicionales:

```text
src/main/resources/db/migration/V2__seed_base_data.sql
src/main/resources/db/migration/V3__seed_superadmin.sql
src/main/resources/db/migration/V4__tighten_core_constraints.sql
src/main/resources/db/migration/V5__add_query_indexes.sql
```

Comportamiento previsto:

- Base vacia: Flyway ejecuta `V1__init_schema.sql` y crea el esquema inicial.
- Base ya existente sin historial de Flyway: se aplica `baseline-on-migrate` para no romper el entorno actual y despues se ejecutan migraciones nuevas como `V2`.

`ddl-auto=validate` se mantiene para que Hibernate compruebe que el esquema real sigue alineado con las entidades.

La `V2` deja sembrados grupos y permisos base del proyecto:

- Grupos: `Administradores`, `Desarrollo`, `Contabilidad`
- Permisos: `GESTIONAR_USUARIOS`, `GESTIONAR_GRUPOS`, `GESTIONAR_PERMISOS`

La `V3` crea un usuario inicial `superadmin` solo si todavia no existe:

- `username`: `superadmin`
- `password`: `admin123`
- `rol`: `SUPER_ADMIN`
- `grupo`: `Administradores` si existe, o `Sistemas IT` como respaldo

Conviene cambiar esa password inicial despues del primer acceso.

La `V4` endurece restricciones del esquema en tablas base:

- `roles.nombre` unico y obligatorio
- `grupos.nombre` unico y obligatorio
- `usuarios.username` unico y obligatorio
- `usuarios.dni` unico y obligatorio
- `usuarios.email` unico si se informa
- `usuarios.password`, `nombre`, `apellidos`, `activo`, `rol_id` y `grupo_id` obligatorios

La `V5` anade indices para consultas frecuentes:

- `usuarios(grupo_id)`
- `usuarios(rol_id, activo)`
- `incidencias(usuario_id)`, `incidencias(grupo_id)`, `incidencias(estado)`, `incidencias(fecha_creacion)`
- `peticiones(usuario_id)`, `peticiones(grupo_id)`, `peticiones(estado)`, `peticiones(fecha_creacion)`

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

Configuracion JWT actual:

```properties
app.jwt.secret=astropi_super_secret_key_very_secure_2026_1234567890
app.jwt.expiration-ms=86400000
```

`app.jwt.expiration-ms` esta en milisegundos. El valor `86400000` equivale a 24 horas.

En un entorno real, `app.jwt.secret` deberia moverse a una variable de entorno o a un sistema seguro de secretos.

Configuracion CORS actual para el futuro frontend:

```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

Esto permite llamadas desde React con Vite (`5173`) o Create React App (`3000`) durante desarrollo.

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
- Regla de negocio que impide reabrir incidencias cerradas.
- Regla de negocio que impide reabrir peticiones cerradas.
- Regla de validacion que impide usar un rango de fechas invalido en incidencias.
- Regla de validacion que impide usar un rango de fechas invalido en peticiones.
- Regla de validacion que impide usar una pagina negativa en incidencias y peticiones.
- Regla de validacion que impide usar un tamano de pagina fuera de rango en incidencias y peticiones.
- Reglas de seguridad HTTP del panel admin para acceso no autenticado, sin permiso y con permiso correcto en usuarios, grupos y permisos.
- Validaciones HTTP del panel admin para bodies invalidos y campos no permitidos en grupos y permisos.
- Validaciones HTTP del panel admin para edicion de usuarios y cambio de password.
- Flujos HTTP de autenticacion para login correcto, credenciales invalidas, usuario desactivado y acceso a /auth/me.
- Validaciones HTTP de autenticacion para login y register con campos obligatorios, formato invalido y campos no permitidos.
- Regla de seguridad que impide a un administrador desactivar su propio usuario.
- Regla de seguridad que impide a un `SUPER_ADMIN` quitarse su propio rol.
- Regla de seguridad que impide eliminar al ultimo `SUPER_ADMIN` activo.
- Regla de seguridad que impide eliminar usuarios con incidencias asociadas.
- Regla de seguridad que impide eliminar usuarios con peticiones asociadas.
- Regla de seguridad que impide eliminar grupos con usuarios asociados.
- Regla de seguridad que impide eliminar grupos con incidencias asociadas.
- Regla de seguridad que impide eliminar grupos con peticiones asociadas.
- Regla de negocio que impide crear permisos duplicados.
- Regla de negocio que impide eliminar permisos asignados a roles.
- Asignacion y retirada de permisos en roles.

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
| Filtros y paginacion de tickets | Implementado |
| Backend usuarios | En progreso |
| Backend admin usuarios | Implementado: listado, edicion, asignacion de grupo, asignacion de rol, activacion, cambio de password, eliminacion segura y autoproteccion |
| Backend grupos | Implementado: listado, creacion, edicion y eliminacion segura |
| Backend peticiones | Implementado |
| Permisos granulares | Implementado: entidad, listado, creacion, eliminacion y asignacion por rol |
| Frontend React | Pendiente |

---

## Proximas mejoras

- Revisar reglas avanzadas de grupos si el panel admin necesita mas restricciones.
- Aplicar permisos granulares a reglas concretas de negocio si el panel admin lo requiere.
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

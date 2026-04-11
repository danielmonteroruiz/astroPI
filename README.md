__________________________________________________________________________
🚀 AstroPI - Sistema de Gestión de Incidencias y Peticiones

AstroPI es una aplicación backend desarrollada con Spring Boot que proporciona una API REST para la gestión de incidencias y peticiones, inspirada en sistemas tipo HelpDesk o Jira.
El proyecto está diseñado para ser consumido por un frontend (por ejemplo, React), ofreciendo una arquitectura escalable, segura y alineada con buenas prácticas profesionales.
__________________________________________________________________________
🧠 Descripción

AstroPI implementa un backend completo que permite:
Autenticación de usuarios mediante JWT
Creación y gestión de incidencias
Control de acceso por usuario y grupo
Generación automática de identificadores de ticket
Protección de datos mediante uso de DTOs
El sistema está preparado para integrarse con un frontend que proporcionará una interfaz visual intuitiva para los usuarios finales.
__________________________________________________________________________
🏗️ Arquitectura

El sistema sigue una arquitectura en capas:
Controller → Exposición de endpoints REST
Service → Lógica de negocio
Repository → Acceso a base de datos
Model → Entidades JPA
DTO → Transferencia segura de datos
🔗 Estructura general
Frontend (React - futuro)
↓
Backend (Spring Boot API)
↓
Base de datos (PostgreSQL)

__________________________________________________________________________
🔐 Seguridad

Autenticación mediante JWT
Sistema stateless (sin sesiones)
Contraseñas encriptadas con BCrypt
Uso de DTOs para evitar exponer información sensible (como contraseñas)
__________________________________________________________________________
🎯 Objetivo del proyecto

Construir un sistema completo de gestión de tickets que permita:
Registro y autenticación de usuarios
Creación y gestión de incidencias y peticiones
Control de acceso basado en grupos
Gestión avanzada por roles (Super Admin)
__________________________________________________________________________
👤 Gestión de usuarios

🔑 Autenticación
Login con username y contraseña
Generación de token JWT
📝 Registro
Username
Nombre
Apellidos
Email
DNI
Contraseña
👉 Tras el registro, un Super Admin asigna el grupo correspondiente.
__________________________________________________________________________
🧾 Sistema de tickets

El sistema gestiona dos tipos de tickets:
Incidencias
Peticiones (pendiente de implementación)
Cada ticket incluye:
Código automático:
I-YYYYMMDD-0001
P-YYYYMMDD-0001
Título (asunto)
Descripción
Servicio
Categoría
Fecha de creación automática
Grupo destino
__________________________________________________________________________
🔐 Control de acceso

Cada usuario pertenece a un grupo
Un usuario puede ver:
Sus propios tickets
Tickets de su grupo
__________________________________________________________________________
👑 Rol Super Admin

Usuarios con este rol pueden:
👤 Usuarios
Crear y eliminar usuarios
Asignar grupos y permisos
👥 Grupos
Crear y eliminar grupos
Asignar usuarios
🔐 Permisos
Modificar permisos existentes
Asignar permisos a usuarios o grupos
__________________________________________________________________________
⚙️ Funcionalidades implementadas

Autenticación con JWT
Endpoint /auth/me
Creación de incidencias
Generación automática de código de ticket
Listado de incidencias:
Por usuario
Por grupo
Uso de DTOs para respuestas seguras
__________________________________________________________________________
📡 Endpoints principales

🔐 Autenticación
POST /auth/login
GET /auth/me
__________________________________________________________________________
🛠️ Incidencias

POST /incidencias
GET /incidencias/mis-incidencias
GET /incidencias

__________________________________________________________________________
🧪 Ejemplo de uso

📥 Crear incidencia
{
"titulo": "Error login",
"descripcion": "No puedo acceder",
"servicio": "Autenticación",
"categoria": "Bug",
"grupoId": 1
}

__________________________________________________________________________
📤 Respuesta

{
"id": 1,
"codigoTicket": "I-20260411-0001",
"titulo": "Error login",
"descripcion": "No puedo acceder",
"estado": "ABIERTA",
"usuario": "usuario1",
"grupo": "Sistemas IT",
"fechaCreacion": "2026-04-11T10:00:00"
}
__________________________________________________________________________

⚙️ Instalación y ejecución

Clonar el repositorio:
git clone https://github.com/tu-usuario/astropi.git

Configurar base de datos en application.properties
Ejecutar la aplicación:
./mvnw spring-boot:run
__________________________________________________________________________

📌 Estado del proyecto

🟢 En desarrollo activo
Actualmente incluye:
Backend funcional con JWT
Módulo de incidencias operativo
Arquitectura limpia y escalable
__________________________________________________________________________
🚀 Próximas mejoras

Cierre de incidencias (PUT)
Filtros avanzados (estado, fecha)
Módulo de peticiones
Validaciones con Bean Validation
Gestión completa de usuarios, grupos y permisos
Desarrollo del frontend en React
__________________________________________________________________________
👨‍💻 Autor

Proyecto desarrollado por Daniel Montero Ruiz como parte de aprendizaje en desarrollo backend con Spring Boot.


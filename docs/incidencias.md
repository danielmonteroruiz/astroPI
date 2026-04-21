# Bloque de Incidencias

## Estado actual

El bloque de incidencias permite:

- Crear incidencias autenticadas.
- Listar incidencias propias.
- Listar incidencias propias y del grupo del usuario con filtros y paginacion.
- Cambiar el estado de una incidencia.
- Generar codigo automatico con formato `I-YYYYMMDD-0001`.
- Mantener numeracion continua aunque cambie el dia.
- Validar los datos de entrada con Bean Validation.
- Proteger los endpoints con JWT.

## Endpoints

### Crear incidencia

```http
POST /incidencias
```

Body:

```json
{
  "titulo": "Error al iniciar sesion",
  "descripcion": "El usuario no puede acceder a la aplicacion",
  "servicio": "Autenticacion",
  "categoria": "Bug",
  "grupoId": 1
}
```

### Ver mis incidencias

```http
GET /incidencias/mis-incidencias
```

### Ver incidencias accesibles por usuario y grupo

```http
GET /incidencias
```

Filtros disponibles:

- `estado`
- `servicio`
- `categoria`
- `grupoId`
- `fechaDesde`
- `fechaHasta`
- `page`
- `size`

### Cambiar estado

```http
PUT /incidencias/{id}/estado
```

Body:

```json
{
  "estado": "CERRADA"
}
```

Estados permitidos:

- `ABIERTA`
- `EN_PROCESO`
- `PARADA`
- `RESUELTA`
- `CERRADA`

## Seguridad

Todos los endpoints de incidencias requieren JWT.

En Postman:

- Authorization: `Bearer Token`
- Token: pegar solo el JWT, sin escribir `Bearer` delante.

## Validaciones

`IncidenciaRequest` valida:

- `titulo`: obligatorio, maximo 150 caracteres.
- `descripcion`: obligatoria, maximo 1000 caracteres.
- `servicio`: obligatorio, maximo 100 caracteres.
- `categoria`: obligatoria, maximo 100 caracteres.
- `grupoId`: obligatorio y positivo.

`EstadoRequest` valida:

- `estado`: obligatorio y debe coincidir con un valor de `EstadoIncidencia`.

Reglas funcionales:

- Una incidencia `CERRADA` no puede volver a un estado anterior.
- `fechaDesde` no puede ser posterior a `fechaHasta`.
- `page` no puede ser negativo.
- `size` debe estar entre `1` y `100`.

## Nota para DBeaver y PostgreSQL

Si se anade un nuevo estado al enum Java, PostgreSQL puede mantener una constraint antigua en la tabla `incidencias`.

Ejemplo real:

```text
ERROR: el nuevo registro para la relacion "incidencias" viola la restriccion "incidencias_estado_check"
```

Esto ocurrio cuando el proyecto todavia dependia de cambios automaticos sobre el esquema y PostgreSQL mantuvo una constraint antigua.

Ahora el proyecto usa Flyway y `spring.jpa.hibernate.ddl-auto=validate`, pero este SQL sigue siendo util si necesitas corregir una base local antigua que arrastras de versiones previas.

En bases nuevas, la constraint correcta ya queda cubierta por la migracion inicial `V1__init_schema.sql`.

En DBeaver, abrir un SQL Editor sobre la base de datos `astroPI_DB` y ejecutar:

```sql
ALTER TABLE incidencias
DROP CONSTRAINT IF EXISTS incidencias_estado_check;

ALTER TABLE incidencias
ADD CONSTRAINT incidencias_estado_check
CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'));
```

Despues de ejecutar el SQL, probar de nuevo:

```json
{
  "estado": "CERRADA"
}
```

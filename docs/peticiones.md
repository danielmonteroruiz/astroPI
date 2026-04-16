# Bloque de Peticiones

## Estado actual

El bloque de peticiones permite:

- Crear peticiones autenticadas.
- Listar peticiones propias.
- Listar peticiones propias y del grupo del usuario.
- Cambiar el estado de una peticion.
- Generar codigo automatico con formato `P-YYYYMMDD-0001`.
- Validar los datos de entrada con Bean Validation.
- Proteger los endpoints con JWT.

## Endpoints

### Crear peticion

```http
POST /peticiones
```

Body:

```json
{
  "titulo": "Solicitar nuevo acceso",
  "descripcion": "Necesito acceso al panel de reporting",
  "servicio": "Accesos",
  "categoria": "Alta de permisos",
  "grupoId": 1
}
```

### Ver mis peticiones

```http
GET /peticiones/mis-peticiones
```

### Ver peticiones accesibles por usuario y grupo

```http
GET /peticiones
```

### Cambiar estado

```http
PUT /peticiones/{id}/estado
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

Todos los endpoints de peticiones requieren JWT.

En Postman:

- Authorization: `Bearer Token`
- Token: pegar solo el JWT, sin escribir `Bearer` delante.

## Validaciones

`PeticionRequest` valida:

- `titulo`: obligatorio, maximo 150 caracteres.
- `descripcion`: obligatoria, maximo 1000 caracteres.
- `servicio`: obligatorio, maximo 100 caracteres.
- `categoria`: obligatoria, maximo 100 caracteres.
- `grupoId`: obligatorio y positivo.

`EstadoPeticionRequest` valida:

- `estado`: obligatorio y debe coincidir con un valor de `EstadoPeticion`.

## Nota para DBeaver y PostgreSQL

Si PostgreSQL crea o mantiene una constraint de estados antigua, revisar la tabla `peticiones` en DBeaver.

Los estados esperados son:

```text
ABIERTA, EN_PROCESO, PARADA, RESUELTA, CERRADA
```

# BCNC Catalog

Microservicio Spring Boot para consultar el precio aplicable de un producto en una cadena y fecha determinadas, siguiendo un enfoque **API-first** con **OpenAPI** como contrato fuente de verdad y una arquitectura **hexagonal (ports & adapters)**.

## Tecnologías

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- H2 en memoria
- OpenAPI Generator (spring generator)
- springdoc-openapi (Swagger UI)
- ArchUnit (test de arquitectura)
- JaCoCo (cobertura)
- Maven

## Arquitectura

El proyecto sigue el patrón **hexagonal (ports & adapters)**, separando el dominio del resto de infraestructura:

- `domain` — modelo de dominio (`Price`, `AuthCredential`, `AuthenticatedUser`) y excepciones (`DomainException`, `PriceNotFoundException`, `AuthenticationFailedException`). No depende de frameworks.
- `application` — casos de uso (`FindApplicablePriceUseCase`, `AuthenticateUseCase`), servicios de aplicación, DTOs y **puertos** (`port.in`, `port.out`).
- `adapter.in.rest` — controladores REST (`PriceController`, `AuthenticationController`), filtro de autenticación (`BearerTokenAuthenticationFilter`), manejo global de errores (`GlobalExceptionHandler`) y mapeadores. Implementa las interfaces generadas por OpenAPI (`PricesApi`, `AuthenticationApi`).
- `adapter.out.persistence` — adaptadores JPA (entidades, repositorios, mappers) para precios y credenciales.
- `adapter.out.security` — verificación de credenciales (`SimpleCredentialVerificationAdapter`) y generación de tokens HMAC (`HmacTokenProvider`).
- `config` — configuración de seguridad, OpenAPI, Jackson, carga de datos iniciales y propiedades.
- `auth` — submódulo de autenticación con su propio dominio/aplicación (`auth.domain`, `auth.application`).

Las dependencias entre capas están protegidas por **ArchUnit** en [`ArchitectureTest`](src/test/java/com/bcnc/ecomerce/catalog/architecture/ArchitectureTest.java):

- Los controladores (`adapter.in.rest`) solo dependen de `application`/`domain`.
- Los servicios (`application.service`) solo dependen de `application`/`domain`.
- El dominio no depende de frameworks (Spring/Jakarta).
- Los adaptadores de persistencia y seguridad no dependen de los adaptadores REST.
- Los puertos no dependen de los adaptadores.
- Los DTOs residen en `application`; las entidades en `adapter.out.persistence.entity`; los repositorios en `adapter.out.persistence`.

## Contrato API

El contrato OpenAPI está en:

- `src/main/resources/openapi/price-api.yaml`

Desde ese fichero se genera automáticamente (en fase `generate-sources`) la interfaz `PricesApi`, `AuthenticationApi` y los modelos REST usados por la implementación, mediante el plugin `openapi-generator-maven-plugin`.

## Endpoints

### Precios (protegido)

- `GET /api/v1/prices`

Query params requeridos:

- `applicationDate`: fecha/hora de aplicación (formato RFC 3339, p. ej. `2020-06-14T10:00:00Z`)
- `productId`: identificador del producto
- `chainId`: identificador de la cadena (brand)

Respuesta `200` con:

- `productId`
- `chainId`
- `priceList`
- `startDate` = `START_DATE`
- `endDate` = `END_DATE`
- `price`
- `currency`

### Autenticación

La API de precios está protegida con un **Bearer token** (esquema `bearerAuth` en OpenAPI). El flujo es **stateless** (sin sesiones) y usa BCrypt para las contraseñas y tokens HMAC firmados.

#### Generar token

- `POST /api/v1/auth/token`

Cuerpo (`TokenRequest`):

```json
{
  "clientKey": "BCNC-CLIENT",
  "username": "catalog-user",
  "password": "catalog-password"
}
```

Respuesta `200` (`TokenResponse`):

```json
{
  "accessToken": "<token>",
  "refreshToken": "<refresh-token>",
  "tokenType": "Bearer",
  "expiresAt": "2026-07-16T18:50:00Z",
  "refreshExpiresAt": "2026-07-17T18:50:00Z"
}
```

#### Renovar token

- `POST /api/v1/auth/refresh`

Cuerpo (`RefreshTokenRequest`):

```json
{
  "refreshToken": "<refresh-token>"
}
```

Devuelve un nuevo par `accessToken`/`refreshToken`. El refresh token solo sirve para renovación y no debe usarse para acceder a endpoints de negocio.

### Credenciales simples por defecto

- `clientKey`: `BCNC-CLIENT`
- `username`: `catalog-user`
- `password`: `catalog-password`

> Estas credenciales están pensadas para entorno local/demo y se cargan al inicio desde `src/main/resources/data/auth-credentials.json` hacia la BD en memoria, igual que los precios.

### Origen de datos de autenticación

Las credenciales de autenticación se almacenan en:

- `src/main/resources/data/auth-credentials.json`

Y se cargan automáticamente al arrancar mediante un `DataLoader` (`AuthCredentialDataLoader`).

### Flujo de uso

1. Llamar a `POST /api/v1/auth/token`
2. Obtener `accessToken` (y opcionalmente `refreshToken`)
3. Invocar `GET /api/v1/prices` enviando:
   - `Authorization: Bearer <accessToken>`
4. (Opcional) Renovar con `POST /api/v1/auth/refresh` usando el `refreshToken`

### Configuración de seguridad

Definida en [`SecurityConfig`](src/main/java/com/bcnc/ecomerce/catalog/config/SecurityConfig.java):

- CSRF deshabilitado, sesiones **stateless**.
- Filtro `BearerTokenAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
- Rutas públicas: `/api/v1/auth/token`, `/api/v1/auth/refresh`, Swagger UI, `/v3/api-docs`, `/h2-console`, `/error`.
- `/api/v1/prices/**` requiere autenticación.
- `RestAuthenticationEntryPoint` devuelve `401` ante peticiones no autenticadas.

La validez de los tokens se configura en `application.yaml` bajo `catalog.security.auth`:

- `token-secret`
- `token-validity-minutes` (por defecto 60)
- `refresh-token-validity-minutes` (por defecto 10080)

## Ejemplo funcional - Test 1

Petición a las **10:00 del día 14** para el producto `35455` de la brand `1` (`ZARA`), usando formato de fecha **RFC 3339**:

Primero generar token:

```powershell
$tokenResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/token" -ContentType "application/json" -Body '{"clientKey":"BCNC-CLIENT","username":"catalog-user","password":"catalog-password"}'
$token = $tokenResponse.accessToken
```

Luego consultar el endpoint protegido:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T10:00:00Z&productId=35455&chainId=1" -Headers @{ Authorization = "Bearer $token" }
```

Resultado esperado:

- `productId`: `35455`
- `chainId`: `1`
- `priceList`: `1`
- `startDate`: `2020-06-14T00:00:00Z` (`START_DATE`)
- `endDate`: `2020-12-31T23:59:59Z` (`END_DATE`)
- `price`: `35.50`
- `currency`: `EUR`

## Ejecutar en local

```powershell
.\mvnw.cmd spring-boot:run
```

La consola H2 está disponible en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:catalog`).

## Ejecutar pruebas

```powershell
.\mvnw.cmd test
```

Incluye:

- Pruebas unitarias de servicios y mappers.
- Pruebas de integración REST (`PriceControllerIntegrationTest`, `AuthenticationControllerIntegrationTest`).
- Pruebas de arquitectura con ArchUnit (`ArchitectureTest`).
- Cobertura con JaCoCo (informe en `target/site/jacoco/index.html`).

## Ejemplo de llamada

```powershell
# 1. Obtener token
$tokenResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/token" -ContentType "application/json" -Body '{"clientKey":"BCNC-CLIENT","username":"catalog-user","password":"catalog-password"}'
$token = $tokenResponse.accessToken

# 2. Consultar precio con el token
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00Z&productId=35455&chainId=1" -Headers @{ Authorization = "Bearer $token" }

# 3. (Opcional) Renovar token
$refreshResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/refresh" -ContentType "application/json" -Body "{`"refreshToken`":`"$($tokenResponse.refreshToken)`"}"
```

## Documentación interactiva

- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Cobertura funcional validada

Se han validado mediante pruebas de integración:

- Generación de token con credenciales válidas
- Rechazo de credenciales inválidas
- Rechazo de acceso sin token
- Rechazo de token malformado
- Renovación de token con refresh token válido
- Los 5 escenarios clásicos de selección de tarifa
- El caso `404` cuando no existe precio aplicable
- El caso `400` por parámetro inválido
- El caso `400` por parámetro obligatorio ausente

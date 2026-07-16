# Revisión Arquitectónica - BCNC Catalog

## Resumen Ejecutivo

Se revisó el proyecto BCNC Catalog (microservicio Spring Boot para consulta de precios) evaluando 9 criterios arquitectónicos. Se detectaron **violaciones significativas** en eficiencia de extracción de datos, claridad de código (duplicación de paquetes), adherencia a SOLID (SRP), y mejoras necesarias en README y testing.

---

## 1. Eficiencia de la Extracción de Datos

### Violaciones Detectadas

| # | Archivo | Problema | Severidad |
|---|---------|----------|-----------|
| 1 | [`PriceDataLoader.java`](src/main/java/com/bcnc/ecomerce/catalog/config/PriceDataLoader.java:36) | Inserta registros uno por uno con `save()` en un bucle. Debería usar `saveAll()` para inserción por lotes. | Alta |
| 2 | [`PriceRepository.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/out/persistence/repository/PriceRepository.java:19) | La consulta `findApplicablePrices` recupera **todos** los candidatos y los ordena por prioridad DESC, pero el servicio solo necesita el primero. Falta `setMaxResults(1)` para evitar traer datos innecesarios. | Alta |
| 3 | [`PricePersistenceMapper.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/out/persistence/mapper/PricePersistenceMapper.java:27) | `toDomainList()` mapea **todos** los entities aun cuando solo se usa el primer elemento. | Media |

### Recomendación

```java
// PriceRepository - agregar límite
@Query("""
    SELECT p FROM PriceEntity p
    WHERE p.brandId = :brandId
      AND p.productId = :productId
      AND p.startDate <= :applicationDate
      AND p.endDate >= :applicationDate
    ORDER BY p.priority DESC
    """)
List<PriceEntity> findApplicablePrices(...);

// En el servicio, usar:
PriceEntity entity = priceRepository.findApplicablePrices(...).stream().findFirst()
    .orElseThrow(() -> new PriceNotFoundException(...));
```

---

## 2. Claridad de Código

### Violaciones Detectadas

| # | Archivo | Problema | Severidad |
|---|---------|----------|-----------|
| 1 | Paquete `com.bcnc.ecomerce.catalog.auth.*` | Existe un **paquete duplicado** `auth/` que contiene código espejo del paquete raíz. El `AuthenticationController` importa desde `auth.*`, pero hay archivos legacy en el paquete raíz que no se usan. Esto genera confusión sobre cuál es la fuente de verdad. | Alta |
| 2 | [`PriceDataLoader.java`](src/main/java/com/bcnc/ecomerce/catalog/config/PriceDataLoader.java:1) | Mezcla dos responsabilidades: carga de precios **y** carga de credenciales de autenticación. Debería separarse en `PriceDataLoader` y `AuthCredentialDataLoader`. | Alta |
| 3 | [`AuthCredentialDataLoader.java`](src/main/java/com/bcnc/ecomerce/catalog/config/AuthCredentialDataLoader.java:1) | Archivo **vacío** (0 bytes). Esto es confuso — ¿está en desarrollo? ¿debería eliminarse? | Media |
| 4 | [`Price.java`](src/main/java/com/bcnc/ecomerce/catalog/domain/model/Price.java:1) | Modelo de dominio **mutable** con setters. En DDD, los modelos de dominio deberían ser inmutables o tener mutación controlada. | Media |
| 5 | [`PriceController.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/in/rest/PriceController.java:52) | Mapeo manual de `Price` → `PriceResponse` en el controlador. Debería extraerse a un `PriceRestMapper` dedicado. | Baja |

### Recomendación

- Eliminar el paquete duplicado `auth/` o consolidar el código en una sola ubicación.
- Separar `PriceDataLoader` y `AuthCredentialDataLoader`.
- Eliminar el archivo vacío `AuthCredentialDataLoader.java` o implementarlo.
- Considerar convertir `Price` en un `record` o hacerlo inmutable.

---

## 3. Endpoint GET con Buenas Prácticas

### Estado Actual

| Aspecto | Evaluación |
|---------|------------|
| Uso de query params en GET | ✅ Correcto |
| Documentación OpenAPI | ✅ Buena, con ejemplos y descripciones |
| Validación de parámetros | ⚠️ Parcial — usa `@Validated` pero faltan anotaciones explícitas como `@RequestParam` y validadores de rango |
| Códigos de respuesta | ✅ 200, 400, 401, 404 documentados |
| Seguridad | ✅ Bearer token correctamente implementado |

### Violaciones Detectadas

| # | Archivo | Problema | Severidad |
|---|---------|----------|-----------|
| 1 | [`PriceController.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/in/rest/PriceController.java:26) | Falta anotación `@Operation` de OpenAPI en el método (solo hay `@Parameter` en los args). El método debería tener `@Operation(summary = ..., description = ...)`. | Baja |
| 2 | [`PriceController.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/in/rest/PriceController.java:46) | Convierte `OffsetDateTime` a `LocalDateTime` con `toLocalDateTime()`, descartando información de zona horaria. Si el cliente envía una fecha con offset diferente a UTC, podría haber inconsistencias. | Media |

### Recomendación

```java
@GetMapping
@Operation(summary = "Get applicable price", description = "...")
public ResponseEntity<PriceResponse> getApplicablePrice(
    @Parameter(...) @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime applicationDate,
    @Parameter(...) @RequestParam @NotNull @Min(1) Long productId,
    @Parameter(...) @RequestParam @NotNull @Min(1) Long chainId) {
    // ...
}
```

---

## 4. README

### Violaciones Detectadas

| # | Sección | Problema | Severidad |
|---|---------|----------|-----------|
| 1 | Línea 118 | Ejemplo de llamada **sin autenticación** (`Invoke-RestMethod` sin header `Authorization`), pero el endpoint requiere token. | Alta |
| 2 | Líneas 78, 96 | Terminología inconsistente: usa "brand" en algunos lugares y "chain" en otros. El API usa `chainId`. | Media |
| 3 | General | Falta diagrama de arquitectura o explicación del enfoque hexagonal/DDD. | Media |
| 4 | General | Falta sección de "Cómo contribuir" o flujo de desarrollo. | Baja |
| 5 | General | No explica cómo ver la cobertura de tests (JaCoCo). | Baja |

### Recomendación

- Corregir el ejemplo sin autenticación.
- Unificar terminología: usar "commercial chain (chainId)" consistentemente.
- Agregar sección de arquitectura.
- Agregar instrucciones de testing con cobertura.

---

## 5. SOLID

### Análisis por Principio

| Principio | Evaluación | Detalle |
|-----------|------------|---------|
| **S** - Single Responsibility | ⚠️ Violación | `PriceDataLoader` carga precios y credenciales. `GlobalExceptionHandler` maneja 7 tipos de excepción (aceptable pero podría modularizarse). |
| **O** - Open/Closed | ✅ | Uso de interfaces (`FindApplicablePriceUseCase`, `AuthenticateUseCase`) permite extender sin modificar. |
| **L** - Liskov Substitution | ✅ | Las implementaciones de puertos son sustituibles. |
| **I** - Interface Segregation | ⚠️ | `AuthenticateUseCase` agrupa `authenticate` y `refreshAccessToken`. Si tienen clientes diferentes, podrían separarse. |
| **D** - Dependency Inversion | ✅ | Excelente uso de puertos (ports) y adaptadores (adapters). La lógica de aplicación no depende de frameworks. |

### Recomendación

- Separar `PriceDataLoader` y `AuthCredentialDataLoader`.
- Considerar segregar `AuthenticateUseCase` en `AuthenticationUseCase` y `RefreshTokenUseCase` si los casos de uso tienen ciclos de vida distintos.

---

## 6. Eficiencia

### Violaciones Detectadas

| # | Archivo | Problema | Severidad |
|---|---------|----------|-----------|
| 1 | [`PriceDataLoader.java`](src/main/java/com/bcnc/ecomerce/catalog/config/PriceDataLoader.java:36) | Inserción uno por uno (`save()` en bucle) en lugar de `saveAll()`. | Alta |
| 2 | [`PriceRepository.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/out/persistence/repository/PriceRepository.java:19) | Consulta sin límite superior — recupera todos los candidatos cuando solo se necesita el de mayor prioridad. | Alta |
| 3 | [`PricePersistenceMapper.java`](src/main/java/com/bcnc/ecomerce/catalog/adapter/out/persistence/mapper/PricePersistenceMapper.java:27) | Mapeo completo de lista cuando solo se usa el primer elemento. | Media |

---

## 7. Testing

### Estado Actual

| Tipo | Cobertura | Evaluación |
|------|-----------|------------|
| Unit tests | `FindApplicablePriceServiceTest`, `AuthenticationServiceTest`, mappers | ✅ Bueno |
| Integration tests | `PriceControllerIntegrationTest`, `AuthenticationControllerIntegrationTest` | ✅ Bueno |
| ArchUnit | Dependencia en `pom.xml` pero **sin tests implementados** | ⚠️ Desperdiciada |
| Coverage | JaCoCo configurado | ✅ |

### Violaciones Detectadas

| # | Archivo | Problema | Severidad |
|---|---------|----------|-----------|
| 1 | `pom.xml` | ArchUnit está en dependencias pero no hay tests que lo usen para validar reglas arquitectónicas (ej: "los controladores no deben acceder a repositorios directamente"). | Media |
| 2 | [`PriceControllerIntegrationTest.java`](src/test/java/com/bcnc/ecomerce/catalog/adapter/in/rest/PriceControllerIntegrationTest.java:16) | Usa `@SpringBootTest` (arranca todo el contexto) para tests de controlador. Podría usar `@WebMvcTest` para pruebas más rápidas y aisladas. | Baja |

### Recomendación

- Implementar tests de ArchUnit para validar:
  - Controladores solo dependen de casos de uso (puertos de entrada).
  - Servicios no dependen de controladores.
  - Entidades JPA están en el paquete `adapter.out.persistence.entity`.
- Considerar `@WebMvcTest` para `PriceControllerIntegrationTest`.

---

## 8. Claridad de Código (repetido)

Ver sección 2.

---

## 9. Control de Versiones

### Estado Actual

| Aspecto | Evaluación |
|---------|------------|
| `.gitignore` | ✅ Presente, cubre IDEs, builds, Maven wrapper |
| `.gitattributes` | ✅ Presente, configura line endings para mvnw |
| `pom.xml` metadata | ⚠️ Campos vacíos: `<name/>`, `<description/>`, `<url/>`, `<licenses>`, `<developers>`, `<scm>` |

### Recomendación

- Completar metadata en `pom.xml` con información del proyecto.

---

## Arquitectura General

El proyecto sigue un enfoque **Hexagonal / DDD** con buena separación de capas:

```
adapter/in/rest      → Controladores REST (API-first con OpenAPI)
application/port/in  → Puertos de entrada (casos de uso)
application/service  → Lógica de aplicación
domain/model         → Modelos de dominio
domain/exception     → Excepciones de dominio
adapter/out/         → Adaptadores de salida (JPA, seguridad)
```

**Puntos fuertes:**
- Uso de puertos y adaptadores correctamente.
- OpenAPI como contrato fuente de verdad.
- Manejo de excepciones centralizado con `@RestControllerAdvice`.
- Autenticación basada en filtros personalizados.

**Puntos débiles:**
- Duplicación de paquetes `auth/` que genera confusión.
- `PriceDataLoader` con doble responsabilidad.
- Inserción de datos uno por uno.
- Consulta sin límite superior.

---

## Plan de Acción Recomendado

1. **Alta prioridad**
   - [ ] Eliminar paquete duplicado `com.bcnc.ecomerce.catalog.auth.*` o consolidar código.
   - [ ] Optimizar `PriceRepository.findApplicablePrices` con `setMaxResults(1)`.
   - [ ] Cambiar inserción en `PriceDataLoader` a `saveAll()`.
   - [ ] Corregir ejemplo sin autenticación en README.

2. **Media prioridad**
   - [ ] Separar `PriceDataLoader` y `AuthCredentialDataLoader`.
   - [ ] Eliminar archivo vacío `AuthCredentialDataLoader.java` o implementarlo.
   - [ ] Hacer inmutables los modelos de dominio (`Price`, `AuthCredential`).
   - [ ] Implementar tests de ArchUnit.

3. **Baja prioridad**
   - [ ] Extraer mapeo `Price` → `PriceResponse` a un mapper dedicado.
   - [ ] Agregar `@Operation` en controladores.
   - [ ] Completar metadata de `pom.xml`.
   - [ ] Cambiar `@SpringBootTest` por `@WebMvcTest` en tests de controlador.

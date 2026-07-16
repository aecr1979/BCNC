# Plan: Persist PRICES Table in H2 Database

## Context
- Spring Boot 4.1.0 with Java 21
- Spring Data JPA already configured in pom.xml
- H2 database needs to be added as dependency
- Data must be loaded on application startup

## Implementation Steps

### 1. Add H2 Database Dependency
Add `com.h2database:h2` to pom.xml with runtime scope.

### 2. Configure H2 in application.yaml
Add datasource configuration:
- URL: `jdbc:h2:mem:testdb` (or file-based)
- Driver: `org.h2.Driver`
- Username/Password: `sa` / (empty)
- Enable H2 console for debugging
- Set `ddl-auto: create-drop` or `update`
- Configure `spring.sql.init.mode: always` for data loading

### 3. Create Domain Model: Price
Location: `src/main/java/com/bcnc/ecomerce/catalog/domain/model/Price.java`
Fields matching the PRICES table:
- `brandId` (Long)
- `startDate` (LocalDateTime)
- `endDate` (LocalDateTime)
- `priceList` (Long)
- `productId` (ProductId)
- `priority` (Integer)
- `price` (BigDecimal)
- `currency` (String)

### 4. Create JPA Entity: PriceEntity
Location: `src/main/java/com/bcnc/ecomerce/catalog/adapter/out/persistence/entity/PriceEntity.java`
- Annotate with `@Entity` and `@Table(name = "PRICES")`
- Map all columns with proper JPA annotations
- Use `@Id` with generated value or composite key

### 5. Create Repository: PriceRepository
Location: `src/main/java/com/bcnc/ecomerce/catalog/adapter/out/persistence/repository/PriceRepository.java`
- Extend `JpaRepository<PriceEntity, Long>`

### 6. Create Data Loader
Location: `src/main/java/com/bcnc/ecomerce/catalog/config/PriceDataLoader.java`
- Implement `CommandLineRunner` or `ApplicationRunner`
- Inject `PriceRepository`
- Insert the 4 rows of data on startup

### 7. Data to Insert
```sql
BRAND_ID | START_DATE           | END_DATE             | PRICE_LIST | PRODUCT_ID | PRIORITY | PRICE  | CURR
1        | 2020-06-14 00:00:00  | 2020-12-31 23:59:59  | 1          | 35455      | 0        | 35.50  | EUR
1        | 2020-06-14 15:00:00  | 2020-06-14 18:30:00  | 2          | 35455      | 1        | 25.45  | EUR
1        | 2020-06-15 00:00:00  | 2020-06-15 11:00:00  | 3          | 35455      | 1        | 30.50  | EUR
1        | 2020-06-15 16:00:00  | 2020-12-31 23:59:59  | 4          | 35455      | 1        | 38.95  | EUR
```

## Architecture Considerations
- Follow existing DDD pattern: domain model → entity → repository
- Use BigDecimal for monetary values
- Use LocalDateTime for timestamps
- Keep the data loader simple and idempotent

Dán toàn bộ:

# Enterprise Ecommerce - Architecture

## 1. Project Architecture

The system uses a **Modular Monolith Architecture**.

The application is deployed as one Spring Boot application, but the business system is divided into clearly separated modules.

High-level structure:

```text
Client
   |
   v
Spring Boot Application
   |
   +-- Auth Module
   +-- User Module
   +-- Product Module
   +-- Category Module
   +-- Cart Module
   +-- Order Module
   +-- Payment Module
   +-- Media Module
   +-- Review Module
   +-- Notification Module
   +-- Chat Module
   |
   v
MySQL
```

The goal is to keep strong module boundaries while avoiding the operational complexity of microservices.

2. Technology Stack
Backend
Java 17+
Spring Boot 3.x
Spring Web
Spring Data JPA
Spring Security
JWT
Bean Validation
Maven
Lombok when appropriate
Database
MySQL
Supporting Technologies
Redis
WebSocket
Docker
Docker Compose

These technologies should only be introduced when the business requirement justifies them.

Frontend
React
TypeScript
Vite
3. Modules

Initial modules:

auth
user
product
category
cart
order
payment
media
review
notification
chat

The module list may evolve during development.

New modules must have a clear business responsibility.

4. Module Ownership

Every module owns its own:

Entities
Repositories
Services
Controllers
DTOs
Business logic

Example:

Product Module
    |
    +-- Product Entity
    +-- ProductRepository
    +-- ProductService
    +-- ProductController
    +-- Product DTOs

Other modules must not directly access these internal implementation details.

5. Cross-Module Communication
Forbidden

A module must NOT directly access another module's:

Repository
Entity
internal Service implementation
database tables through its repository

For example:

Cart
   X
   |
   +----> ProductRepository

or:

Cart
   X
   |
   +----> ProductEntity

These create tight coupling between modules.

6. Preferred Cross-Module Communication

Modules communicate through explicit internal contracts.

Example:

Cart
 |
 v
ProductInternalService
 |
 v
ProductInternalData

Example:

public interface ProductInternalService {

    ProductInternalData getProductVariant(Long variantId);

    boolean exists(Long productId);
}

The implementation belongs to the Product module.

The Cart module only depends on the contract.

7. Internal DTOs

When one module needs information from another module, use an internal DTO.

Example:

public record ProductInternalData(
    Long productId,
    Long variantId,
    String productName,
    String variantName,
    BigDecimal price,
    boolean active
) {}

The internal DTO should contain only the information required by the consuming module.

Do not expose the complete Entity.

8. Dependency Direction

Dependencies should be intentional.

Example:

Cart
 |
 v
Product Contract

is acceptable.

But:

Product
 |
 v
Cart
 |
 v
Product

must be avoided unless there is a very strong architectural reason.

Avoid circular module dependencies.

9. Layering Inside a Module

A typical module may contain:

product/
|
+-- controller/
|
+-- service/
|
+-- repository/
|
+-- entity/
|
+-- dto/
|
+-- exception/
|
+-- internal/

Typical request flow:

HTTP Request
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
MySQL

The Controller should not contain business logic.

The Repository should focus on persistence.

Business rules belong in the appropriate service/domain layer.

10. DTO Rules

Do not expose JPA Entities directly from REST APIs.

Use:

Request DTO
Response DTO
Internal DTO

Example:

POST /api/products

ProductCreateRequest
        |
        v
ProductService
        |
        v
Product Entity
        |
        v
ProductRepository
        |
        v
ProductResponse
11. Exception Handling

Business exceptions should be explicit.

Example:

ProductNotFoundException
InsufficientStockException
InvalidOrderStateException
PaymentException

Use centralized exception handling where appropriate.

Example:

@RestControllerAdvice

Do not silently swallow exceptions.

12. Validation

Validate input at the API boundary using Bean Validation.

Example:

@NotBlank
@Size(...)
@Positive
@Email

Business validation must still be performed in the service/domain layer when necessary.

13. Transaction Management

Use transactions at appropriate application/service boundaries.

Example:

@Transactional
public void createOrder(...) {
    ...
}

Do not add @Transactional everywhere without understanding the transaction boundary.

14. Security

Authentication and authorization are centralized through Spring Security.

Expected authentication flow:

Login
  |
  v
Authentication
  |
  v
JWT
  |
  v
Client
  |
  v
Authorization Header
  |
  v
Spring Security
  |
  v
Protected Endpoint

Do not duplicate authentication logic inside every module.

15. Database Rules

MySQL is the primary relational database.

Entities are owned by their respective modules.

Avoid using database access as a shortcut for bypassing module boundaries.

A module should access its own persistence layer.

16. API Rules

REST APIs should follow consistent conventions.

Example:

GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

Use appropriate HTTP status codes.

Keep request and response structures consistent.

17. Architecture Principles

Prioritize:

Correctness
Clear module boundaries
Simplicity
Maintainability
Security
Testability
Development speed

Avoid unnecessary:

Design patterns
Abstraction layers
Generic frameworks
Microservices
Infrastructure complexity

Use complexity only when there is a real requirement.

18. Architecture Decision Rule

When multiple solutions are possible:

Prefer the simplest valid solution.
Prefer consistency with existing architecture.
Prefer low coupling.
Prefer explicit module boundaries.
Avoid premature optimization.
Explain important trade-offs before implementation.
## 19. Scope Rule

Every development task must have an explicit scope.

The scope should define:

- Module
- Use case
- Expected outcome
- Files or modules that may need to be changed

AI must not expand the scope automatically.

If implementation requires changes outside the declared scope:

1. Explain why the change is necessary.
2. Identify the affected files/modules.
3. Explain the impact.
4. Ask for approval before making the change.

AI must not modify unrelated modules without explicit justification.

---

## 20. AI-Assisted Development Rules

Before modifying code, AI must:

1. Read this document.
2. Inspect the existing project.
3. Check the proposed change against these architecture rules.
4. Identify possible architecture violations.
5. Explain significant architectural changes before implementation.

AI must not:

- Directly access another module's Repository.
- Directly access another module's Entity.
- Introduce unnecessary abstractions.
- Introduce microservices without explicit approval.
- Rewrite unrelated code.
- Expand the task scope without approval.

When multiple valid designs exist, AI must:

1. Recommend one.
2. Explain why.
3. Explain the main alternative.
4. Explain the trade-offs.

The simplest valid solution should be preferred.

---

## 21. Architecture Evolution

This architecture is allowed to evolve as the project grows.

However, architectural changes must be intentional.

Before making a significant architectural change:

1. Identify the current rule.
2. Explain why it is insufficient.
3. Propose the new approach.
4. Explain the trade-offs.
5. Obtain approval before implementation.

Do not change architecture merely for theoretical flexibility or premature optimization.
## Architecture Rules

- Use a separate domain model for business objects. Controllers may use request/response DTOs at the HTTP boundary, but service and repository-service layers must operate on domain objects, not JPA entities.
- Every Spring Data repository must have its own `RepositoryService`. Application services must not inject repositories directly. Repository-service classes accept domain objects, map them to entities for repository operations, and map entities back to domain objects.
- All JPA persistence classes must be suffixed with `Entity`.
- Architecture tests must enforce that controllers do not depend on entities, services do not depend on DTOs or entities, and repositories do not depend on request/response DTOs.

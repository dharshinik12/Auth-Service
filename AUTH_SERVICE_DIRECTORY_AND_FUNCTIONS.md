# Auth Service Directory Structure and Function Inventory

## Overview

This service is a Spring Boot authentication microservice for:

- user registration
- user login
- JWT generation and validation
- request authentication through a JWT filter
- basic user lookup for Spring Security

It uses:

- Spring Boot Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JJWT
- Eureka Client

## Directory Structure

```text
Auth-Service/
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   `-- com/jobzen/auth/
|   |   |       |-- AuthServiceApplication.java
|   |   |       |-- config/
|   |   |       |   |-- CorsConfigurationSource.java
|   |   |       |   `-- SecurityConfig.java
|   |   |       |-- controller/
|   |   |       |   `-- AuthController.java
|   |   |       |-- dto/
|   |   |       |   |-- AuthResponse.java
|   |   |       |   |-- LoginRequest.java
|   |   |       |   |-- LoginResponse.java
|   |   |       |   `-- RegisterRequest.java
|   |   |       |-- entity/
|   |   |       |   |-- Role.java
|   |   |       |   `-- User.java
|   |   |       |-- exception/
|   |   |       |   `-- GlobalExceptionHandler.java
|   |   |       |-- repository/
|   |   |       |   |-- RoleRepository.java
|   |   |       |   `-- UserRepository.java
|   |   |       |-- security/
|   |   |       |   |-- CustomUserDetailsService.java
|   |   |       |   |-- JwtAuthenticationFilter.java
|   |   |       |   `-- JwtService.java
|   |   |       `-- service/
|   |   |           |-- AuthService.java
|   |   |           `-- AuthServiceImpl.java
|   |   `-- resources/
|   |       |-- application.properties
|   |       `-- application.yml
|   `-- test/
|       `-- java/
|           `-- com/jobzen/auth/
|               |-- AuthServiceApplicationTests.java
|               `-- service/
|                   `-- AuthServiceImplTest.java
`-- AUTH_SERVICE_DIRECTORY_AND_FUNCTIONS.md
```

## File-by-File Functions and Use Cases

### `src/main/java/com/jobzen/auth/AuthServiceApplication.java`

**Purpose:** Spring Boot entry point.

**Functions**

- `main(String[] args)`
  - Starts the Spring Boot application.
  - Use case: bootstraps the auth service and all configured beans.

### `src/main/java/com/jobzen/auth/config/SecurityConfig.java`

**Purpose:** Central Spring Security and CORS configuration.

**Functions**

- `securityFilterChain(HttpSecurity http)`
  - Disables CSRF, enables CORS, allows public access to `/auth/register`, `/auth/login`, and `/auth/test`, and requires authentication for everything else.
  - Adds `JwtAuthenticationFilter` before the standard username/password filter.
  - Use case: defines which endpoints are public and how JWT authentication is enforced.

- `passwordEncoder()`
  - Returns a `BCryptPasswordEncoder`.
  - Use case: hashes user passwords during registration and checks hashes during login.

- `authenticationManager(AuthenticationConfiguration config)`
  - Returns the framework-managed `AuthenticationManager`.
  - Use case: exposes authentication infrastructure as a bean if needed by other components.

- `corsConfigurationSource()`
  - Allows requests from `http://localhost:5173` with any method and header.
  - Use case: enables the local frontend to call this auth service during development.

### `src/main/java/com/jobzen/auth/config/CorsConfigurationSource.java`

**Purpose:** Empty class stub.

**Functions**

- No functions.
  - Use case: none in current state.
  - Note: this file is not providing runtime behavior; the active CORS bean is in `SecurityConfig`.

### `src/main/java/com/jobzen/auth/controller/AuthController.java`

**Purpose:** HTTP API layer for auth endpoints.

**Functions**

- `AuthController(AuthService authService)`
  - Constructor injection for the service layer.
  - Use case: wires controller requests to the auth business logic.

- `register(RegisterRequest request)`
  - Handles `POST /auth/register`.
  - Validates the request body and delegates registration.
  - Use case: creates a new user account.

- `login(LoginRequest request)`
  - Handles `POST /auth/login`.
  - Validates the request body and delegates login.
  - Use case: authenticates a user and returns a JWT token string.

- `profile()`
  - Handles `GET /auth/profile`.
  - Returns a success message if JWT authentication has passed.
  - Use case: protected test endpoint to confirm authenticated access works.

- `test()`
  - Handles `GET /auth/test`.
  - Returns a simple service health string.
  - Use case: public smoke-test endpoint.

### `src/main/java/com/jobzen/auth/dto/RegisterRequest.java`

**Purpose:** Request payload for registration.

**Functions**

- No handwritten functions.
  - Lombok `@Data` generates getters, setters, `toString`, `equals`, and `hashCode`.
  - Use case: carries and validates `email` and `password` for registration requests.

**Validation rules**

- `email` must be non-blank and a valid email format.
- `password` must be non-blank and at least 8 characters long.

### `src/main/java/com/jobzen/auth/dto/LoginRequest.java`

**Purpose:** Request payload for login.

**Functions**

- No handwritten functions.
  - Lombok `@Data` generates getters, setters, `toString`, `equals`, and `hashCode`.
  - Use case: carries login credentials from the client to the service.

**Validation rules**

- `email` must be non-blank and a valid email format.
- `password` must be non-blank.

### `src/main/java/com/jobzen/auth/dto/LoginResponse.java`

**Purpose:** DTO for returning a token.

**Functions**

- No handwritten functions.
  - Lombok generates constructor and accessors.
  - Use case: intended to wrap a JWT in a response object.

**Current usage**

- Not currently used by `AuthController` or `AuthServiceImpl`, which return raw `String` values instead.

### `src/main/java/com/jobzen/auth/dto/AuthResponse.java`

**Purpose:** Another DTO for returning a token.

**Functions**

- No handwritten functions.
  - Lombok generates constructor and accessors.
  - Use case: intended token response wrapper.

**Current usage**

- Not currently used by the active controller/service flow.

### `src/main/java/com/jobzen/auth/entity/User.java`

**Purpose:** JPA entity for application users.

**Functions**

- No handwritten functions.
  - Lombok generates constructors, getters, and setters.
  - Use case: persists user ID, email, password hash, and assigned roles.

**Key role in the system**

- Stored in the `users` table.
- Linked to roles through `user_roles`.
- Read during login and security context creation.

### `src/main/java/com/jobzen/auth/entity/Role.java`

**Purpose:** JPA entity for roles/authorities.

**Functions**

- No handwritten functions.
  - Lombok generates constructors, getters, and setters.
  - Use case: stores role names used as Spring Security authorities.

**Key role in the system**

- Stored in the `roles` table.
- Mapped to users via a many-to-many relationship.

### `src/main/java/com/jobzen/auth/exception/GlobalExceptionHandler.java`

**Purpose:** Maps exceptions to HTTP responses.

**Functions**

- `handleRuntime(RuntimeException ex)`
  - Converts runtime exceptions into `400 Bad Request` with the exception message.
  - Use case: returns readable errors such as duplicate email, missing user, or invalid password.

- `handleValidation(MethodArgumentNotValidException ex)`
  - Converts validation failures into `400 Bad Request` with `"Validation failed"`.
  - Use case: handles invalid request payloads for login and registration.

### `src/main/java/com/jobzen/auth/repository/UserRepository.java`

**Purpose:** JPA repository for `User`.

**Functions**

- `findByEmail(String email)`
  - Looks up a user by email.
  - Use case: checks for duplicate registration, loads users for login, and loads users for JWT-backed authentication.

**Inherited repository capabilities**

- Also inherits `save`, `findById`, `findAll`, `deleteById`, and other CRUD methods from `JpaRepository`.

### `src/main/java/com/jobzen/auth/repository/RoleRepository.java`

**Purpose:** JPA repository for `Role`.

**Functions**

- No custom functions.
  - Use case: provides standard CRUD access to roles through inherited `JpaRepository` methods.

### `src/main/java/com/jobzen/auth/security/CustomUserDetailsService.java`

**Purpose:** Adapts application users to Spring Security `UserDetails`.

**Functions**

- `loadUserByUsername(String email)`
  - Finds a user by email and converts that user into Spring Security's `UserDetails`.
  - Maps role names into authorities.
  - Use case: supplies authenticated principal data when validating a JWT and building the security context.

### `src/main/java/com/jobzen/auth/security/JwtService.java`

**Purpose:** Creates and validates JWT tokens.

**Functions**

- `getSigningKey()`
  - Builds the HMAC signing key from `jwt.secret`.
  - Use case: internal helper for signing and parsing tokens.

- `generateToken(String email)`
  - Creates a JWT with the email as subject, issued time, expiration, and HS256 signature.
  - Use case: returns an access token after successful login.

- `extractEmail(String token)`
  - Parses a JWT and returns the subject email.
  - Use case: identifies which user a bearer token belongs to.

- `isTokenValid(String token, String email)`
  - Confirms the token subject matches the expected email and that the token is not expired.
  - Use case: final token check before authenticating a request.

- `isTokenExpired(String token)`
  - Parses token expiration and compares it with current time.
  - Use case: blocks expired tokens from authenticating requests.

### `src/main/java/com/jobzen/auth/security/JwtAuthenticationFilter.java`

**Purpose:** Per-request JWT authentication filter.

**Functions**

- `doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`
  - Reads the `Authorization` header.
  - Skips processing if the header is missing or does not start with `Bearer `.
  - Extracts the token and email.
  - Loads user details by email.
  - Validates the token.
  - Stores an authenticated `UsernamePasswordAuthenticationToken` in the Spring Security context.
  - Continues the request through the filter chain.
  - Use case: enables stateless authentication for protected endpoints.

### `src/main/java/com/jobzen/auth/service/AuthService.java`

**Purpose:** Service contract for auth operations.

**Functions**

- `register(RegisterRequest request)`
  - Declares the user registration operation.
  - Use case: allows controller logic to depend on an interface instead of a concrete class.

- `login(LoginRequest request)`
  - Declares the login operation.
  - Use case: allows controller logic to delegate authentication/token generation cleanly.

### `src/main/java/com/jobzen/auth/service/AuthServiceImpl.java`

**Purpose:** Core business logic for registration and login.

**Functions**

- `register(RegisterRequest request)`
  - Checks whether the email already exists.
  - Creates a new `User`.
  - Hashes the password.
  - Initializes the user with an empty roles set.
  - Saves the user in the database.
  - Returns `"User registered successfully"`.
  - Use case: onboarding new users securely by storing hashed passwords.

- `login(LoginRequest request)`
  - Looks up the user by email.
  - Throws if the user does not exist.
  - Verifies the provided password against the stored hash.
  - Throws if the password is invalid.
  - Generates and returns a JWT token.
  - Use case: authenticates existing users and issues stateless access tokens.

## Test Files and What They Cover

### `src/test/java/com/jobzen/auth/AuthServiceApplicationTests.java`

**Functions**

- `contextLoads()`
  - Verifies that the Spring application context starts successfully.
  - Use case: basic application boot sanity check.

### `src/test/java/com/jobzen/auth/service/AuthServiceImplTest.java`

**Functions**

- `setUp()`
  - Recreates `AuthServiceImpl` with mocks before each test.
  - Use case: isolates service-level behavior from real infrastructure.

- `registerShouldSaveNewUserAndReturnSuccessMessage()`
  - Verifies successful registration flow.
  - Use case: confirms user creation, password encoding, and success response.

- `registerShouldThrowWhenEmailAlreadyExists()`
  - Verifies duplicate email rejection.
  - Use case: confirms the service prevents duplicate registrations.

- `loginShouldReturnJwtTokenForValidCredentials()`
  - Verifies successful login and token generation.
  - Use case: confirms the happy path for authentication.

- `loginShouldThrowWhenUserNotFound()`
  - Verifies login fails when no user exists for the email.
  - Use case: confirms missing-user handling.

- `loginShouldThrowWhenPasswordIsInvalid()`
  - Verifies login fails for bad credentials.
  - Use case: confirms password mismatch handling.

## Runtime Configuration Files

### `src/main/resources/application.yml`

**Purpose**

- Runs the service on port `8081`.
- Configures PostgreSQL datasource.
- Enables Hibernate schema auto-update and SQL logging.
- Registers with Eureka at `http://localhost:8761/eureka/`.

### `src/main/resources/application.properties`

**Purpose**

- Stores JWT settings:
  - `jwt.secret`
  - `jwt.expiration`

## Request Flow Summary

### Registration Flow

1. Client sends `POST /auth/register`.
2. `AuthController.register()` validates payload.
3. `AuthServiceImpl.register()` checks email uniqueness.
4. Password is hashed with `BCryptPasswordEncoder`.
5. User is saved with empty roles.
6. Success message is returned.

### Login Flow

1. Client sends `POST /auth/login`.
2. `AuthController.login()` validates payload.
3. `AuthServiceImpl.login()` loads user by email.
4. Password is verified with `PasswordEncoder.matches(...)`.
5. `JwtService.generateToken()` creates a JWT.
6. Token string is returned to the client.

### Protected Request Flow

1. Client sends a request with `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter.doFilterInternal()` extracts the token.
3. `JwtService.extractEmail()` reads the token subject.
4. `CustomUserDetailsService.loadUserByUsername()` loads user details.
5. `JwtService.isTokenValid()` checks subject and expiry.
6. Security context is populated.
7. Protected endpoint such as `GET /auth/profile` is allowed.

## Notes and Gaps in Current Code

- `AuthController` returns raw `String` values instead of using `AuthResponse` or `LoginResponse`.
- `config/CorsConfigurationSource.java` is an empty file and appears unused.
- New users are saved with an empty role set; there is no role assignment during registration.
- `RoleRepository` exists but is not used in the current flow.
- Validation error responses are generic and do not expose field-level messages.

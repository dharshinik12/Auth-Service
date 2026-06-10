# Auth Service API Testing Guide

This guide covers the order to test the auth APIs, sample headers, payloads, and expected responses.

Base URL for direct service testing:

```text
http://localhost:8081
```

Base URL for gateway testing after service discovery is working:

```text
http://localhost:8080
```

## Prerequisites

1. PostgreSQL is installed and running.
2. Database `jobzen_auth` exists.
3. `Auth-Service/src/main/resources/application.yml` has the correct Postgres username and password.
4. Start `Auth-Service`.
5. For gateway testing, also start `Discovery-Server` and `Gateway-Server`.

## Recommended Test Order

Test in this order:

1. Health check
2. Register new user
3. Login with valid credentials
4. Access protected profile endpoint with token
5. Negative login and validation tests
6. Logout with token
7. Access protected profile again with the same token
8. Optional gateway-level retest

## Common Headers

Use these headers unless noted otherwise:

```http
Content-Type: application/json
Accept: application/json
```

For protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
```

Note:
- The current login endpoint returns a raw JWT string, not a JSON object.
- Some endpoints return plain text responses.

## 1. Health Check

Request:

```http
GET /auth/test HTTP/1.1
Host: localhost:8081
```

Full example:

```text
GET http://localhost:8081/auth/test
```

Expected response:

```text
Auth Service Working
```

## 2. Register New User

Request:

```text
POST http://localhost:8081/auth/register
```

Headers:

```http
Content-Type: application/json
Accept: application/json
```

Payload:

```json
{
  "email": "user1@example.com",
  "password": "password123"
}
```

Expected response:

```text
User registered successfully
```

## 3. Login With Valid Credentials

Request:

```text
POST http://localhost:8081/auth/login
```

Headers:

```http
Content-Type: application/json
Accept: application/json
```

Payload:

```json
{
  "email": "user1@example.com",
  "password": "password123"
}
```

Expected response:

```text
<JWT token string>
```

Save the token from this response. You will use it in the `Authorization` header for protected endpoints.

## 4. Access Protected Profile Endpoint

Request:

```text
GET http://localhost:8081/auth/profile
```

Headers:

```http
Authorization: Bearer <JWT_TOKEN>
Accept: application/json
```

Expected response:

```text
JWT Authentication Successful
```

## 5. Negative Tests

### 5.1 Register Same Email Again

Request:

```text
POST http://localhost:8081/auth/register
```

Payload:

```json
{
  "email": "user1@example.com",
  "password": "password123"
}
```

Expected response:

```text
Email already exists
```

### 5.2 Login With Wrong Password

Request:

```text
POST http://localhost:8081/auth/login
```

Payload:

```json
{
  "email": "user1@example.com",
  "password": "wrongpassword"
}
```

Expected response:

```text
Invalid password
```

### 5.3 Login With Unknown User

Request:

```text
POST http://localhost:8081/auth/login
```

Payload:

```json
{
  "email": "missing@example.com",
  "password": "password123"
}
```

Expected response:

```text
User not found
```

### 5.4 Register With Invalid Payload

Request:

```text
POST http://localhost:8081/auth/register
```

Payload:

```json
{
  "email": "bad-email",
  "password": "123"
}
```

Expected response:

```text
Validation failed
```

### 5.5 Profile Without Token

Request:

```text
GET http://localhost:8081/auth/profile
```

Headers:

```http
Accept: application/json
```

Expected response:

- `401 Unauthorized` or `403 Forbidden`

## 6. Logout

Use this only if your current local code includes the `/auth/logout` endpoint.

Request:

```text
POST http://localhost:8081/auth/logout
```

Headers:

```http
Authorization: Bearer <JWT_TOKEN>
Accept: application/json
```

Expected response:

```text
User logged out successfully
```

## 7. Reuse Same Token After Logout

After logout, call the profile endpoint again with the same token.

Request:

```text
GET http://localhost:8081/auth/profile
```

Headers:

```http
Authorization: Bearer <JWT_TOKEN>
Accept: application/json
```

Expected result:

- If token revocation is active, the request should fail as unauthorized.
- If it still succeeds, logout is not invalidating tokens yet.

## 8. Token Expiry Test

The current config uses:

```yaml
jwt:
  expiration: 86400000
```

That is 24 hours, so expiry is not practical to test manually as-is.

To test expiry quickly:

1. Temporarily change `jwt.expiration` in `application.yml` to a short value such as:

```yaml
jwt:
  expiration: 10000
```

2. Restart `Auth-Service`.
3. Login and get a token.
4. Use the token immediately on `/auth/profile` and confirm it works.
5. Wait 10 to 15 seconds.
6. Call `/auth/profile` again with the same token.

Expected result:

- The second request should fail because the token is expired.

After testing, restore the original value:

```yaml
jwt:
  expiration: 86400000
```

## Sample cURL Commands

### Register

```bash
curl -X POST http://localhost:8081/auth/register ^
  -H "Content-Type: application/json" ^
  -H "Accept: application/json" ^
  -d "{\"email\":\"user1@example.com\",\"password\":\"password123\"}"
```

### Login

```bash
curl -X POST http://localhost:8081/auth/login ^
  -H "Content-Type: application/json" ^
  -H "Accept: application/json" ^
  -d "{\"email\":\"user1@example.com\",\"password\":\"password123\"}"
```

### Profile

```bash
curl http://localhost:8081/auth/profile ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Accept: application/json"
```

### Logout

```bash
curl -X POST http://localhost:8081/auth/logout ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Accept: application/json"
```

## Postman Setup

Create a collection variable:

```text
baseUrl = http://localhost:8081
```

Create another variable after login:

```text
token = <paste JWT here>
```

Then use this header for protected requests:

```http
Authorization: Bearer {{token}}
```

## Recommended Startup Order For Full Flow

If testing direct auth service only:

1. PostgreSQL
2. Auth-Service

If testing through gateway:

1. PostgreSQL
2. Discovery-Server
3. Auth-Service
4. Gateway-Server

## Notes

- Tables are created automatically by Hibernate because `ddl-auto: update` is enabled.
- You do not need to create tables manually.
- You do need the `jobzen_auth` database to exist before starting `Auth-Service`.

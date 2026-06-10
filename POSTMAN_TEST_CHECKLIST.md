# Auth Service Postman Test Checklist

Use this checklist while testing the auth APIs from Postman.

Directory:

```text
Auth-Service/POSTMAN_TEST_CHECKLIST.md
```

## Environment Setup

- [ ] PostgreSQL is installed
- [ ] PostgreSQL service is running
- [ ] Database `jobzen_auth` is created
- [ ] `Auth-Service/src/main/resources/application.yml` has the correct DB username and password
- [ ] `Auth-Service` is started successfully

Base URL for direct service testing:

```text
http://localhost:8081
```

Base URL for gateway testing:

```text
http://localhost:8080
```

## Postman Variables

Create these collection or environment variables:

```text
baseUrl = http://localhost:8081
token =
```

## Default Headers

For register and login:

```http
Content-Type: application/json
Accept: application/json
```

For protected endpoints:

```http
Authorization: Bearer {{token}}
Accept: application/json
```

## Test Sequence

### 1. Health Check

- [ ] Request method: `GET`
- [ ] URL: `{{baseUrl}}/auth/test`
- [ ] No body required

Expected:

```text
Auth Service Working
```

### 2. Register User

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/register`
- [ ] Headers added
- [ ] Body set to `raw` and `JSON`

Payload:

```json
{
  "email": "user1@example.com",
  "password": "password123"
}
```

Expected:

```text
User registered successfully
```

### 3. Login User

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/login`
- [ ] Headers added
- [ ] Body set to `raw` and `JSON`

Payload:

```json
{
  "email": "user1@example.com",
  "password": "password123"
}
```

Expected:

```text
<raw JWT token>
```

Action:

- [ ] Copy the token from response
- [ ] Save it into Postman variable `token`

### 4. Access Profile With Token

- [ ] Request method: `GET`
- [ ] URL: `{{baseUrl}}/auth/profile`
- [ ] Add header `Authorization: Bearer {{token}}`

Expected:

```text
JWT Authentication Successful
```

### 5. Register Same User Again

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/register`

Payload:

```json
{
  "email": "user1@example.com",
  "password": "password123"
}
```

Expected:

```text
Email already exists
```

### 6. Login With Wrong Password

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/login`

Payload:

```json
{
  "email": "user1@example.com",
  "password": "wrongpassword"
}
```

Expected:

```text
Invalid password
```

### 7. Login With Unknown User

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/login`

Payload:

```json
{
  "email": "nouser@example.com",
  "password": "password123"
}
```

Expected:

```text
User not found
```

### 8. Validation Failure Test

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/register`

Payload:

```json
{
  "email": "bad-email",
  "password": "123"
}
```

Expected:

```text
Validation failed
```

### 9. Profile Without Token

- [ ] Request method: `GET`
- [ ] URL: `{{baseUrl}}/auth/profile`
- [ ] Do not send `Authorization` header

Expected:

- [ ] Response is unauthorized or forbidden

### 10. Logout

Only use this if your current code includes `/auth/logout`.

- [ ] Request method: `POST`
- [ ] URL: `{{baseUrl}}/auth/logout`
- [ ] Add header `Authorization: Bearer {{token}}`

Expected:

```text
User logged out successfully
```

### 11. Reuse Same Token After Logout

- [ ] Request method: `GET`
- [ ] URL: `{{baseUrl}}/auth/profile`
- [ ] Add header `Authorization: Bearer {{token}}`

Expected:

- [ ] Request should fail if logout revokes tokens correctly

## Expiry Test Checklist

Current JWT expiry in config is 24 hours, so shorten it temporarily for manual testing.

- [ ] Open `Auth-Service/src/main/resources/application.yml`
- [ ] Change:

```yaml
jwt:
  expiration: 86400000
```

to:

```yaml
jwt:
  expiration: 10000
```

- [ ] Restart `Auth-Service`
- [ ] Login again and get a fresh token
- [ ] Call `/auth/profile` immediately and confirm success
- [ ] Wait 10 to 15 seconds
- [ ] Call `/auth/profile` again with the same token
- [ ] Confirm token is rejected
- [ ] Restore expiry back to `86400000`

## Gateway Retest Checklist

Run this only after direct testing succeeds.

- [ ] Start `Discovery-Server`
- [ ] Start `Auth-Service`
- [ ] Start `Gateway-Server`
- [ ] Change `baseUrl` in Postman to:

```text
http://localhost:8080
```

- [ ] Repeat health, register, login, profile, and logout tests through gateway routes

## Pass Criteria

- [ ] Auth service starts without DB errors
- [ ] Register works for a new user
- [ ] Login returns a token
- [ ] Protected endpoint accepts valid token
- [ ] Duplicate registration fails correctly
- [ ] Wrong password fails correctly
- [ ] Missing user fails correctly
- [ ] Invalid payload fails correctly
- [ ] Missing token is rejected
- [ ] Logout invalidates token if logout is implemented
- [ ] Expired token is rejected

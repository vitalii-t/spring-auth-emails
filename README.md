# Spring Auth Emails Demo

Demo Spring Boot project showing a production-like auth flow with the Mailtrap API.

## Scope

- user registration
- email verification with a signed link
- password reset with a stored hashed token
- password reset token expiry after 1 hour
- protected route blocked for guests and unverified users
- H2 database
- Thymeleaf HTML pages and email templates

## Stack

- Java 21
- Spring Boot 3.4.0
- Spring Security
- Spring Data JPA
- H2
- Thymeleaf
- `io.mailtrap:mailtrap-java`

## How it works

1. Register at `/register`.
2. The app creates an unverified user, signs the user in, and sends a Mailtrap verification email.
3. Until the email is verified, `/dashboard` redirects to `/verify-email`.
4. The verification email contains a signed URL. When opened, the app validates the signature and marks the user
   verified.
5. Forgot-password sends a reset email with a random token.
6. The reset token is stored hashed in the H2 database and expires after 60 minutes.
7. A successful password reset updates the password and invalidates the reset token.

## Environment variables

| Variable                         | Required | Default                         | Description                                                                     |
|----------------------------------|----------|---------------------------------|---------------------------------------------------------------------------------|
| `SERVER_PORT`                    | No       | `8080`                          | HTTP port used by the app                                                       |
| `APP_BASE_URL`                   | No       | `http://localhost:${SERVER_PORT}` | Public base URL used in verification and reset links                          |
| `APP_SIGNING_SECRET`             | No       | `change-me-demo-signing-secret` | HMAC secret used to sign verification links. Replace it outside demo use        |
| `APP_VERIFICATION_TTL_MINUTES`   | No       | `60`                            | Verification link lifetime                                                      |
| `APP_PASSWORD_RESET_TTL_MINUTES` | No       | `60`                            | Password reset token lifetime                                                   |
| `MAILTRAP_API_TOKEN`             | Yes      | empty                           | Mailtrap API token                                                              |
| `MAILTRAP_MAIL_FROM_EMAIL`       | No       | `[email protected]`             | Sender email                                                                    |
| `MAILTRAP_MAIL_FROM_NAME`        | No       | `Spring Auth Emails Demo`       | Sender display name                                                             |
| `MAILTRAP_USE_SANDBOX`           | No       | `false`                         | Enable Mailtrap Sandbox API                                                     |
| `MAILTRAP_SANDBOX_INBOX_ID`      | Conditional | `0`                           | Required when `MAILTRAP_USE_SANDBOX=true`; identifies the Mailtrap sandbox inbox |

## Run locally

1. Set environment variables.

macOS/Linux:

```bash
export MAILTRAP_API_TOKEN=your-mailtrap-token
export APP_SIGNING_SECRET=replace-me
export SERVER_PORT=8080
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
$env:MAILTRAP_API_TOKEN="your-mailtrap-token"
$env:APP_SIGNING_SECRET="replace-me"
$env:SERVER_PORT="8080"
.\mvnw.cmd spring-boot:run
```

2. Open `http://localhost:8080` by default, or `http://localhost:<your-port>` if you changed `SERVER_PORT`.

## H2

- JDBC URL: `jdbc:h2:mem:spring-auth-emails`
- Username: `sa`
- Password: empty
- Console: `http://localhost:8080/h2-console` by default, or `http://localhost:<your-port>/h2-console` if you changed `SERVER_PORT`

## Main routes

- `/register`
- `/login`
- `/forgot-password`
- `/reset-password`
- `/verify-email`
- `/dashboard`

## Notes

- The UI is intentionally minimal.
- Tests are limited to unit tests for core services and signing logic.
- Mail sending uses the official Mailtrap Java SDK.
  Reference: [Mailtrap Java SDK guide](https://docs.mailtrap.io/guides/sdk/java)

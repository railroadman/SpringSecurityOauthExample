# Spring Boot 3 + Spring Security + Google OAuth2 (Thymeleaf)

Минимальный проект со страницей и кнопкой **"Sign in with Google"**.

## Быстрый старт
1. Создай OAuth2 Client в Google Cloud Console (Web application).
2. Добавь Redirect URI: `http://localhost:8080/login/oauth2/code/google`.
3. Экспортируй переменные окружения:
   ```bash
   export GOOGLE_SEC_AUTH_CLIENT_ID=your-client-id
   export GOOGLE_SEC_AUTH_CLIENT_SECRET=your-client-secret
   ```
4. Запуск:
   ```bash
   ./gradlew bootRun    # если есть Gradle Wrapper
   # или
   gradle bootRun
   ```
5. Открой `http://localhost:8080`.

## Что есть
- `/` — главная с кнопкой входа; после входа показывает имя/email/аватар.
- `/me` — выводит OIDC claims.
- `/logout` — выход (POST c CSRF token'ом в форме).

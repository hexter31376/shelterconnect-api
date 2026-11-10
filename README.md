# ShelterConnect API

Java 25 / Spring Boot 4.1.2 기반 모놀리식 REST API입니다.

## 기술 구성

- Gradle Wrapper 9.8.2, Spring MVC, Spring Data JPA, Bean Validation, Lombok
- Spring Security: 초기 HTTP Basic 인증, CSRF 보호 유지
- PostgreSQL 17, Flyway: DB 변경 이력 관리, Hibernate는 스키마 검증만 수행
- springdoc-openapi 3.2.0 / Swagger UI: 로컬 프로필에서만 활성화
- Docker / Docker Compose: PostgreSQL 및 선택적 API 컨테이너 실행
- Java 컨테이너 이미지: 빌드·실행 모두 Amazon Corretto 25 (`amazoncorretto:25-al2023`, JDK 포함)
- 이미지 저장: 로컬 디스크 유지. Supabase 연동 및 업로드 API는 아직 구현하지 않음

## 로컬 실행

JDK 25와 실행 중인 Docker Engine / Docker Compose가 필요합니다.

```sh
./gradlew bootRun --args='--spring.profiles.active=local'
```

Spring Boot가 Compose의 PostgreSQL을 시작합니다. 종료 시 DB는 유지됩니다.
기본 DB는 `shelterconnect`, DB 사용자도 `shelterconnect`입니다.
로컬 기본 DB 암호와 Basic 인증 암호는 `local-development-only`, 인증 사용자는 `local`입니다.
개발용 기본값을 외부에 노출하지 마세요.

- 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs
- 상태 확인: http://localhost:8080/actuator/health

일반 API는 인증이 필요합니다. 아직 실제 업무 API와 사용자 인증 도메인은 없습니다.
POST/PUT/PATCH/DELETE는 인증과 CSRF 토큰이 모두 필요합니다. Swagger의 Basic 인증만으로 쓰기 요청을 허용하지 않습니다.
JWT 등 최종 인증 방식을 도입할 때 보안 정책을 함께 변경하세요.

## Docker로 전체 실행

```sh
cp .env.example .env
# .env의 암호를 변경한 뒤 실행
docker compose --profile app up --build -d
docker compose --profile app logs -f api
docker compose --profile app down
```

API 컨테이너는 `prod` 프로필로 실행되므로 Swagger는 비활성화됩니다.
포트는 로컬호스트에만 바인딩됩니다. 외부 배포 시 HTTPS 프록시와 접근 정책을 설정하세요.
DB와 이미지는 각각 named volume에 보존됩니다. `down -v`는 데이터를 삭제하므로 주의하세요.
Docker 이미지 빌드는 테스트를 수행하지 않으므로 별도로 테스트를 실행하세요.

## 환경 설정

| 변수 | 용도 |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | JDBC 연결 정보. `prod`에서는 모두 필수 |
| `APP_SECURITY_USERNAME`, `APP_SECURITY_PASSWORD` | 초기 Basic 인증 계정. `prod`에서는 필수 |
| `SERVER_PORT` | HTTP 포트, 기본 8080 |
| `STORAGE_LOCAL_ROOT` | 이미지 저장 경로, 기본 `./storage/images` |
| `POSTGRES_DB`, `DB_PORT` | Compose DB 이름과 호스트 포트 |

`.env`는 Compose가 읽으며 Java/Gradle은 자동으로 읽지 않습니다. `bootRun` 설정은 셸 환경 변수로 전달하세요.
외부 DB를 사용하려면 `SPRING_DOCKER_COMPOSE_ENABLED=false`와 DB 연결 정보를 지정하세요.
이미지 경로는 `StorageProperties`로 바인딩됩니다. 실제 저장/조회 로직은 이후 이 설정을 사용해 구현합니다.
Supabase 자격 증명이나 SDK는 추가하지 않았습니다.

## 개발 구조 및 DB 변경

소스 루트는 `src/main/java/org/shelterconnect/api`, 공통 설정은 `config`입니다.
기능 추가 시 `<domain>/domain`, `<domain>/application`, `<domain>/infrastructure`, `<domain>/presentation` 구조를 권장합니다.
아직 요구사항이 없는 도메인이나 테이블은 생성하지 않습니다.

`src/main/resources/db/migration/V1__create_application_schema.sql`은 `shelterconnect` 스키마만 생성합니다.
Flyway 이력은 기본 `public` 스키마에, JPA 업무 테이블은 `shelterconnect`에 저장합니다.
변경은 `V2__create_dogs.sql`처럼 새 파일로 추가하고 적용된 파일은 수정하지 마세요.
기존 DB에는 이 초기 마이그레이션을 바로 적용하지 말고 스키마와 기존 Flyway 이력을 먼저 확인하세요.

## 빌드 및 테스트

```sh
./gradlew test --tests '*SecurityConfigTests' # Docker 없이 보안 정책 검증
./gradlew test                             # Docker 필요: PostgreSQL Testcontainers 포함
./gradlew build                            # 테스트 및 실행 JAR 생성
java -jar build/libs/app.jar --spring.profiles.active=prod
```

통합 테스트는 독립 PostgreSQL 컨테이너에서 Flyway 마이그레이션과 Spring 컨텍스트를 검증합니다.
Docker가 없으면 통합 테스트는 실패하며 자동으로 생략하지 않습니다.

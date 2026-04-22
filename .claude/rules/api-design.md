---
globs: src/main/java/com/inseong/dallyrun/backend/controller/**/*.java, src/main/java/com/inseong/dallyrun/backend/dto/**/*.java
---

# API 설계 규칙

REST 컨트롤러와 DTO 에 적용되는 규칙입니다. Spring Boot + `jakarta.validation` + `record` DTO 기반.

## DTO

### Request — `dto/request/`

```java
public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}
```

**규칙**
- `record` 를 우선 사용 (불변)
- 검증 애너테이션은 필드 위에 부착 (`@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max` 등)
- 메시지는 한글, 사용자에게 그대로 노출 가능한 문장으로
- 엔티티를 Request 로 직접 받지 않음

### Response — `dto/response/`

```java
public record MemberResponse(
        Long memberId,
        String email,
        String nickname
) {
}
```

**규칙**
- 민감 정보 노출 금지 (password 해시, refreshToken 등)
- 엔티티를 그대로 반환하지 않음 — DTO 로 변환
- `Optional` 필드는 `null` 로 직렬화 (필요시 `@JsonInclude(NON_NULL)`)

## 응답 포맷

모든 성공 응답은 `ApiResponse<T>` 로 래핑합니다.

```java
public record ApiResponse<T>(T data) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
```

**사용**

```java
return ResponseEntity.ok(ApiResponse.of(response));
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
```

응답 본문이 없는 경우 (DELETE 등) 는 `ResponseEntity<Void>` 반환.

## 컨트롤러 패턴

```java
@Tag(name = "Auth", description = "인증 API — ...")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "...")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "..."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "..."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "...")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        TokenResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }
}
```

**규칙**
- 생성자 주입만 사용 (`@Autowired` 필드 주입 금지)
- `@RequestBody` 에는 반드시 `@Valid`
- 인증된 사용자는 `@AuthenticationPrincipal CustomUserDetails` 로 받음
- **비즈니스 로직은 컨트롤러에 두지 않음** — 서비스로 위임
- SpringDoc (`@Operation`, `@ApiResponses`) 주석을 누락하지 않음 — Swagger 문서의 품질이 API 문서 품질

## 에러 처리

### ErrorCode

`exception/ErrorCode.java` 의 enum 을 사용합니다. 새 에러 상황이면 **먼저 enum 에 항목을 추가**하세요.

```java
public enum ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    // ...
}
```

### BusinessException

```java
throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
```

- `RuntimeException` 직접 던지기 금지
- `GlobalExceptionHandler` 가 `BusinessException` → `ErrorResponse` + 적절한 HTTP status 로 변환
- 검증 실패 (`@Valid` 위반) 는 Spring 이 400 으로 자동 변환됨

## 엔드포인트 네이밍

- 기본: `/api/{domain}/...`
- 인증 불필요: `/api/auth/**`, `/api/share/{code}` (공개 조회)
- 리소스 복수형: `/api/running-sessions`, `/api/goals`
- HTTP 메서드 의미 준수: GET 조회, POST 생성, PATCH 부분 수정, PUT 전체 교체, DELETE 삭제

## 체크리스트

PR/리뷰 시 확인:

- [ ] Request DTO 에 적절한 `@NotBlank`/`@Size`/`@Email` 등 검증이 있는가
- [ ] Controller `@RequestBody` 에 `@Valid` 가 있는가
- [ ] 응답이 `ApiResponse<T>` 로 래핑되는가
- [ ] 민감 정보가 응답에 포함되지 않는가
- [ ] `RuntimeException` 이 아닌 `BusinessException(ErrorCode.*)` 를 사용하는가
- [ ] Swagger 주석 (`@Operation`, `@ApiResponses`) 이 있는가
- [ ] 비즈니스 로직이 컨트롤러가 아닌 서비스에 있는가

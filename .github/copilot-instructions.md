# GitHub Copilot Instructions for Spring Boot Backend

## 1. 코드 스타일 (Code Style)
- **Java 버전:** Java 17 이상의 문법(Record, Switch Expression 등)을 적극 활용해 주세요.
- **Lombok:** Getter, Setter, Builder, RequiredArgsConstructor 어노테이션을 사용하여 보일러플레이트 코드를 줄여주세요.
- **가독성:** Stream API를 사용할 때는 디버깅과 가독성을 위해 각 연산마다 줄바꿈을 해주세요.
- **가독성** Fully Qualified Name방식을 지양하고,Import 구문을 활용하여 코드의 가독성을 높여주세요.

## 2. Spring Boot 규칙 (Spring Best Practices)
- **의존성 주입:** `@Autowired` 필드 주입 대신, `final` 키워드와 `@RequiredArgsConstructor`를 사용한 **생성자 주입** 방식을 사용하세요.
- **JPA:** Entity 클래스에는 `@Setter` 사용을 지양하고, 별도의 비즈니스 메서드(예: `changeStatus()`)를 만들어 상태를 변경하세요.
- **Controller:** API 응답은 단순히 엔티티를 반환하지 말고, 반드시 `Dto(Data Transfer Object)`로 변환하여 반환하세요.
- **Transaction:** 데이터를 변경하는 서비스 메서드에는 `@Transactional`을 명시하세요. 읽기 전용에는 `@Transactional(readOnly = true)`를 붙여주세요.

## 3. 테스트 (Testing)
- **JUnit 5:** JUnit 4 대신 JUnit 5(Jupiter)를 사용하세요.
- **Mocking:** `Mockito`를 사용하여 단위 테스트를 작성하고, 통합 테스트는 `@SpringBootTest`를 사용하세요.
- **BDD:** `given`, `when`, `then` 주석을 사용하여 테스트 흐름을 명확히 하세요.

## 4.문서화(Documentation)
- **JavaDoc:** 공개 메서드와 클래스에는 JavaDoc 주석을 추가하여 설명을 달아주세요.
- 기능을 구현한 후 별도의 문서화 요청이 없는 한, 코드 내 주석과 JavaDoc에만 문서화를 제한해주세요.
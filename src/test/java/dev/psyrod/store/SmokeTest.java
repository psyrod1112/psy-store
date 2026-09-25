package dev.psyrod.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest // @SpringBootTest는 스프링 부트의 통합 테스트를 지원하는 어노테이션이다.
// (즉, @SpringBootTest를 붙이면 스프링 부트의 모든 빈을 로드하여 테스트를 수행할 수 있다.)
@AutoConfigureMockMvc // @AutoConfigureMockMvc는 MockMvc를 자동으로 구성하는 어노테이션이다.
// (즉, @AutoConfigureMockMvc를 붙이면 MockMvc를 주입받아 테스트를 수행할 수 있다.)
@Import(TestcontainersConfiguration.class) // @Import는 다른 설정 클래스를 가져오는 어노테이션이다.
// (즉, @Import를 붙이면 TestcontainersConfiguration 클래스를 가져와서 테스트를 수행할 수 있다.)
class SmokeTest {

    @Autowired MockMvc mockMvc;
    // @Autowired는 스프링의 의존성 주입을 지원하는 어노테이션이다.
    // (즉, @Autowired를 붙이면 스프링이 관리하는 빈을 주입받아 테스트를 수행할 수 있다.)
    // MockMvc는 스프링 MVC 테스트를 지원하는 클래스이다.
    // (즉, MockMvc를 사용하면 컨트롤러를 실제로 호출하지 않고도 테스트를 수행할 수 있다.)
    @Autowired JdbcClient jdbcClient;
    // JdbcClient는 JDBC를 사용하여 데이터베이스에 접근하는 클래스이다.
    // (즉, JdbcClient를 사용하면 데이터베이스에 쿼리를 실행하고 결과를 가져올 수 있다.)

    @Test // @Test는 JUnit 5에서 테스트 메서드를 나타내는 어노테이션이다.
    // (즉, @Test를 붙이면 해당 메서드가 테스트 메서드임을 나타내고, 테스트를 수행할 수 있다.)
    @DisplayName("헬스 체크가 UP을 반환한다")
    // @DisplayName은 JUnit 5에서 테스트 메서드의 이름을 지정하는 어노테이션이다.
    // (즉, @DisplayName을 붙이면 해당 메서드의 이름을 지정할 수 있고, 테스트 결과를 확인할 때 가독성을 높일 수 있다.)
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
                // mockMvc.perform(get("/actuator/health"))는 /actuator/health 엔드포인트에 GET 요청을 보내는 코드이다.
                // .andExpect(status().isOk())는 응답 상태 코드가 200 OK인지 확인하는 코드이다.
                // .andExpect(jsonPath("$.status").value("UP"))는 응답 본문의 JSON 경로 $.status의 값이 UP인지 확인하는 코드이다.
    }

    @Test
    @DisplayName("Flyway 마이그레이션이 성공적으로 적용되어 있다")
    void flywayMigrationsApplied() {
        Integer applied = jdbcClient
                .sql("SELECT count(*) FROM flyway_schema_history WHERE success = true")
                .query(Integer.class)
                .single();
                // jdbcClient.sql("SELECT count(*) FROM flyway_schema_history WHERE success = true")는 
                // flyway_schema_history 테이블에서 성공적으로 적용된 마이그레이션의 수를 조회하는 SQL 쿼리이다.
                // .query(Integer.class) 는 쿼리 결과를 Integer 타입으로 매핑하는 코드이다.
                // .single()은 쿼리 결과가 단일 행(single row)임을 나타내는 코드이다.
                //  (즉, 쿼리 결과가 단일 행이면 해당 행의 값을 반환하고, 그렇지 않으면 예외를 발생시킨다.)   

        assertThat(applied).isPositive();
        // assertThat(applied).isPositive()는 applied 값이 양수인지 확인하는 코드이다.
        // (즉, applied 값이 양수이면 테스트가 성공하고, 그렇지 않으면 테스트가 실패한다.)
    }

    @Test
    @DisplayName("스키마의 모든 테이블이 생성되어 있다")
    void allTablesExist() {
        List<String> expected = List.of(
                "users", "apps", "app_releases", "orders", "payments", "entitlements");
                // List.of("users", "apps", "app_releases", "orders", "payments", "entitlements")는
                // 스키마에 존재해야 하는 테이블 이름을 담은 리스트를 생성하는 코드이다.
                // (즉, expected 리스트에는 스키마에 존재해야 하는 테이블 이름이 담겨 있다.)

        List<String> actual = jdbcClient
                .sql("""
                     SELECT table_name FROM information_schema.tables
                     WHERE table_schema = 'public'
                     """)
                .query(String.class)
                .list();
                // jdbcClient.sql("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")는
                // information_schema.tables 테이블에서 public 스키마에 존재하는 테이블 이름을 조회하는 SQL 쿼리이다.
                // .query(String.class)는 쿼리 결과를 String 타입으로 매핑하는 코드이다.
                // .list()는 쿼리 결과를 리스트로 반환하는 코드이다.

        assertThat(actual).containsAll(expected);
        // assertThat(actual).containsAll(expected)는 actual 리스트에 expected 리스트의 모든 요소가 포함되어 있는지 확인하는 코드이다.
    }
}
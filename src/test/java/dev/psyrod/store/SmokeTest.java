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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SmokeTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbcClient;

    @Test
    @DisplayName("헬스 체크가 UP을 반환한다")
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Flyway 마이그레이션이 성공적으로 적용되어 있다")
    void flywayMigrationsApplied() {
        Integer applied = jdbcClient
                .sql("SELECT count(*) FROM flyway_schema_history WHERE success = true")
                .query(Integer.class)
                .single();

        assertThat(applied).isPositive();
    }

    @Test
    @DisplayName("스키마의 모든 테이블이 생성되어 있다")
    void allTablesExist() {
        List<String> expected = List.of(
                "users", "apps", "app_releases", "orders", "payments", "entitlements");

        List<String> actual = jdbcClient
                .sql("""
                     SELECT table_name FROM information_schema.tables
                     WHERE table_schema = 'public'
                     """)
                .query(String.class)
                .list();

        assertThat(actual).containsAll(expected);
    }
}
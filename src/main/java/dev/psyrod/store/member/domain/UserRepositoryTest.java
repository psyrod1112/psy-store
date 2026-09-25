public class UserRepositoryTest {
    
}
package dev.psyrod.store.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.psyrod.store.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("회원을 저장하면 id와 생성 시각이 채워진다")
    void saveUser() {
        User saved = userRepository.save(User.register("Psy@Example.com", "hashed"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("이메일은 소문자로 정규화되어 저장된다")
    void emailIsNormalized() {
        userRepository.save(User.register("  Psy@Example.COM ", "hashed"));

        assertThat(userRepository.existsByEmail("psy@example.com")).isTrue();
        assertThat(userRepository.findByEmail("psy@example.com")).isPresent();
    }

    @Test
    @DisplayName("같은 이메일로 두 번 가입할 수 없다")
    void emailMustBeUnique() {
        userRepository.saveAndFlush(User.register("dup@example.com", "hashed"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(User.register("dup@example.com", "other")))
                .isInstanceOf(Exception.class);
    }
}
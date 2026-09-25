package dev.psyrod.store.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // @Configuration은 스프링 컨테이너에 빈을 등록하는 역할을 한다. 
// (즉, @Component와 동일한 역할)
@EnableWebSecurity // @EnableWebSecurity는 스프링 시큐리티를 활성화하는 역할을 한다.
// (즉, @EnableWebSecurity를 붙이면 스프링 시큐리티가 적용된다.)
public class SecurityConfig {

    @Bean
    // @Bean은 스프링 컨테이너에 빈을 등록하는 역할을 한다.
    // (즉, @Bean을 붙이면 해당 메서드의 반환값이 스프링 컨테이너에 빈으로 등록된다.)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // SecurityFilterChain은 스프링 시큐리티의 필터 체인을 구성하는 역할을 한다.
        // 이 함수가 실행되는 시점은 스프링 시큐리티가 초기화될 때이다.
        // (즉, 스프링 시큐리티가 초기화될 때 이 함수가 실행되고, SecurityFilterChain이 생성된다.)
        http
            // JWT 기반이므로 세션을 만들지 않는다 → CSRF 토큰도 불필요
            .csrf(csrf -> csrf.disable())
            // 세션을 만들지 않음 → JWT 기반이므로 세션을 만들지 않는다.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 브라우저 기본 인증 팝업과 로그인 폼을 끈다 (API 서버이므로)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/apps/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
        // http.build()는 SecurityFilterChain을 생성하는 코드이다.
        // (즉, http.build()를 호출하면 SecurityFilterChain이 생성되고, 스프링 시큐리티가 적용된다.)
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // @Bean은 스프링 컨테이너에 빈을 등록하는 역할을 한다.
        // (즉, @Bean을 붙이면 해당 메서드의 반환값이 스프링 컨테이너에 빈으로 등록된다.)
        return new BCryptPasswordEncoder();
        // new BCryptPasswordEncoder()는 BCryptPasswordEncoder를 생성하는 코드이다.
        // (즉, new BCryptPasswordEncoder()를 호출하면 BCryptPasswordEncoder가 생성되고, 
        // 스프링 시큐리티에서 비밀번호를 암호화할 때 사용된다.)
    }
}
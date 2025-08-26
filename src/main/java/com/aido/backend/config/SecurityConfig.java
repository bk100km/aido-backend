package com.aido.backend.config;

import com.aido.backend.oauth.CustomOAuth2UserService;
import com.aido.backend.oauth.OAuth2AuthenticationFailureHandler;
import com.aido.backend.oauth.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired(required = false)
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired(required = false)
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", 
                                "/api/users/**", "/swagger-ui/**", 
                                "/v3/api-docs/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            );

        // OAuth2 설정이 있을 때만 OAuth2 로그인 활성화
        if (hasValidOAuthProviders() && 
            customOAuth2UserService != null && 
            oAuth2AuthenticationSuccessHandler != null && 
            oAuth2AuthenticationFailureHandler != null) {
            
            http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            );
        }

        http.logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );

        return http.build();
    }
    
    private boolean hasValidOAuthProviders() {
        // 환경 변수 확인
        String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        String googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        String kakaoClientId = System.getenv("KAKAO_CLIENT_ID");
        String kakaoClientSecret = System.getenv("KAKAO_CLIENT_SECRET");
        
        boolean hasGoogle = googleClientId != null && !googleClientId.equals("disabled") && 
                           googleClientSecret != null && !googleClientSecret.equals("disabled");
        
        boolean hasKakao = kakaoClientId != null && !kakaoClientId.equals("disabled") && 
                          kakaoClientSecret != null && !kakaoClientSecret.equals("disabled");
        
        return hasGoogle || hasKakao;
    }
}
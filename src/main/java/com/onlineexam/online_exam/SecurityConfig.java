package com.onlineexam.online_exam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .userDetailsService(userDetailsService)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/register-college", "/register-student", "/register-faculty",
                    "/css/**", "/js/**", "/images/**", "/h2-console/**", "/api/firebase-config"
                ).permitAll()
                .requestMatchers("/platform/**").hasRole("PLATFORM_ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "COLLEGE_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("/faculty/**").hasAnyRole("FACULTY", "PLATFORM_ADMIN")
                .requestMatchers("/student/**", "/dashboard", "/exams", "/exam/**", "/results", "/profile").hasAnyRole("STUDENT", "ADMIN", "PLATFORM_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isPlatformAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_COLLEGE_ADMIN"));
                    boolean isFaculty = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));

                    if (isPlatformAdmin) {
                        response.sendRedirect("/platform/dashboard");
                    } else if (isAdmin) {
                        response.sendRedirect("/admin/dashboard");
                    } else if (isFaculty) {
                        response.sendRedirect("/faculty/dashboard");
                    } else {
                        response.sendRedirect("/dashboard");
                    }
                })
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}

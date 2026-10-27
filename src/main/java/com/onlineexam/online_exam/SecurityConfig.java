package com.onlineexam.online_exam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {

        UserDetails student = User.builder()
                .username("user")
                .password(encoder.encode("Exam@123"))
                .roles("STUDENT")
                .build();

        UserDetails faculty = User.builder()
                .username("faculty")
                .password(encoder.encode("Faculty@123"))
                .roles("FACULTY")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("Admin@123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(student, faculty, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/h2-console/**", "/api/firebase-config").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/faculty/**").hasRole("FACULTY")
                .requestMatchers("/student/**", "/dashboard", "/exams", "/exam/**", "/results", "/profile").hasAnyRole("STUDENT", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    boolean isFaculty = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));

                    if (isAdmin) {
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
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}

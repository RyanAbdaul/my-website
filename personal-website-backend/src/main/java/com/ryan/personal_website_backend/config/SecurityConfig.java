package com.ryan.personal_website_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // LinkedIn redirects the browser here without our site's JWT after OAuth consent.
                        .requestMatchers(HttpMethod.GET, "/linkedin/callback", "/linkedin/callback/**").permitAll()
                        .requestMatchers(
                                "/auth/students",
                                "/auth/members",
                                "/auth/login",
                                "/actuator/health"
                        ).permitAll()
                        // Swagger / OpenAPI docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/webjars/**",
                                "/docs",
                                "/docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/courses", "/courses/**", "/projects", "/projects/**",
                                "/work-experiences", "/certificates",
                                "/lessons", "/lessons/**",
                                "/articles", "/articles/**",
                                "/tags", "/tags/**",
                                "/members", "/members/**",
                                "/uploads", "/uploads/**",
                                "/reviews/course/**",
                                "/sections", "/sections/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/auth/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/members/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/members/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/members/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/students").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/students").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/enrollments/courses/*/me").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/courses").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.PUT, "/courses/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.POST, "/lessons").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.PUT, "/lessons/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.DELETE, "/lessons/**").hasAnyRole("ADMIN", "MEMBER")
                        // Sections - POST, PUT, PATCH for ADMIN and MEMBER
                        .requestMatchers(HttpMethod.POST, "/sections/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.PUT, "/sections/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.PATCH, "/sections/**").hasAnyRole("ADMIN", "MEMBER")
                        // Sections - DELETE only for ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/sections/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.POST, "/reviews").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**").hasRole("ADMIN")
                        .requestMatchers("/linkedin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/articles", "/tags", "/projects", "/work-experiences", "/certificates").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/articles/**", "/projects/**", "/work-experiences/**", "/certificates/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/articles/**", "/tags/**", "/projects/**", "/work-experiences/**", "/certificates/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
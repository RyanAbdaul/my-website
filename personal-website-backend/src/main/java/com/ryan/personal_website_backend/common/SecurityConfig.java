package com.ryan.personal_website_backend.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.public-site-url}")
    private String publicSiteUrl;

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
                                "/auth/login",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/courses", "/courses/**", "/projects", "/projects/**",
                                "/profile",
                                "/work-experiences", "/certificates",
                                "/lessons", "/lessons/**",
                                "/articles", "/articles/**",
                                "/tags", "/tags/**",
                                "/uploads", "/uploads/**",
                                "/reviews/course/**",
                                "/sections", "/sections/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/students").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/students").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/enrollments/courses/*/me").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/courses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/lessons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/lessons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/lessons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sections/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/sections/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/sections/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sections/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/reviews").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**").hasRole("ADMIN")
                        .requestMatchers("/linkedin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/articles", "/tags", "/projects", "/work-experiences", "/certificates").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/articles/**", "/projects/**", "/work-experiences/**", "/certificates/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/articles/**", "/tags/**", "/projects/**", "/work-experiences/**", "/certificates/**").hasRole("ADMIN")
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
        configuration.setAllowedOrigins(List.of(publicSiteUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

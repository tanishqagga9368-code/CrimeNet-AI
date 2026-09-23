package criminal_network_intelligence.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AppUserService appUserService;
    private final String allowedOriginsStr;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AppUserService appUserService,
            @Value("${cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,http://127.0.0.1:3000,https://crime-net-ai-1dsp.vercel.app}") String allowedOriginsStr
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.appUserService = appUserService;
        this.allowedOriginsStr = allowedOriginsStr;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: full authentication required with valid JWT token\",\"status\":401}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Access Denied: Insufficient permissions for your role\",\"status\":403}");
                        })
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        // Authentication endpoints (login, etc.)
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Internal AI service integration
                        .requestMatchers(
                                "/internal/ai/**"
                        )
                        .permitAll()

                        // Role-restricted actions
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/cases/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/entity-resolution/merge"
                        )
                        .hasAnyRole("ADMIN", "SENIOR_OFFICER")

                        // All protected investigation APIs require authentication
                        .requestMatchers(
                                "/api/dashboard",
                                "/api/network/**",
                                "/api/cases/**",
                                "/api/evidence/**",
                                "/api/ingestion/**",
                                "/api/ai/**",
                                "/api/analytics/**",
                                "/api/graph/**",
                                "/api/timeline/**",
                                "/api/cross-case/**",
                                "/api/entity-resolution/**",
                                "/api/location/**",
                                "/api/evidence-chain/**",
                                "/api/audit/**",
                                "/api/blockchain/**",
                                "/api/alerts/**",
                                "/api/copilot/**",
                                "/api/reports/**",
                                "/api/integration/**"
                        )
                        .authenticated()

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        /*
         * Spring Security 7 / Spring Boot 4:
         * DaoAuthenticationProvider requires
         * UserDetailsService through its constructor.
         */
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(appUserService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOriginsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedOriginPatterns(
                List.of(
                        "https://*.vercel.app"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setExposedHeaders(
                List.of("Authorization", "Content-Disposition")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
package com.example.annita.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/api/auth/check-username",
                                                                "/api/newsletter/subscribe",
                                                                "/api/newsletter/unsubscribe/**",
                                                                "/api/events",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/docs")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                                .exceptionHandling(ex -> ex
                                        .authenticationEntryPoint(authenticationEntryPoint())
                                        .accessDeniedHandler(accessDeniedHandler()))
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
                return NimbusJwtDecoder.withSecretKey(secretKey).build();
        }

        @Bean
        public JwtEncoder jwtEncoder() {
                JWK jwk = new OctetSequenceKey.Builder(jwtSecret.getBytes())
                                .algorithm(JWSAlgorithm.HS256)
                                .keyUse(KeyUse.SIGNATURE) //
                                .keyID(UUID.randomUUID().toString()) // 
                                .build();
                JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
                return new NimbusJwtEncoder(jwks);
        }

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
            return (request, response, authException) -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String message = buildAuthErrorMessage(authException);
                Map<String, String> body = new LinkedHashMap<>();
                body.put("message", message);
                response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            };
        }

        @Bean
        public AccessDeniedHandler accessDeniedHandler() {
            return (request, response, accessDeniedException) -> {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, String> body = new LinkedHashMap<>();
                body.put("message", "Acesso negado. Você não tem permissão para acessar este recurso.");
                response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            };
        }

        private String buildAuthErrorMessage(AuthenticationException ex) {
            String msg = extractMessage(ex);
            if (msg == null) {
                return "Token de autenticação ausente. Envie o token no header Authorization: Bearer <token>";
            }
            String lower = msg.toLowerCase();
            if (lower.contains("expired")) {
                return "Token de autenticação expirado. Faça login novamente.";
            }
            if (lower.contains("malformed") || lower.contains("bad jwt")) {
                return "Token de autenticação mal formatado.";
            }
            if (lower.contains("signature")) {
                return "Token de autenticação inválido ou violado.";
            }
            return "Token de autenticação ausente ou inválido. Verifique o header Authorization: Bearer <token>";
        }

        private String extractMessage(Throwable t) {
            if (t == null) return null;
            String msg = t.getMessage();
            if (msg != null && !msg.isBlank()) return msg;
            return extractMessage(t.getCause());
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.setAllowedMethods(List.of("*"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
                return new io.swagger.v3.oas.models.OpenAPI()
                                .info(new io.swagger.v3.oas.models.info.Info()
                                                .title("Annita API")
                                                .version("1.0.0")
                                                .description("""
                                                                API for Annita Event Platform.
                                                                
                                                                Authentication is done via JWT Bearer token.
                                                                The token must be sent in the **Authorization** header as `Bearer <token>`.
                                                                Never pass the token as a query parameter or in the request body.
                                                                """))
                                .components(new io.swagger.v3.oas.models.Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                                                                .name("bearerAuth")
                                                                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}

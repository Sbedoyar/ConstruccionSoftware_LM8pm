package app.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                    "{\"status\":401,\"message\":\"No autenticado: debe iniciar sesión para acceder a este recurso\",\"errors\":null}"                            );
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                    "{\"status\":403,\"message\":\"Acceso denegado: no tiene permisos para este recurso\",\"errors\":null}"
                        );
                    })
                )
                .authorizeHttpRequests(auth -> auth

                        // Login público
                        .requestMatchers("/auth/**").permitAll()

                        // Solo el empleado comercial puede crear usuarios
                        .requestMatchers("/users/**").hasRole("COMMERCIAL_EMPLOYEE")

                        .requestMatchers("/product-catalog/**").hasRole("COMMERCIAL_EMPLOYEE")

                        // Comercial
                        .requestMatchers("/commercial/**").hasRole("COMMERCIAL_EMPLOYEE")

                        // Ventanilla
                        .requestMatchers("/teller/**").hasRole("TELLER_EMPLOYEE")

                        // Analista interno
                        .requestMatchers("/internal-analyst/**").hasRole("INTERNAL_ANALYST")

                        // Operador de empresa
                        .requestMatchers("/company-operator/**").hasRole("COMPANY_OPERATOR")

                        // Supervisor de empresa
                        .requestMatchers(HttpMethod.POST, "/company-supervisor/users/delegate-operator").hasAnyRole(
                                "COMPANY_SUPERVISOR",
                                "BUSINESS_CUSTOMER"
                        )

                        .requestMatchers("/company-supervisor/**").hasRole("COMPANY_SUPERVISOR")

                        // Clientes
                        .requestMatchers(HttpMethod.POST, "/customers/transfers").hasRole("INDIVIDUAL_CUSTOMER")

                        .requestMatchers("/customers/**").hasAnyRole(
                                "INDIVIDUAL_CUSTOMER",
                                "BUSINESS_CUSTOMER"
                        )

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
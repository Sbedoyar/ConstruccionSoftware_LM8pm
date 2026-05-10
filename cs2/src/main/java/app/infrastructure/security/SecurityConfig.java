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
                .authorizeHttpRequests(auth -> auth

                        // Login público
                        .requestMatchers("/auth/**").permitAll()

                        // Temporalmente dejamos users abierto para seguir creando usuarios de prueba
                        .requestMatchers("/users/**").hasRole("COMMERCIAL_EMPLOYEE")

                        // Comercial
                        .requestMatchers("/commercial/**").hasRole("COMMERCIAL_EMPLOYEE")

                        // Ventanilla
                        .requestMatchers("/teller/**").hasRole("TELLER_EMPLOYEE")

                        // Analista interno
                        .requestMatchers("/internal-analyst/**").hasRole("INTERNAL_ANALYST")

                        // Operador de empresa
                        .requestMatchers("/company-operator/**").hasRole("COMPANY_OPERATOR")

                        // Supervisor de empresa
                        .requestMatchers("/company-supervisor/**").hasAnyRole(
                                "COMPANY_SUPERVISOR",
                                "BUSINESS_CUSTOMER"
                        )

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
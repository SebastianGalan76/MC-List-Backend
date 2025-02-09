package com.coresaken.mcserverlist.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(request -> {
            request.requestMatchers(
                    "/auth/**",
                            "/user",
                            "/dont-sleep-buddy",
                            "/mode/listAll",
                            "/version/listAll",
                            "/add-new-server",
                            "/server/payment/**",
                            "/banner/payment/**",
                            "/banners",
                            "/banner",
                            "/payment-notification",
                            "/server/list/**",
                            "/server/*",
                            "/server/**",
                            "/server/*/promote",
                            "/server/search/*",
                            "/random",
                            "/uploads/banners/*",
                            "/vote",
                            "/api/vote/check/**",
                            "/api/vote/confirm/**")
                    .permitAll()
                    .anyRequest().authenticated();
        });

        http.sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

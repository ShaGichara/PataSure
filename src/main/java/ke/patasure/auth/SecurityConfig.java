
package ke.patasure.auth;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth

            // Public pages and resources
            .requestMatchers(
                "/",
                "/index.html",
                "/buyers.html",
                "/sellers.html",
                "/login.html",
                "/access-denied.html",
                "/csrf",
                "/verify.html",
                "/css/**",
                "/js/**",
                "/images/**"
            ).permitAll()

            // Public product details
            .requestMatchers(
                HttpMethod.GET,
                "/api/products",
                "/api/products/*"
            ).permitAll()

            // Public QR and product history
            .requestMatchers(
                HttpMethod.GET,
                "/api/products/*/qr",
                "/api/products/*/history",
                "/api/products/*/history/integrity"
            ).permitAll()

            // Role-restricted dashboards
            .requestMatchers("/admin.html").hasRole("ADMIN")
            .requestMatchers("/seller.html").hasAnyRole("SELLER", "ADMIN")

            // Admin management APIs
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Seller and admin product registration
            .requestMatchers(HttpMethod.POST, "/api/products")
                .hasAnyRole("SELLER", "ADMIN")

            // Seller and admin history updates
            .requestMatchers(HttpMethod.POST, "/api/products/*/history")
                .hasAnyRole("SELLER", "ADMIN")

            // Everything else requires authentication
            .anyRequest().authenticated()
        );

        // Friendly page for browser access-denied responses.
        // API requests retain a proper JSON 403 response.
        http.exceptionHandling(exception -> exception
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                if (request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"error\":\"forbidden\",\"message\":\"You do not have permission to access this resource.\"}"
                    );
                } else {
                    response.sendRedirect("/access-denied.html");
                }
            })
        );

        http.formLogin(form -> form
            .loginPage("/login.html")
            .loginProcessingUrl("/login")
            .successHandler((request, response, authentication) -> {

                boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    response.sendRedirect("/admin.html");
                } else {
                    response.sendRedirect("/seller.html");
                }
            })
            .permitAll()
        );

        http.logout(logout -> logout
            .logoutSuccessUrl("/login.html?logout")
            .permitAll()
        );

        return http.build();
    }
}

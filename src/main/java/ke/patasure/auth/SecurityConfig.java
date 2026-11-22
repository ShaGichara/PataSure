
package ke.patasure.auth;

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
                "/", "/index.html", "/login.html", "/csrf",
                "/verify.html", "/css/**", "/js/**", "/images/**"
            ).permitAll()

            // Public product details: exact endpoint matching
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

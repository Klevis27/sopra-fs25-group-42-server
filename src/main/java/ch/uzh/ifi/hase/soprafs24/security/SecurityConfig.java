package ch.uzh.ifi.hase.soprafs24.security;

import ch.uzh.ifi.hase.soprafs24.jwt.JwtFilter;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()  // Enable CORS
                .csrf().disable()  // CSRF deaktivieren (wenn kein Session-Login)
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/users", "/login", "/logout", "/edit", "/vaults", "vaults/**").permitAll()  // Register/Login/Logout erlauben
                .antMatchers(HttpMethod.GET, "/**").permitAll()  // Alle GET-Anfragen erlauben
                .antMatchers(HttpMethod.PUT, "/users/**").permitAll()
                .anyRequest().authenticated()  // Alle anderen Anfragen brauchen Auth
                .and()
                .headers().frameOptions().sameOrigin()  // H2 Console in iframe erlauben
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized JWT Exception");
                })
                .and()
                .formLogin().disable()  // Verhindert automatische Login-Redirects
                .logout().disable()  // Verhindert automatische Logout-Redirects
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);  // JWT-Filter

        return http.build();
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://sopra-fs25-group-42-client.vercel.app"));  // Allow frontend origin
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

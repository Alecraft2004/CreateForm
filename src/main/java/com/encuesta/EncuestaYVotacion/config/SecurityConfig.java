package com.encuesta.EncuestaYVotacion.config;

import com.encuesta.EncuestaYVotacion.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  private final UsuarioDetailsService uds;

  public SecurityConfig(UsuarioDetailsService uds) {
    this.uds = uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @SuppressWarnings("deprecation")
  public DaoAuthenticationProvider authProvider(PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    provider.setPasswordEncoder(encoder);
    return provider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
    http
      .authorizeHttpRequests(reg -> reg
        .requestMatchers("/", "/index", "/login", "/register", "/css/**", "/js/**").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .formLogin(login -> login
        .loginPage("/login").permitAll()
        // Si tu input del login se llama "correo":
        // .usernameParameter("correo")
        // Si el input de password no se llama "password":
        // .passwordParameter("password")
        .defaultSuccessUrl("/", false)
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login?logout").permitAll()
      )
      .csrf(csrf -> csrf
        // Expone el token en una cookie accesible por JS: 'XSRF-TOKEN'
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      )
      .authenticationProvider(authProvider);

    return http.build();
  }
}


package br.com.cpa.questionario.config;

import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(a -> a
              .requestMatchers("/login", "/perform_login", "/error",
                               "/css/**", "/js/**", "/images/**").permitAll()
              .anyRequest().authenticated()
          )
          .formLogin(f -> f
              .loginPage("/login")                 // GET → mostra a tela
              .loginProcessingUrl("/perform_login")// POST → processa login
              .defaultSuccessUrl("/home", true)
              .failureUrl("/login?error=true")
              .permitAll()
          )
          .logout(l -> l
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout=true")
              .permitAll()
          )
          .exceptionHandling(e -> e.accessDeniedPage("/error"))
          .csrf(c -> c.disable()); // só para testes locais
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User u = userRepository.findByUsername(username);
            if (u == null) throw new UsernameNotFoundException("Usuário não encontrado");
            var b = org.springframework.security.core.userdetails.User
                     .withUsername(u.getUsername())
                     .password(u.getPassword())
                     .roles(u.getRole().startsWith("ROLE_")
                            ? u.getRole().substring(5)
                            : u.getRole());
            return b.build();
        };
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        var amb = http.getSharedObject(AuthenticationManagerBuilder.class);
        amb.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        return amb.build();
    }
}

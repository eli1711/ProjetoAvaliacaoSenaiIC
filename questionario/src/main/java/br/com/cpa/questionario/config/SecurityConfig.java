package br.com.cpa.questionario.config;

import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ================== PÚBLICO / LOGIN ==================
                .requestMatchers(
                        "/login",
                        "/perform_login",
                        "/error",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                ).permitAll()

                // ================== ROTAS DO ALUNO (AVALIAÇÕES) ==================
                // lista de avaliações abertas para o aluno
                .requestMatchers("/avaliacoes/disponiveis")
                    .authenticated()
                // responder avaliação aplicada
                .requestMatchers("/avaliacoes/*/responder/**")
                    .authenticated()

                // ================== USUÁRIOS (APENAS ADMIN) ==================
                .requestMatchers("/users/**")
                    .hasAuthority("ROLE_ADMIN")

                // ================== TURMAS ==================
                // listar turmas -> qualquer autenticado
                .requestMatchers(HttpMethod.GET, "/turmas")
                    .authenticated()
                // criar / editar turmas -> ADMIN ou COORDENADOR
                .requestMatchers(HttpMethod.GET, "/turmas/new", "/turmas/*/edit")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDENADOR")
                .requestMatchers(HttpMethod.POST, "/turmas/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDENADOR")

                // ================== QUESTIONÁRIOS & AVALIAÇÕES (CRUD) ==================
                // telas de configuração de questionário + telas de gestão de avaliações
                .requestMatchers("/questionnaires/**", "/avaliacoes/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_PROFESSOR")

                // ================== HOME ==================
                .requestMatchers("/", "/home")
                    .authenticated()

                // ================== QUALQUER OUTRA ROTA ==================
                .anyRequest().authenticated()
            )
            .formLogin(f -> f
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
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
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // ===================== USER DETAILS =====================
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User u = userRepository.findByUsername(username);
            if (u == null) {
                throw new UsernameNotFoundException("Usuário não encontrado");
            }

            // valores no banco devem ser: ROLE_ADMIN, ROLE_ALUNO, ROLE_PROFESSOR, ROLE_COORDENADOR
            return org.springframework.security.core.userdetails.User
                    .withUsername(u.getUsername())
                    .password(u.getPassword())
                    .authorities(u.getRole())
                    .build();
        };
    }

    // ===================== PASSWORD ENCODER =====================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===================== AUTH MANAGER =====================
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder amb = http.getSharedObject(AuthenticationManagerBuilder.class);
        amb.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return amb.build();
    }
}

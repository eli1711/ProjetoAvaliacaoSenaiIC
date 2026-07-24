package br.com.cpa.questionario.config;

import br.com.cpa.questionario.model.PerfilSistema;
import br.com.cpa.questionario.model.Permissao;
import br.com.cpa.questionario.model.StatusAluno;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final MustChangePasswordFilter mustChangePasswordFilter;

    public SecurityConfig(UserRepository userRepository,
                          MustChangePasswordFilter mustChangePasswordFilter) {
        this.userRepository = userRepository;
        this.mustChangePasswordFilter = mustChangePasswordFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/perform_login",
                                "/error",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/users/aluno/registro",
                                "/users/aluno/registrar"
                        ).permitAll()

                        .requestMatchers("/minha-conta/senha/**")
                        .authenticated()

                        .requestMatchers("/avaliacoes/disponiveis/**")
                        .hasAuthority(Permissao.RESPONDER_AVALIACAO.name())
                        .requestMatchers("/avaliacoes/*/responder/**")
                        .hasAuthority(Permissao.RESPONDER_AVALIACAO.name())
                        .requestMatchers("/questionnaires/available/**")
                        .hasAuthority(Permissao.RESPONDER_AVALIACAO.name())

                        .requestMatchers("/dev", "/dev/**", "/instituicoes", "/instituicoes/**")
                        .hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers("/users/**")
                        .hasAuthority(Permissao.GERENCIAR_USUARIOS.name())
                        .requestMatchers("/turmas/**", "/alunos/**")
                        .hasAuthority(Permissao.GERENCIAR_TURMAS_CURSOS.name())
                        .requestMatchers("/questionnaires/**")
                        .hasAnyAuthority(
                                Permissao.CRIAR_QUESTIONARIO.name(),
                                Permissao.EDITAR_QUESTIONARIO.name()
                        )
                        .requestMatchers("/avaliacoes/*/csv-template")
                        .hasAuthority(Permissao.EXPORTAR_RELATORIOS.name())
                        .requestMatchers(
                                "/analise/avaliacoes/export-resumo",
                                "/analise/avaliacoes/export-perguntas"
                        )
                        .hasAuthority(Permissao.EXPORTAR_RELATORIOS.name())
                        .requestMatchers("/avaliacoes/*/respostas")
                        .hasAuthority(Permissao.VISUALIZAR_RESULTADOS.name())
                        .requestMatchers("/avaliacoes/**")
                        .hasAuthority(Permissao.PUBLICAR_AVALIACAO.name())
                        .requestMatchers("/analise/**")
                        .hasAuthority(Permissao.VISUALIZAR_RESULTADOS.name())
                        .requestMatchers("/planos-acao/**")
                        .hasAuthority(Permissao.GERENCIAR_PLANOS_ACAO.name())
                        .requestMatchers("/auditoria/**")
                        .hasAuthority(Permissao.GERENCIAR_USUARIOS.name())

                        .requestMatchers("/home").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterAfter(mustChangePasswordFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("Usuario nao encontrado");
            }

            String role = (user.getRole() == null || user.getRole().isBlank())
                    ? "ROLE_USER"
                    : user.getRole();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role));
            PerfilSistema.permissoesPorAuthority(role).stream()
                    .map(permissao -> new SimpleGrantedAuthority(permissao.name()))
                    .forEach(authorities::add);

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .disabled(user.getStatus() == StatusAluno.INATIVO)
                    .accountLocked(user.isTemporarilyLocked())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}

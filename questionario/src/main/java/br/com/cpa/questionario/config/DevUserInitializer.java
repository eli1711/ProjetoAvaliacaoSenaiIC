package br.com.cpa.questionario.config;

import br.com.cpa.questionario.exception.PasswordPolicyException;
import br.com.cpa.questionario.model.StatusAluno;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import br.com.cpa.questionario.service.PasswordPolicyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(prefix = "app.dev-user", name = "enabled", havingValue = "true")
public class DevUserInitializer {

    @Bean
    public CommandLineRunner devUser(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     PasswordPolicyService passwordPolicyService,
                                     @Value("${app.dev-user.username}") String username,
                                     @Value("${app.dev-user.password}") String password,
                                     @Value("${app.dev-user.email}") String email,
                                     @Value("${app.dev-user.name}") String name) {
        return args -> {
            if (password == null || password.isBlank()) {
                throw new IllegalStateException("app.dev-user.password deve ser informado quando o usuario dev estiver ativo.");
            }

            User user = userRepository.findByUsername(username);
            if (user == null) {
                user = new User();
                user.setUsername(username);
            }

            user.setName(name);
            user.setEmail(email);
            user.setRa(null);
            user.setRole("ROLE_SUPER_ADMIN");
            user.setStatus(StatusAluno.ATIVO);
            user.setTurma(null);
            user.setInstituicao(null);

            try {
                passwordPolicyService.validar(password, user);
            } catch (PasswordPolicyException ex) {
                throw new IllegalStateException("Senha do usuario dev invalida: " + ex.getMessage(), ex);
            }

            user.setPassword(passwordEncoder.encode(password));
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setMustChangePassword(false);
            user.resetLoginFailures();
            userRepository.save(user);
        };
    }
}

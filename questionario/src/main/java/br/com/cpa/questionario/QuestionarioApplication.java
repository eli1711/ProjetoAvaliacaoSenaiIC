package br.com.cpa.questionario;

import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class QuestionarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionarioApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserRepository userRepository) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            if (userRepository.findByUsername("aluno") == null) {
                User u = new User();
                u.setUsername("aluno");
                u.setPassword(encoder.encode("123456"));
                u.setName("Aluno Padrão");
                u.setRole("ROLE_ALUNO");
                u.setClassName("TurmaA");
                userRepository.save(u);
            }

            if (userRepository.findByUsername("prof") == null) {
                User u = new User();
                u.setUsername("prof");
                u.setPassword(encoder.encode("123456"));
                u.setName("Professor Padrão");
                u.setRole("ROLE_PROFESSOR");
                u.setClassName("Coordenação");
                userRepository.save(u);
            }

            if (userRepository.findByUsername("admin") == null) {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin"));
                u.setName("Administrador");
                u.setRole("ROLE_ADMIN");
                u.setClassName("Admin");
                userRepository.save(u);
            }
        };
    }
}

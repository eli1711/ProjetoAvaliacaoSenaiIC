package br.com.cpa.questionario.config;

import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.model.StatusAluno;
import br.com.cpa.questionario.model.Turma;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.InstituicaoRepository;
import br.com.cpa.questionario.repository.TurmaRepository;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataInitializer {

    @Bean
    public CommandLineRunner demoUsers(UserRepository userRepository,
                                       TurmaRepository turmaRepository,
                                       InstituicaoRepository instituicaoRepository,
                                       PasswordEncoder encoder,
                                       @Value("${app.demo-data.admin-password}") String adminPassword,
                                       @Value("${app.demo-data.aluno-password}") String alunoPassword,
                                       @Value("${app.demo-data.professor-password}") String professorPassword) {
        return args -> {
            validarSenhaDemo(adminPassword, "app.demo-data.admin-password");
            validarSenhaDemo(alunoPassword, "app.demo-data.aluno-password");
            validarSenhaDemo(professorPassword, "app.demo-data.professor-password");

            Instituicao instituicao = instituicaoRepository
                    .findByIdentificadorInstitucional("DEMO-CPA")
                    .orElseGet(() -> {
                        Instituicao nova = new Instituicao();
                        nova.setNome("Instituicao Demo CPA");
                        nova.setIdentificadorInstitucional("DEMO-CPA");
                        nova.setResponsavelInstitucional("Administrador Demo");
                        nova.setPeriodoLetivoAtual("2025/2");
                        return instituicaoRepository.save(nova);
                    });

            Turma turmaPadrao = turmaRepository.findByNomeAndInstituicaoId("ADS 2 Semestre 2025", instituicao.getId())
                    .orElseGet(() -> {
                        Turma turma = new Turma();
                        turma.setNome("ADS 2 Semestre 2025");
                        turma.setCurso("Analise e Desenvolvimento de Sistemas");
                        turma.setSemestre(2);
                        turma.setAno(2025);
                        turma.setInstituicao(instituicao);
                        return turmaRepository.save(turma);
                    });

            criarUsuarioDemo(userRepository, encoder, "aluno", alunoPassword,
                    "Aluno Padrao", "aluno@instituicao.example", "RA0001",
                    "ROLE_ALUNO", turmaPadrao, instituicao);

            criarUsuarioDemo(userRepository, encoder, "prof", professorPassword,
                    "Professor Padrao", "professor@instituicao.example", "PROF001",
                    "ROLE_PROFESSOR", turmaPadrao, instituicao);

            criarUsuarioDemo(userRepository, encoder, "admin", adminPassword,
                    "Administrador do Sistema", "admin@instituicao.example", "ADMIN001",
                    "ROLE_ADMIN", turmaPadrao, instituicao);
        };
    }

    private void criarUsuarioDemo(UserRepository userRepository,
                                  PasswordEncoder encoder,
                                  String username,
                                  String password,
                                  String nome,
                                  String email,
                                  String ra,
                                  String role,
                                  Turma turma,
                                  Instituicao instituicao) {
        if (userRepository.findByUsername(username) != null) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setName(nome);
        user.setEmail(email);
        user.setRa(ra);
        user.setRole(role);
        user.setTurma(turma);
        user.setInstituicao(instituicao);
        user.setStatus(StatusAluno.ATIVO);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.resetLoginFailures();
        userRepository.save(user);
    }

    private void validarSenhaDemo(String password, String propertyName) {
        if (password == null || password.length() < 12) {
            throw new IllegalStateException(propertyName + " deve ter pelo menos 12 caracteres quando dados demo estiverem ativos.");
        }
    }
}

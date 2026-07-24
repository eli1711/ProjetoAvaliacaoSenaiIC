package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.Aluno;
import br.com.cpa.questionario.model.StatusAluno;
import br.com.cpa.questionario.model.Turma;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.AlunoRepository;
import br.com.cpa.questionario.repository.TurmaRepository;
import br.com.cpa.questionario.repository.UserRepository;
import br.com.cpa.questionario.service.InstituicaoScopeService;
import br.com.cpa.questionario.service.PasswordPolicyService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private static final Logger log = LoggerFactory.getLogger(AlunoController.class);

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InstituicaoScopeService instituicaoScopeService;
    private final PasswordPolicyService passwordPolicyService;

    public AlunoController(AlunoRepository alunoRepository,
                           TurmaRepository turmaRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           InstituicaoScopeService instituicaoScopeService,
                           PasswordPolicyService passwordPolicyService) {
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.instituicaoScopeService = instituicaoScopeService;
        this.passwordPolicyService = passwordPolicyService;
    }

    // ===============================================================
    // LISTAR ALUNOS (RAIZ /alunos)
    // ===============================================================
    @GetMapping
    public String listarAlunosRaiz(Model model) {
        return listarAlunos(model);
    }

    // ===============================================================
    // LISTAR ALUNOS (/alunos/list)
    // ===============================================================
    @GetMapping("/list")
    public String listarAlunos(Model model) {
        List<Aluno> alunos = instituicaoScopeService.isSuperAdmin()
                ? alunoRepository.findAll()
                : instituicaoScopeService.getInstituicaoAtual()
                        .map(instituicao -> alunoRepository.findByInstituicaoId(instituicao.getId()))
                        .orElseGet(List::of);
        model.addAttribute("alunos", alunos);
        return "aluno/list"; // templates/aluno/list.html
    }

    // ===============================================================
    // DEFINIR TURMA (Aluno define sua própria turma)
    // ===============================================================
    @GetMapping("/definir-turma")
    public String escolherTurma(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        Aluno aluno = alunoRepository.findByUserUsername(username).orElse(null);
        if (aluno == null) {
            return "redirect:/home";
        }

        model.addAttribute("aluno", aluno);
        model.addAttribute("turmas", getTurmasPermitidas());
        return "aluno/definir_turma";
    }

    @PostMapping("/definir-turma")
    public String salvarTurma(@RequestParam Long turmaId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        Aluno aluno = alunoRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
        instituicaoScopeService.validarAcesso(turma.getInstituicao());

        aluno.setTurma(turma);
        aluno.setInstituicao(turma.getInstituicao());
        alunoRepository.save(aluno);

        // mantém User sincronizado
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setTurma(turma);
            user.setInstituicao(turma.getInstituicao());
            userRepository.save(user);
        }

        redirectAttributes.addFlashAttribute("success", "Turma definida com sucesso!");
        return "redirect:/home";
    }

    // ===============================================================
    // EXEMPLO DE CSV PARA IMPORTAR ALUNOS
    // ===============================================================
    @GetMapping("/import/exemplo")
    public void downloadExemploCsv(HttpServletResponse response) throws IOException {
        String filename = "exemplo_import_alunos.csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("nome;ra;cpf");
            writer.println("João da Silva;123456;12345678900");
        }
    }

    // ===============================================================
    // FORM DE IMPORTAÇÃO
    // ===============================================================
    @GetMapping("/import")
    public String importarAlunosForm(Model model) {
        model.addAttribute("turmas", getTurmasPermitidas());
        return "aluno/import";
    }

    // ===============================================================
    // IMPORTAR ALUNOS (CSV) - login = RA, senha inicial = CPF
    // ===============================================================
    @PostMapping("/import")
    public String importarAlunos(@RequestParam("file") MultipartFile file,
                                 @RequestParam("turmaId") Long turmaId,
                                 RedirectAttributes redirectAttributes) {

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Selecione um arquivo CSV para importar.");
            return "redirect:/alunos/import";
        }

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));
        instituicaoScopeService.validarAcesso(turma.getInstituicao());

        int criados = 0;
        int atualizados = 0;
        int ignorados = 0;
        int linhaAtual = 0;

        List<Aluno> alunosImportados = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                linhaAtual++;

                // pula cabeçalho "nome;ra;cpf"
                if (linhaAtual == 1 && line.toLowerCase().contains("nome;ra;cpf")) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                String[] cols = separarColunasCsv(line);
                if (cols.length < 3) {
                    ignorados++;
                    continue;
                }

                String nome = limparValorCsv(cols[0]);
                String ra = limparValorCsv(cols[1]);
                String cpf = passwordPolicyService.gerarSenhaInicialAluno(ra, cols[2]);

                if (ra.isEmpty() || cpf.isEmpty()) {
                    ignorados++;
                    continue;
                }

                String username = ra; // login = RA

                User userExistente = userRepository.findByUsername(username);
                if (userExistente == null) {
                    userExistente = userRepository.findByRa(ra);
                }
                boolean raJaExisteEmAluno = turma.getInstituicao() != null
                        ? alunoRepository.findByRaAndInstituicaoId(ra, turma.getInstituicao().getId()).isPresent()
                        : alunoRepository.findByRa(ra).isPresent();

                if (userExistente != null || raJaExisteEmAluno) {
                    if (atualizarSenhaTemporariaAluno(userExistente, cpf, turma)) {
                        atualizados++;
                    } else {
                        ignorados++;
                    }
                    continue;
                }

                // ====== cria User (login) ======
                User user = new User();
                user.setUsername(username);
                user.setName(nome);
                user.setEmail(ra + "@aluno.sem-email.com");
                user.setRole("ROLE_ALUNO");
                user.setStatus(StatusAluno.ATIVO);
                user.setTurma(turma);
                user.setRa(ra);
                user.setInstituicao(turma.getInstituicao());

                user.setPassword(passwordEncoder.encode(cpf));
                user.setMustChangePassword(true);
                user.setPasswordChangedAt(LocalDateTime.now());
                user.resetLoginFailures();

                userRepository.save(user);

                // ====== cria Aluno (tabela separada) ======
                Aluno aluno = new Aluno();
                aluno.setNome(nome);
                aluno.setRa(ra);
                aluno.setCpf(cpf);
                aluno.setEmail(user.getEmail());
                aluno.setUser(user);
                aluno.setTurma(turma);
                aluno.setInstituicao(turma.getInstituicao());

                alunoRepository.save(aluno);

                alunosImportados.add(aluno);
                criados++;
            }

        } catch (Exception e) {
            log.warn("Erro ao importar alunos por CSV", e);
            redirectAttributes.addFlashAttribute("error",
                    "Erro ao importar: " + e.getMessage());
            return "redirect:/alunos/import";
        }

        redirectAttributes.addFlashAttribute("success",
                "Importacao concluida. Criados: " + criados
                        + ", atualizados: " + atualizados
                        + ", ignorados: " + ignorados + ".");
        redirectAttributes.addFlashAttribute("alunosImportados", alunosImportados);

        return "redirect:/alunos/import";
    }

    private List<Turma> getTurmasPermitidas() {
        if (instituicaoScopeService.isSuperAdmin()) {
            return turmaRepository.findAll();
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> turmaRepository.findByInstituicaoId(instituicao.getId()))
                .orElseGet(List::of);
    }

    private String[] separarColunasCsv(String line) {
        String[] cols = line.split(";", -1);
        if (cols.length >= 3) {
            return cols;
        }
        return line.split(",", -1);
    }

    private String limparValorCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "").trim();
    }

    private boolean atualizarSenhaTemporariaAluno(User user, String cpf, Turma turma) {
        if (user == null || !ehAluno(user) || !user.isMustChangePassword()) {
            return false;
        }
        if (user.getInstituicao() != null) {
            instituicaoScopeService.validarAcesso(user.getInstituicao());
        } else if (turma != null) {
            user.setInstituicao(turma.getInstituicao());
        }
        user.setPassword(passwordEncoder.encode(cpf));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(true);
        user.resetLoginFailures();
        userRepository.save(user);
        return true;
    }

    private boolean ehAluno(User user) {
        return "ROLE_ALUNO".equals(user.getRole());
    }
}

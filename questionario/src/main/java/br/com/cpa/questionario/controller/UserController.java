package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.exception.PasswordPolicyException;
import br.com.cpa.questionario.model.*;
import br.com.cpa.questionario.repository.AlunoRepository;
import br.com.cpa.questionario.repository.InstituicaoRepository;
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
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final UserRepository userRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlunoRepository alunoRepository;
    private final InstituicaoScopeService instituicaoScopeService;
    private final PasswordPolicyService passwordPolicyService;

    public UserController(UserRepository userRepository,
                          InstituicaoRepository instituicaoRepository,
                          TurmaRepository turmaRepository,
                          PasswordEncoder passwordEncoder,
                          AlunoRepository alunoRepository,
                          InstituicaoScopeService instituicaoScopeService,
                          PasswordPolicyService passwordPolicyService) {
        this.userRepository = userRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
        this.alunoRepository = alunoRepository;
        this.instituicaoScopeService = instituicaoScopeService;
        this.passwordPolicyService = passwordPolicyService;
    }

    // ========== CADASTRO PÚBLICO DE ALUNO ==========

    @GetMapping("/aluno/registro")
    public String formCadastroAluno(Model model) {
        User user = new User();
        user.setStatus(StatusAluno.ATIVO);
        user.setRole("ROLE_ALUNO"); // default no objeto

        model.addAttribute("user", user);
        model.addAttribute("turmas", turmaRepository.findAll());
        model.addAttribute("instituicoes", List.of());
        model.addAttribute("superAdmin", false);
        model.addAttribute("cadastroAluno", true);
        model.addAttribute("passwordRules", passwordPolicyService.resumoRegras());

        return "user/edit"; // reutilizando o mesmo formulário
    }

    @PostMapping("/aluno/registrar")
    public String processarCadastroAluno(@ModelAttribute("user") User user,
                                         @RequestParam(value = "turmaId", required = false) Long turmaId,
                                         RedirectAttributes redirectAttributes) {

        // validações simples
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Login (RA) é obrigatório.");
            return "redirect:/users/aluno/registro";
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Senha e obrigatoria.");
            return "redirect:/users/aluno/registro";
        }

        user.setRa(user.getUsername());
        try {
            passwordPolicyService.validar(user.getPassword(), user);
        } catch (PasswordPolicyException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/users/aluno/registro";
        }

        if (userRepository.existsById(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Já existe um usuário com este login.");
            return "redirect:/users/aluno/registro";
        }

        // turma (pode ser null, mas sua regra de negócio é que ele escolha no primeiro login;
        // aqui podemos ignorar e deixar null, ou permitir escolher já)
        if (turmaId != null) {
            Turma turma = turmaRepository.findById(turmaId).orElse(null);
            user.setTurma(turma);
            user.setInstituicao(turma != null ? turma.getInstituicao() : null);
        } else {
            user.setTurma(null);
        }

        // força status e role
        user.setStatus(StatusAluno.ATIVO);
        user.setRole("ROLE_ALUNO");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.resetLoginFailures();

        userRepository.save(user);

        // cria o registro na tabela aluno
        Aluno aluno = new Aluno();
        aluno.setNome(user.getName());
        aluno.setRa(user.getUsername());
        aluno.setCpf("********");
        aluno.setEmail(user.getEmail());
        aluno.setUser(user);
        aluno.setTurma(user.getTurma());
        aluno.setInstituicao(user.getInstituicao());
        alunoRepository.save(aluno);

        redirectAttributes.addFlashAttribute("success",
                "Cadastro realizado com sucesso! Agora você já pode fazer login.");
        return "redirect:/login";
    }

    // ========== LISTA DE USUÁRIOS (ADMIN) ==========

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", getUsuariosPermitidos());
        model.addAttribute("superAdmin", instituicaoScopeService.isSuperAdmin());
        return "user/list";
    }

    // ========== FORMULÁRIO NOVO USUÁRIO (ADMIN) ==========

    @GetMapping("/new")
    public String newUser(Model model) {
        User user = new User();
        user.setStatus(StatusAluno.ATIVO);

        adicionarAtributosFormularioUsuario(model, user, false);
        return "user/edit";
    }

    // ========== EDITAR USUÁRIO EXISTENTE (ADMIN) ==========

    @GetMapping("/{username}/edit")
    public String editUser(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "redirect:/users";
        }
        instituicaoScopeService.validarAcesso(user.getInstituicao());
        user.setPassword(null);
        adicionarAtributosFormularioUsuario(model, user, false);
        return "user/edit";
    }

    // ========== SALVAR (CRIAR/ATUALIZAR) (ADMIN) ==========

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam(value = "turmaId", required = false) Long turmaId,
                           @RequestParam(value = "instituicaoId", required = false) Long instituicaoId,
                           RedirectAttributes redirectAttributes) {

        User existing = userRepository.findByUsername(user.getUsername());
        String redirectErro = existing == null
                ? "redirect:/users/new"
                : "redirect:/users/" + user.getUsername() + "/edit";
        boolean superAdmin = instituicaoScopeService.isSuperAdmin();

        if (existing != null) {
            instituicaoScopeService.validarAcesso(existing.getInstituicao());
        }
        if (!superAdmin && ROLE_SUPER_ADMIN.equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Somente o dev pode cadastrar usuario super admin.");
            return redirectErro;
        }

        Instituicao instituicaoSelecionada = null;
        if (superAdmin && instituicaoId != null) {
            instituicaoSelecionada = instituicaoRepository.findById(instituicaoId)
                    .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada"));
        }

        if (turmaId != null) {
            Turma turma = turmaRepository.findById(turmaId).orElse(null);
            if (turma != null) {
                instituicaoScopeService.validarAcesso(turma.getInstituicao());
            }
            if (superAdmin && instituicaoSelecionada != null && turma != null
                    && !mesmaInstituicao(instituicaoSelecionada, turma.getInstituicao())) {
                redirectAttributes.addFlashAttribute("error", "A turma selecionada pertence a outra instituicao.");
                return redirectErro;
            }
            user.setTurma(turma);
            user.setInstituicao(turma != null ? turma.getInstituicao() : instituicaoSelecionada);
        } else {
            user.setTurma(null);
            user.setInstituicao(superAdmin
                    ? instituicaoSelecionada
                    : instituicaoScopeService.instituicaoParaNovoRegistro(null));
        }

        if (ROLE_SUPER_ADMIN.equals(user.getRole())) {
            user.setTurma(null);
            user.setInstituicao(null);
        } else if (user.getInstituicao() == null) {
            redirectAttributes.addFlashAttribute("error", "Selecione uma instituicao para este usuario.");
            return redirectErro;
        }

        String senhaInformada = user.getPassword();
        boolean alterarSenha = senhaInformada != null && !senhaInformada.isBlank();
        if (existing == null) {
            if (!alterarSenha) {
                redirectAttributes.addFlashAttribute("error", "Informe uma senha inicial para o usuario.");
                return redirectErro;
            }
            try {
                passwordPolicyService.validar(senhaInformada, user);
            } catch (PasswordPolicyException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
                return redirectErro;
            }
            user.setPassword(passwordEncoder.encode(senhaInformada));
            user.setMustChangePassword(true);
            user.setPasswordChangedAt(LocalDateTime.now());
            user.resetLoginFailures();
        } else {
            if (!alterarSenha) {
                user.setPassword(existing.getPassword());
                copiarControlesDeSeguranca(existing, user);
            } else {
                try {
                    passwordPolicyService.validar(senhaInformada, user);
                } catch (PasswordPolicyException ex) {
                    redirectAttributes.addFlashAttribute("error", ex.getMessage());
                    return redirectErro;
                }
                user.setPassword(passwordEncoder.encode(senhaInformada));
                user.setMustChangePassword(true);
                user.setPasswordChangedAt(LocalDateTime.now());
                user.resetLoginFailures();
            }
        }

        if (user.getStatus() == null) {
            user.setStatus(StatusAluno.ATIVO);
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Usuário salvo com sucesso!");
        return "redirect:/users";
    }

    // ========== EXCLUIR (ADMIN) ==========

    @PostMapping("/{username}/delete")
    public String delete(@PathVariable String username,
                         RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            instituicaoScopeService.validarAcesso(user.getInstituicao());
            userRepository.delete(user);
        }
        redirectAttributes.addFlashAttribute("success", "Usuário excluído com sucesso!");
        return "redirect:/users";
    }

    // ========= CSV MODELO DE ALUNOS =========

    @GetMapping("/alunos/template-csv")
    public void downloadAlunoTemplate(HttpServletResponse response) throws IOException {
        String filename = "modelo_cadastro_alunos.csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("nome;ra;cpf");
            writer.println("João da Silva;123456;12345678900");
        }
    }

    // ========= IMPORTAR CSV DE ALUNOS =========
    // Formato: Nome;RA;CPF. Login = RA, senha inicial = CPF.
    @PostMapping("/alunos/import")
    public String importAlunosCsv(@RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nenhum arquivo CSV enviado.");
            return "redirect:/users";
        }

        int criados = 0;
        int atualizados = 0;
        int ignorados = 0;
        int linhaAtual = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                linhaAtual++;

                // Cabeçalho: nome;ra;cpf
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
                if (userExistente != null) {
                    if (atualizarSenhaTemporariaAluno(userExistente, cpf)) {
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
                user.setEmail(ra + "@aluno.sem-email.com"); // pode trocar depois
                user.setRole("ROLE_ALUNO");
                user.setStatus(StatusAluno.ATIVO);
                user.setTurma(null); // será definido no primeiro login
                user.setRa(ra);
                user.setInstituicao(instituicaoScopeService.instituicaoParaNovoRegistro(null));

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
                aluno.setTurma(null);
                aluno.setInstituicao(user.getInstituicao());

                alunoRepository.save(aluno);

                criados++;
            }

        } catch (Exception e) {
            log.warn("Erro ao processar importacao de usuarios por CSV", e);
            redirectAttributes.addFlashAttribute("error",
                    "Erro ao processar CSV: " + e.getMessage());
            return "redirect:/users";
        }

        redirectAttributes.addFlashAttribute("success",
                "Importacao concluida. Criados: " + criados
                        + ", atualizados: " + atualizados
                        + ", ignorados: " + ignorados + ".");
        return "redirect:/users";
    }

    private void adicionarAtributosFormularioUsuario(Model model, User user, boolean cadastroAluno) {
        boolean superAdmin = instituicaoScopeService.isSuperAdmin();
        model.addAttribute("user", user);
        model.addAttribute("turmas", getTurmasPermitidas());
        model.addAttribute("instituicoes", superAdmin ? instituicaoRepository.findAll() : List.of());
        model.addAttribute("superAdmin", superAdmin);
        model.addAttribute("cadastroAluno", cadastroAluno);
        model.addAttribute("passwordRules", passwordPolicyService.resumoRegras());
    }

    private List<User> getUsuariosPermitidos() {
        if (instituicaoScopeService.isSuperAdmin()) {
            return userRepository.findAll();
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> userRepository.findByInstituicaoId(instituicao.getId()))
                .orElseGet(List::of);
    }

    private List<Turma> getTurmasPermitidas() {
        if (instituicaoScopeService.isSuperAdmin()) {
            return turmaRepository.findAll();
        }
        return instituicaoScopeService.getInstituicaoAtual()
                .map(instituicao -> turmaRepository.findByInstituicaoId(instituicao.getId()))
                .orElseGet(List::of);
    }

    private boolean mesmaInstituicao(Instituicao a, Instituicao b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null) {
            return a == b;
        }
        return a.getId().equals(b.getId());
    }

    private void copiarControlesDeSeguranca(User origem, User destino) {
        destino.setFailedLoginAttempts(origem.getFailedLoginAttempts());
        destino.setLockedUntil(origem.getLockedUntil());
        destino.setPasswordChangedAt(origem.getPasswordChangedAt());
        destino.setMustChangePassword(origem.isMustChangePassword());
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

    private boolean atualizarSenhaTemporariaAluno(User user, String cpf) {
        if (user == null || !ehAluno(user) || !user.isMustChangePassword()) {
            return false;
        }
        instituicaoScopeService.validarAcesso(user.getInstituicao());
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

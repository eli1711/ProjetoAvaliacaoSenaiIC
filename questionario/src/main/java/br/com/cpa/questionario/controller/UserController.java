package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.StatusAluno;
import br.com.cpa.questionario.model.Turma;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.TurmaRepository;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          TurmaRepository turmaRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // LISTA DE USUÁRIOS (já está funcionando)
    @GetMapping
    public String list(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user/list";    // sua tela "Usuários Cadastrados"
    }

    // FORMULÁRIO NOVO USUÁRIO
    // >>> NÃO coloque @PreAuthorize aqui por enquanto <<<
    @GetMapping("/new")
    public String newUser(Model model) {
        User user = new User();
        user.setStatus(StatusAluno.ATIVO);

        model.addAttribute("user", user);
        model.addAttribute("turmas", turmaRepository.findAll());
        return "user/edit";    // é o HTML que você mandou
    }

    // EDITAR USUÁRIO EXISTENTE
    @GetMapping("/{username}/edit")
    public String editUser(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "redirect:/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("turmas", turmaRepository.findAll());
        return "user/edit";
    }

    // SALVAR (CRIAR/ATUALIZAR)
    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam(value = "turmaId", required = false) Long turmaId,
                           RedirectAttributes redirectAttributes) {

        // se vier turmaId, associa
        if (turmaId != null) {
            Turma turma = turmaRepository.findById(turmaId).orElse(null);
            user.setTurma(turma);
        } else {
            user.setTurma(null);
        }

        // se for novo usuário ou senha alterada, criptografa
        User existing = userRepository.findByUsername(user.getUsername());
        if (existing == null) {
            // novo
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // edição: se campo senha veio vazio, mantém a antiga
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(existing.getPassword());
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }

        if (user.getStatus() == null) {
            user.setStatus(StatusAluno.ATIVO);
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Usuário salvo com sucesso!");
        return "redirect:/users";
    }

    // EXCLUIR
    @PostMapping("/{username}/delete")
    public String delete(@PathVariable String username,
                         RedirectAttributes redirectAttributes) {
        userRepository.deleteById(username);
        redirectAttributes.addFlashAttribute("success", "Usuário excluído com sucesso!");
        return "redirect:/users";
    }
}

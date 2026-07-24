package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.exception.PasswordPolicyException;
import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import br.com.cpa.questionario.service.AuditService;
import br.com.cpa.questionario.service.PasswordPolicyService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/minha-conta")
public class ContaController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final AuditService auditService;

    public ContaController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           PasswordPolicyService passwordPolicyService,
                           AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
        this.auditService = auditService;
    }

    @GetMapping("/senha")
    public String alterarSenhaForm(Model model, Principal principal) {
        User user = usuarioAtual(principal);
        model.addAttribute("mustChangePassword", user != null && user.isMustChangePassword());
        model.addAttribute("passwordRules", passwordPolicyService.resumoRegras());
        return "conta/alterar-senha";
    }

    @PostMapping("/senha")
    public String alterarSenha(@RequestParam("senhaAtual") String senhaAtual,
                               @RequestParam("novaSenha") String novaSenha,
                               @RequestParam("confirmacao") String confirmacao,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User user = usuarioAtual(principal);
        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(senhaAtual, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Nao foi possivel alterar a senha. Verifique os dados informados.");
            return "redirect:/minha-conta/senha";
        }

        if (!novaSenha.equals(confirmacao)) {
            redirectAttributes.addFlashAttribute("error", "A confirmacao deve ser igual a nova senha.");
            return "redirect:/minha-conta/senha";
        }

        try {
            passwordPolicyService.validar(novaSenha, user);
        } catch (PasswordPolicyException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/minha-conta/senha";
        }

        user.setPassword(passwordEncoder.encode(novaSenha));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.resetLoginFailures();
        userRepository.save(user);

        auditService.registrar(
                "SENHA_ALTERADA",
                "User",
                user.getUsername(),
                user.getInstituicao(),
                "Senha alterada pelo proprio usuario.");

        redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso.");
        return "redirect:/home";
    }

    private User usuarioAtual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName());
    }
}

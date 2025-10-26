package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Lista todos os usuários
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user/list";
    }

    // Exibe o formulário de criação
    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        return "user/new";
    }

    // Salva o usuário
    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/users";
    }

    // Deleta usuário
    @PostMapping("/{username}/delete")
    public String deleteUser(@PathVariable String username) {
        userRepository.deleteById(username);
        return "redirect:/users";
    }
}

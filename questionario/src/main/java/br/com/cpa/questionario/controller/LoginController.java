package br.com.cpa.questionario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/home")
    public String home() { return "redirect:/questionnaires"; }

    @GetMapping("/error")
    public String error() { return "error"; }
}

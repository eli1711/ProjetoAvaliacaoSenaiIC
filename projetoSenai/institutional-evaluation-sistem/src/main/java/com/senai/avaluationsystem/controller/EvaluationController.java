package com.senai.avaluationsystem.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    
    @GetMapping("/test")
    public String test() {
        return "Sistema de Avaliação Institucional está funcionando!";
    }
}
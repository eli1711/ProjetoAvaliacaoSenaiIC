package com.senai.avaluationsystem.controller;

import com.senai.avaluationsystem.dto.QuestionRequest;
import com.senai.avaluationsystem.dto.QuestionResponse;
import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService service;

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        return ResponseEntity.ok(service.getAllQuestions());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<QuestionResponse>> getByType(@PathVariable QuestionType type) {
        return ResponseEntity.ok(service.getQuestionsByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getQuestionById(id));
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(@Valid @RequestBody QuestionRequest request,
                                            BindingResult result) {
        if (result.hasErrors()) {
            String erros = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body("Erro de validação: " + erros);
        }

        try {
            QuestionResponse response = service.createQuestion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar questão: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id,
                                            @Valid @RequestBody QuestionRequest request,
                                            BindingResult result) {
        if (result.hasErrors()) {
            String erros = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body("Erro de validação: " + erros);
        }

        try {
            QuestionResponse response = service.updateQuestion(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar questão: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            service.deleteQuestion(id);
            return ResponseEntity.ok("Questão excluída com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir questão: " + e.getMessage());
        }
    }
}

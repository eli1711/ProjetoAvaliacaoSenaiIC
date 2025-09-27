package com.senai.avaluationsystem.controller;

import com.senai.avaluationsystem.dto.AnswerRequest;
import com.senai.avaluationsystem.model.Answer;
import com.senai.avaluationsystem.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    /**
     * Cria uma nova resposta
     */
    @PostMapping
    public ResponseEntity<?> createAnswer(@Valid @RequestBody AnswerRequest request) {
        try {
            Answer saved = answerService.createAnswer(request);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("❌ Erro ao salvar resposta: " + e.getMessage());
        }
    }

    /**
     * Retorna todas as respostas
     */
    @GetMapping
    public ResponseEntity<List<Answer>> getAllAnswers() {
        return ResponseEntity.ok(answerService.getAllAnswers());
    }

    /**
     * Retorna respostas de uma questão específica
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getAnswersByQuestion(@PathVariable Long questionId) {
        try {
            return ResponseEntity.ok(answerService.getAnswersByQuestion(questionId));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("❌ Erro ao buscar respostas: " + e.getMessage());
        }
    }
}

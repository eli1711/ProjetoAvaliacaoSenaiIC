package com.senai.avaluationsystem.controller;

import com.senai.avaluationsystem.dto.QuestionRequest;
import com.senai.avaluationsystem.dto.QuestionResponse;
import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        try {
            logger.info("Recebida requisição GET /api/questions");
            List<QuestionResponse> questions = questionService.getAllQuestions();
            logger.info("Retornando {} questões", questions.size());
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Erro interno ao processar requisição GET /api/questions", e);
            return ResponseEntity.internalServerError()
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getQuestionsByType(@PathVariable QuestionType type) {
        try {
            logger.info("Recebida requisição GET /api/questions/type/{}", type);
            List<QuestionResponse> questions = questionService.getQuestionsByType(type);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Erro ao buscar questões por tipo: {}", type, e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao buscar questões por tipo: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        try {
            logger.info("Recebida requisição GET /api/questions/{}", id);
            QuestionResponse question = questionService.getQuestionById(id);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            logger.warn("Questão não encontrada: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar questão: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao buscar questão: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody QuestionRequest questionRequest) {
        try {
            logger.info("Recebida requisição POST /api/questions");
            QuestionResponse createdQuestion = questionService.createQuestion(questionRequest);
            return ResponseEntity.ok(createdQuestion);
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao criar questão", e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao criar questão: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long id, 
            @RequestBody QuestionRequest questionRequest) {
        try {
            logger.info("Recebida requisição PUT /api/questions/{}", id);
            QuestionResponse updatedQuestion = questionService.updateQuestion(id, questionRequest);
            return ResponseEntity.ok(updatedQuestion);
        } catch (RuntimeException e) {
            logger.warn("Questão não encontrada para atualização: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar questão: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao atualizar questão: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            logger.info("Recebida requisição DELETE /api/questions/{}", id);
            questionService.deleteQuestion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("Questão não encontrada para exclusão: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao excluir questão: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao excluir questão: " + e.getMessage());
        }
    }
}
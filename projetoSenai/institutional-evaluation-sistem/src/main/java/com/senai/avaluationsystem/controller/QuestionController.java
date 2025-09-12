package com.senai.avaluationsystem.controller;

import com.senai.avaluationsystem.dto.QuestionRequest;
import com.senai.avaluationsystem.dto.QuestionResponse;
import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*") // Permite requisições do frontend
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        List<QuestionResponse> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByType(@PathVariable QuestionType type) {
        List<QuestionResponse> questions = questionService.getQuestionsByType(type);
        return ResponseEntity.ok(questions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        QuestionResponse question = questionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }
    
    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@RequestBody QuestionRequest questionRequest) {
        QuestionResponse createdQuestion = questionService.createQuestion(questionRequest);
        return ResponseEntity.ok(createdQuestion);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id, 
            @RequestBody QuestionRequest questionRequest) {
        QuestionResponse updatedQuestion = questionService.updateQuestion(id, questionRequest);
        return ResponseEntity.ok(updatedQuestion);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
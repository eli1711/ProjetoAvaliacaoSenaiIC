package com.senai.avaluationsystem.service;

import com.senai.avaluationsystem.dto.QuestionRequest;
import com.senai.avaluationsystem.dto.QuestionResponse;
import com.senai.avaluationsystem.model.Question;
import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<QuestionResponse> getQuestionsByType(QuestionType type) {
        return questionRepository.findByType(type).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com id: " + id));
        return convertToResponse(question);
    }
    
    public QuestionResponse createQuestion(QuestionRequest questionRequest) {
        Question question = convertToEntity(questionRequest);
        Question savedQuestion = questionRepository.save(question);
        return convertToResponse(savedQuestion);
    }
    
    public QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest) {
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com id: " + id));
        
        // Atualizar os campos
        existingQuestion.setText(questionRequest.getText());
        existingQuestion.setType(questionRequest.getType());
        existingQuestion.setMinScale(questionRequest.getMinScale());
        existingQuestion.setMaxScale(questionRequest.getMaxScale());
        existingQuestion.setResponseType(questionRequest.getResponseType());
        existingQuestion.setRequired(questionRequest.getRequired());
        
        Question updatedQuestion = questionRepository.save(existingQuestion);
        return convertToResponse(updatedQuestion);
    }
    
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Questão não encontrada com id: " + id);
        }
        questionRepository.deleteById(id);
    }
    
    private Question convertToEntity(QuestionRequest request) {
        Question question = new Question();
        question.setText(request.getText());
        question.setType(request.getType());
        question.setMinScale(request.getMinScale());
        question.setMaxScale(request.getMaxScale());
        question.setResponseType(request.getResponseType());
        question.setRequired(request.getRequired());
        return question;
    }
    
    private QuestionResponse convertToResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setText(question.getText());
        response.setType(question.getType());
        response.setMinScale(question.getMinScale());
        response.setMaxScale(question.getMaxScale());
        response.setResponseType(question.getResponseType());
        response.setRequired(question.getRequired());
        return response;
    }
}
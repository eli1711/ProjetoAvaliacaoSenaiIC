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
    private QuestionRepository repository;

    public List<QuestionResponse> getAllQuestions() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<QuestionResponse> getQuestionsByType(QuestionType type) {
        return repository.findByType(type)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse getQuestionById(Long id) {
        Question q = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com id " + id));
        return toResponse(q);
    }

    public QuestionResponse createQuestion(QuestionRequest request) {
        Question q = new Question();
        q.setText(request.getText());
        q.setType(request.getType());
        q.setMinScale(request.getMinScale());
        q.setMaxScale(request.getMaxScale());
        q.setResponseType(request.getResponseType());
        q.setRequired(request.getRequired());

        Question saved = repository.save(q);
        return toResponse(saved);
    }

    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question q = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com id " + id));

        q.setText(request.getText());
        q.setType(request.getType());
        q.setMinScale(request.getMinScale());
        q.setMaxScale(request.getMaxScale());
        q.setResponseType(request.getResponseType());
        q.setRequired(request.getRequired());

        Question updated = repository.save(q);
        return toResponse(updated);
    }

    public void deleteQuestion(Long id) {
        Question q = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com id " + id));
        repository.delete(q);
    }

    private QuestionResponse toResponse(Question q) {
        QuestionResponse dto = new QuestionResponse();
        dto.setId(q.getId());
        dto.setText(q.getText());
        dto.setType(q.getType());
        dto.setMinScale(q.getMinScale());
        dto.setMaxScale(q.getMaxScale());
        dto.setResponseType(q.getResponseType());
        dto.setRequired(q.getRequired());
        return dto;
    }
}

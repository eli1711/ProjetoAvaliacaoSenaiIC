package com.senai.avaluationsystem.service;

import com.senai.avaluationsystem.dto.AnswerRequest;
import com.senai.avaluationsystem.model.Answer;
import com.senai.avaluationsystem.model.Question;
import com.senai.avaluationsystem.repository.AnswerRepository;
import com.senai.avaluationsystem.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public Answer saveAnswer(AnswerRequest request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Questão não encontrada"));

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswerText(request.getAnswerText());

        return answerRepository.save(answer);
    }

    public List<Answer> getAnswersByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada"));

        return answerRepository.findByQuestion(question);
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }
}

package com.senai.avaluationsystem.service;

import com.senai.avaluationsystem.dto.AnswerRequest;
import com.senai.avaluationsystem.model.Answer;
import com.senai.avaluationsystem.model.Question;
import com.senai.avaluationsystem.repository.AnswerRepository;
import com.senai.avaluationsystem.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * Cria e persiste uma resposta no banco.
     */
    @Transactional
    public Answer createAnswer(AnswerRequest request) {
        // Busca a questão; lança erro claro se não existir
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Questão não encontrada: id=" + request.getQuestionId()));

        Answer answer = new Answer();
        answer.setAnswerText(request.getAnswerText());
        answer.setQuestion(question);

        return answerRepository.save(answer);
    }

    /**
     * Lista todas as respostas.
     */
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    /**
     * Lista respostas de uma questão específica.
     */
    public List<Answer> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }
}

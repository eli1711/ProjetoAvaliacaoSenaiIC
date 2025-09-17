package com.senai.avaluationsystem.service;

import com.senai.avaluationsystem.dto.QuestionRequest;
import com.senai.avaluationsystem.dto.QuestionResponse;
import com.senai.avaluationsystem.model.Question;
import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.model.ResponseType;
import com.senai.avaluationsystem.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    
    @Autowired
    private QuestionRepository questionRepository;
    
    public List<QuestionResponse> getAllQuestions() {
        try {
            logger.info("Buscando todas as questões");
            List<Question> questions = questionRepository.findAll();
            logger.info("Encontradas {} questões", questions.size());
            return questions.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            logger.error("Erro ao acessar o banco de dados para buscar todas as questões", e);
            throw new RuntimeException("Erro ao buscar questões. Verifique a conexão com o banco de dados.", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar questões", e);
            throw new RuntimeException("Erro inesperado ao buscar questões.", e);
        }
    }
    
    public List<QuestionResponse> getQuestionsByType(QuestionType type) {
        try {
            logger.info("Buscando questões do tipo: {}", type);
            List<Question> questions = questionRepository.findByType(type);
            logger.info("Encontradas {} questões do tipo {}", questions.size(), type);
            return questions.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar questões por tipo: {}", type, e);
            throw new RuntimeException("Erro ao buscar questões por tipo.", e);
        }
    }
    
    public QuestionResponse getQuestionById(Long id) {
        try {
            logger.info("Buscando questão com ID: {}", id);
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Questão não encontrada com ID: {}", id);
                        return new RuntimeException("Questão não encontrada com id: " + id);
                    });
            return convertToResponse(question);
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar questão com ID: {}", id, e);
            throw new RuntimeException("Erro ao buscar questão.", e);
        }
    }
    
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest questionRequest) {
        try {
            logger.info("Criando nova questão: {}", questionRequest.getText());
            validateQuestionRequest(questionRequest);
            
            Question question = convertToEntity(questionRequest);
            Question savedQuestion = questionRepository.save(question);
            
            logger.info("Questão criada com sucesso. ID: {}", savedQuestion.getId());
            return convertToResponse(savedQuestion);
            
        } catch (DataAccessException e) {
            logger.error("Erro ao salvar questão no banco de dados", e);
            throw new RuntimeException("Erro ao salvar questão. Verifique os dados e a conexão com o banco.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Dados inválidos para criação de questão", e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar questão", e);
            throw new RuntimeException("Erro inesperado ao criar questão.", e);
        }
    }
    
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest) {
        try {
            logger.info("Atualizando questão com ID: {}", id);
            validateQuestionRequest(questionRequest);
            
            Question existingQuestion = questionRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Questão não encontrada para atualização. ID: {}", id);
                        return new RuntimeException("Questão não encontrada com id: " + id);
                    });
            
            // Atualizar os campos
            existingQuestion.setText(questionRequest.getText());
            existingQuestion.setType(questionRequest.getType());
            existingQuestion.setMinScale(questionRequest.getMinScale());
            existingQuestion.setMaxScale(questionRequest.getMaxScale());
            existingQuestion.setResponseType(questionRequest.getResponseType());
            existingQuestion.setRequired(questionRequest.getRequired());
            
            Question updatedQuestion = questionRepository.save(existingQuestion);
            logger.info("Questão atualizada com sucesso. ID: {}", id);
            
            return convertToResponse(updatedQuestion);
            
        } catch (DataAccessException e) {
            logger.error("Erro ao atualizar questão com ID: {}", id, e);
            throw new RuntimeException("Erro ao atualizar questão.", e);
        }
    }
    
    @Transactional
    public void deleteQuestion(Long id) {
        try {
            logger.info("Excluindo questão com ID: {}", id);
            
            if (!questionRepository.existsById(id)) {
                logger.warn("Tentativa de excluir questão inexistente. ID: {}", id);
                throw new RuntimeException("Questão não encontrada com id: " + id);
            }
            
            questionRepository.deleteById(id);
            logger.info("Questão excluída com sucesso. ID: {}", id);
            
        } catch (EmptyResultDataAccessException e) {
            logger.warn("Questão não encontrada para exclusão. ID: {}", id);
            throw new RuntimeException("Questão não encontrada com id: " + id, e);
        } catch (DataAccessException e) {
            logger.error("Erro ao excluir questão com ID: {}", id, e);
            throw new RuntimeException("Erro ao excluir questão.", e);
        }
    }
    
    private void validateQuestionRequest(QuestionRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Texto da questão é obrigatório");
        }
        
        if (request.getType() == null) {
            throw new IllegalArgumentException("Tipo da questão é obrigatório");
        }
        
        if (request.getRequired() == null) {
            request.setRequired(false); // Valor padrão
        }
        
        // Validações específicas por tipo
        if (request.getType() == QuestionType.QUANTITATIVE) {
            if (request.getMinScale() == null || request.getMaxScale() == null) {
                throw new IllegalArgumentException("Escala mínima e máxima são obrigatórias para questões quantitativas");
            }
            if (request.getMinScale() >= request.getMaxScale()) {
                throw new IllegalArgumentException("Escala máxima deve ser maior que a escala mínima");
            }
        } else if (request.getType() == QuestionType.QUALITATIVE) {
            if (request.getResponseType() == null) {
                request.setResponseType(ResponseType.TEXT); // Valor padrão
            }
        }
    }
    
    private Question convertToEntity(QuestionRequest request) {
        Question question = new Question();
        question.setText(request.getText().trim());
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
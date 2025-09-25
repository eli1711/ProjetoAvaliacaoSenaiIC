package com.senai.avaluationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnswerRequest {

    @NotNull(message = "O ID da questão é obrigatório")
    private Long questionId;

    @NotBlank(message = "A resposta não pode estar vazia")
    private String answerText;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}

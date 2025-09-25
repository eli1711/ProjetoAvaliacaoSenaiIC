package com.senai.avaluationsystem.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa uma lista de respostas enviadas no questionário.
 */
public class AnswerListRequest {

    @Valid
    private List<AnswerRequest> answers = new ArrayList<>();

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }
}
